package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluethoothMain extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private BtScanAdapter BtAdapter;
    private List<BtData> BtDataList, BtVinculados, Btsaved;
    private BtData BtActual;
    private BluetoothDevice dispositivoBluetooth;
    String version = "", ip = "", admin = "", pass = "", puerto ="";
    public BluetoothAdapter bluetoothAdapter;
    private Button buscar_bt, btn_prueba_imp;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private SharedPreferences prefences_printer;
    private SharedPreferences.Editor editor;
    private Gson gson;
    ConnectThread connectThread;
    public Handler handler;
    private TextView conect_bt; //mensaje para ver con cual esta conectado
    private ProgressDialog progress;
    private boolean flag_buscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluethooth_main);

        prefences_printer = this.getSharedPreferences("printer_bt", Context.MODE_PRIVATE);
        editor = prefences_printer.edit();

        gson = new Gson();
        String json = prefences_printer.getString("btlist", null);
        Type type = new TypeToken<ArrayList<BtData>>() {
        }.getType();
        Btsaved = gson.fromJson(json, type);

        flag_buscar = false; //verificamos si el usuario usa el boton de buscar
        connectThread = null;


        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");

        buscar_bt = findViewById(R.id.buscar_bt);
        btn_prueba_imp = findViewById(R.id.imp_bt);
        conect_bt = findViewById(R.id.conetado_bt);
        progress = new ProgressDialog(this);

        //manejamos las llamadas desde un tread para modificar la vista principal
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                if (bundle.getBoolean("exito")) {
                    btn_prueba_imp.setEnabled(true);
                    conect_bt.setText("Conectado con: " + BtActual.getNombre_bt().split("#")[0] + " \uD83D\uDDA8");
                    Toast.makeText(BluethoothMain.this, "Conexión éxitosa", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                } else {
                    Toast.makeText(BluethoothMain.this, "Conexión fallida", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            }
        };

        BtDataList = new ArrayList<>();
        BtVinculados = new ArrayList<>();
        if (Btsaved == null) {
            Btsaved = new ArrayList<>();
        }
        BtAdapter = new BtScanAdapter(BtDataList, this, new BtScanAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(BtData item) {
                moveToDescription(item);
            }

            @Override
            public void OnItemClickMenu(BtData item, View view) {
                menuOptionItem(item, view);
            }
        });

        RecyclerView mrecyclerview = findViewById(R.id.blue_adapter);

        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(BtAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Dispositivo no compatible con Bluetooth", Toast.LENGTH_LONG).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(BluethoothMain.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 100);

        }


        buscar_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_prueba_imp.setEnabled(false); //deshabilitaos el boton de imprimir
                verificaPermisoEscanea(); //verificamos permisos y mandamos a busacr
            }
        });

        btn_prueba_imp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectThread.printDataTest();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            if (requestCode == 100 || requestCode == 2) { //resultado de activar bluetooth
                Toast.makeText(this, "Necesitas hablitar permiso de Bluetooth", Toast.LENGTH_SHORT).show();
                buscar_bt.setEnabled(false);
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(BluethoothMain.this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }

                int k = 0;
                for (BtData btd : BtDataList) {
                    if (btd.getMac_bt().equals(device.getAddress())) {
                        k++;
                    }
                }
                if (k == 0)
                    BtDataList.add(new BtData(device.getName() + "#\uD83D\uDD0E", device.getAddress()));

            }
            // Codigo que se ejecutara cuando el Bluetooth finalice la busqueda de dispositivos.
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progress.dismiss();
            }
            BtAdapter.notifyDataSetChanged();
            flag_buscar = true; //confirmamos que hucismo una busqeda
        }
    };

    private void verificaPermisoEscanea() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Verificar si la aplicación tiene permiso para acceder a la ubicación aproximada del dispositivo (necesario para escanear redes Wi-Fi)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                // La aplicación tiene el permiso ACCESS_COARSE_LOCATION
                // Aquí puedes realizar las operaciones que requieren el permiso, como escanear redes Wi-Fi
                buscarBT();

            } else {
                // La aplicación no tiene el permiso ACCESS_COARSE_LOCATION
                // Aquí puedes solicitar el permiso al usuario utilizando el método requestPermissions()
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
            }
        }
        else {
            // Verificar si la aplicación tiene permiso para acceder a la ubicación aproximada del dispositivo (necesario para escanear redes Wi-Fi)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                // La aplicación tiene el permiso ACCESS_COARSE_LOCATION
                // Aquí puedes realizar las operaciones que requieren el permiso, como escanear redes Wi-Fi
                buscarBT();

            } else {
                // La aplicación no tiene el permiso ACCESS_COARSE_LOCATION
                // Aquí puedes solicitar el permiso al usuario utilizando el método requestPermissions()
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void buscarBT() {

        BtDataList.clear();
        if (ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(BluethoothMain.this, new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
        }
        //busqeuda de sipositivos ya vinculados
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BtDataList.add(new BtData(device.getName() + "#\uD83D\uDD17", device.getAddress()));
            }
        }
        BtAdapter.notifyDataSetChanged();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        boolean busq = bluetoothAdapter.startDiscovery();
        if (busq) {
            progress.setMessage("Buscando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }
        else{
            Toast.makeText(this, "No se encontró dispositivo BT cercano", Toast.LENGTH_LONG).show();
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(flag_buscar){
            unregisterReceiver(receiver);
            if(connectThread != null) {
                connectThread.cancel();
                connectThread.disconnectBT();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (connectThread != null) {
            connectThread.cancel();
            connectThread.disconnectBT();
        }
        Intent i = new Intent(BluethoothMain.this, Admin.class);
        i.putExtra("ip", ip);
        i.putExtra("puerto", puerto);
        i.putExtra("admin", admin);
        i.putExtra("pass", pass);
        i.putExtra("version", version);
        startActivity(i);
        finish();
    }

    private void moveToDescription(BtData item) {
        //al seelccionar un blutu
        // Obtenemos el dispositivo con la direccion seleccionada en la lista
        BtActual = item;
        progress.setMessage("Conectando...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();
        dispositivoBluetooth = bluetoothAdapter.getRemoteDevice(item.getMac_bt());
        if(connectThread != null) {
            connectThread.cancel();
            connectThread.disconnectBT();
            conect_bt.setText("No conectado");
            btn_prueba_imp.setEnabled(false);
        }
        connectThread = new ConnectThread(dispositivoBluetooth, getResources());
        connectThread.start();

    }

    private void menuOptionItem(BtData item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(BtData item, View view) {
        //mostrar menu l selecionar
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_bt);
        popupmenu.show();
        BtActual = item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
           /* case R.id.editar_router:
                return true;*/
            case R.id.eliminar_bt:
                boolean eli = false;
                if(Btsaved.size() > 0) {
                    int i;
                    for (i = 0; i < Btsaved.size(); i++) {
                        if (Btsaved.get(i).getMac_bt().equals(BtActual.getMac_bt())){
                            Log.d("IMP", Btsaved.get(i).getMac_bt());
                            Btsaved.remove(i);
                            editor = prefences_printer.edit();
                            Gson gson = new Gson();
                            String json = gson.toJson(Btsaved);
                            editor.putString("btlist", json);
                            editor.apply();
                            Toast.makeText(this, "Eliminada de la lista de impresoras", Toast.LENGTH_SHORT).show();
                            eli = true;
                            break;
                        }
                    }
                    if(i == Btsaved.size() && !eli) {
                        Toast.makeText(this, "No esta en la lista de impresoras", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(this, "Lista de impresora vacía", Toast.LENGTH_SHORT).show();

                }

                return true;
            case R.id.agregar_bt:
                for (int i = 0; i < Btsaved.size(); i++) {
                    if (Btsaved.get(i).getMac_bt().equals(BtActual.getMac_bt())){
                        Toast.makeText(this, "La impresora ya esta agregada", Toast.LENGTH_SHORT).show();
                        Log.d("IMP", Btsaved.get(i).getMac_bt()+" Ya esta");
                        return true;
                    }
                }
                Btsaved.add(new BtData(BtActual.getNombre_bt().split("#")[0], BtActual.getMac_bt()));
                editor = prefences_printer.edit();
                Gson gson1 = new Gson();
                String json1 = gson1.toJson(Btsaved);
                editor.putString("btlist", json1);
                editor.apply();
                Toast.makeText(this, "Agregada a la lista de impresoras", Toast.LENGTH_SHORT).show();

                return true;
            default:
                return false;
        }

    }

    private class ConnectThread extends Thread {

        private final byte[] ESC_ALIGN_LEFT = new byte[] { 0x1b, 'a', 0x00 };
        private final byte[] ESC_ALIGN_RIGHT = new byte[] { 0x1b, 'a', 0x02 };
        private final byte[] ESC_ALIGN_CENTER = new byte[] { 0x1b, 'a', 0x01 };
        private final byte[] ESC_CANCEL_BOLD = new byte[] { 0x1B, 0x45, 0 };
        private byte[] format = { 27, 33, 0 };
        private  byte[] arrayOfByte1 = { 27, 33, 0 };
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String MY_UUID = "00001101-0000-1000-8000-00805f9b34fb";
        private String TAG = "MyTAG";

        private byte[] readBuffer;
        private int readBufferPosition;
        private volatile boolean stopWorker;
        private Resources resources;
        private OutputStream outputStream;
        private InputStream inputStream;

        public ConnectThread(BluetoothDevice device,  Resources resources) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            this.resources = resources;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                if (ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(BluethoothMain.this, new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                mmSocket = tmp;
                outputStream = mmSocket.getOutputStream();
                inputStream = mmSocket.getInputStream();


            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            if (ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(BluethoothMain.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(BluethoothMain.this, new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }

            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                mmSocket.connect();
                final byte delimiter=10;
                stopWorker =false;
                readBufferPosition=0;
                readBuffer = new byte[1024];

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putBoolean("exito",true);
                message.setData(bundle);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                handler.sendMessage(message);

                while (!Thread.currentThread().isInterrupted() && !stopWorker){
                    try{
                        int byteAvailable = inputStream.available();
                        if(byteAvailable>0){
                            byte[] packetByte = new byte[byteAvailable];
                            inputStream.read(packetByte);
                            for(int i=0; i<byteAvailable; i++){
                                byte b = packetByte[i];
                                if(b==delimiter){
                                    byte[] encodedByte = new byte[readBufferPosition];
                                    System.arraycopy(
                                            readBuffer,0,
                                            encodedByte,0,
                                            encodedByte.length
                                    );
                                    final String data = new String(encodedByte,"US-ASCII");
                                    readBufferPosition=0;
                                }else{
                                    readBuffer[readBufferPosition++]=b;
                                }
                            }
                        }
                    }catch(Exception ex){
                        stopWorker=true;
                    }
                }


            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("exito",false);
                    message.setData(bundle);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    handler.sendMessage(message);
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);
        }
        // Closes the client socket and causes the thread to finish.
        public void printData() {
            try{

            //printPhoto(R.drawable.logmikroficha);
            //BOLD
            format[2] = ((byte)(0x8 | arrayOfByte1[2]));
            String hello = "MIKROFICHA\n";
            hello += "\n";
            hello += "\n";

            outputStream.write(ESC_CANCEL_BOLD);
            outputStream.write(hello.getBytes());
            /*
            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            outputStream.write(ESC_ALIGN_LEFT);
            outputStream.write(hello.getBytes());
            // Underline
            format[2] = ((byte)(0x80 | arrayOfByte1[2]));
            outputStream.write(ESC_ALIGN_RIGHT);
            outputStream.write(hello.getBytes());
            // Small
            format[2] = ((byte)(0x1 | arrayOfByte1[2]));
            outputStream.write(format);
            outputStream.write(hello.getBytes());*/


            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void printDataTest() {

            try{
                resetPrint();
                String merchantName = "MIKROFICHA";
                printCustom(merchantName,3,1);
                String restaurantName = "Conexión exitosa";
                printCustom(restaurantName,2,1);
                String restaurantAddress = "https://mikroficha.com";
                printCustom(restaurantAddress,1,1);
                printNewLine();
                printPhoto(R.drawable.logo_ticket);
                printNewLine();
                printNewLine();
                printNewLine();
                /*
                format[2] = ((byte)(0x8 | arrayOfByte1[2]));
                String mikroficha_print = "\n";
                mikroficha_print += "\n";
                mikroficha_print += "\n";
                mikroficha_print += "\n";
                mikroficha_print = "MIKROFICHA CONECTADA\n";
                mikroficha_print += "visita: https://mikroficha.com";
                mikroficha_print += "\n";
                mikroficha_print += "\n";
                mikroficha_print += "\n";
                outputStream.write(ESC_CANCEL_BOLD);
                outputStream.write(mikroficha_print.getBytes());
                */

                //print title
               /* printUnicode();
                //print normal text
                printCustom("MIKROFICHA CONECTADA",0,0);
                Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.logodos);
                PrintPic printpic1 = PrintPic.getInstance();
                printpic1.init(bm);
                byte[] bitmapdata2 = printpic1.printDraw();
                outputStream.write(bitmapdata2);
               // printPhoto(R.drawable.logodos);
                printNewLine();
                printText("     >>>>   Thank you  <<<<     "); // total 32 char in a single line
                //resetPrint(); //reset printer
                printUnicode();
                printNewLine();
                printNewLine();*/

                // ***********************************************

                /*Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.dos);
                PrintPic printpic1 = PrintPic.getInstance();
                printpic1.init(bm);
                byte[] bitmapdata2 = printpic1.printDraw();
                outputStream.write(bitmapdata2);*/

                // ***********************************************
               /* printCustom("To ",0,0);

                String customerName = "Abhinav Raj";
                printCustom(customerName,0,0);

                String customerAddress = "Jharkhand 12208, India";
                printCustom(customerAddress,0,0);

                String customerPhone = "Phone: +91-0987654321";
                printCustom(customerPhone,0,0);

                printUnicode();
                //***********************************************

                printText(leftRightAlign("Qty: Name" , "Price "));
                printCustom(new String(new char[32]).replace("\0", "."),0,1);
                */

                outputStream.flush();

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void disconnectBT() {
            try {
                stopWorker=true;
                outputStream.close();
                inputStream.close();
                mmSocket.close();
                //lblPrinterName.setText("Printer Disconnected.");
            }catch (Exception ex){
                ex.printStackTrace();

            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        //print custom
        private void printCustom(String msg, int size, int align) {
            //Print config "mode"
            byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
            //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
            byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
            byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
            try {
                switch (size){
                    case 0:
                        outputStream.write(cc);
                        break;
                    case 1:
                        outputStream.write(bb);
                        break;
                    case 2:
                        outputStream.write(bb2);
                        break;
                    case 3:
                        outputStream.write(bb3);
                        break;
                }

                switch (align){
                    case 0:
                        //left align
                        outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                        break;
                    case 1:
                        //center align
                        outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                        break;
                    case 2:
                        //right align
                        outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                        break;
                }
                outputStream.write(msg.getBytes());
                outputStream.write(PrinterCommands.LF);
                //outputStream.write(cc);
                //printNewLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //print photo
        public void printPhoto(int img) {
            try {
                //((BitmapDrawable) logo_img.getDrawable()).getBitmap();//
               Bitmap bmp = BitmapFactory.decodeResource(getResources(),img);
                if(bmp!=null){
                    //byte[] command = Utils.decodeBitmap(bmp);
                    byte[] command = Utils.bitmapToBytes(bmp,false);
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    printText(command);
                }else{
                    Log.e("PrintTools", "the file isn't exists");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PrintTools", "the file isn't "+e.getMessage());
            }
        }

        //print unicode
        public void printUnicode(){
            try {
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(Utils.UNICODE_TEXT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //print new line
        private void printNewLine() {
            try {
                outputStream.write(PrinterCommands.FEED_LINE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void resetPrint() {
            try{
                outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
                outputStream.write(PrinterCommands.FS_FONT_ALIGN);
                outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
                outputStream.write(PrinterCommands.LF);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //print text
        private void printText(String msg) {
            try {
                // Print normal text
                outputStream.write(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //print byte[]
        private void printText(byte[] msg) {
            try {
                // Print normal text
                outputStream.write(msg);
                printNewLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private String leftRightAlign(String str1, String str2) {
            String ans = str1 +str2;
            if(ans.length() <31){
                int n = (31 - str1.length() + str2.length());
                ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
            }
            return ans;
        }


        private String[] getDateTime() {
            final Calendar c = Calendar.getInstance();
            String dateTime [] = new String[2];
            dateTime[0] = c.get(Calendar.DAY_OF_MONTH) +"/"+ c.get(Calendar.MONTH) +"/"+ c.get(Calendar.YEAR);
            dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE);
            return dateTime;
        }
    }
}

