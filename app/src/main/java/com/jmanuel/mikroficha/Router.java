package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd; //anuncio poner comentario
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import javax.net.SocketFactory;

public class Router extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private RoutersAdapter routerAdapter;
    private List<RoutersMk> routers;
    private RoutersMk itemActual;
    final String FILE_NAME_ROUTER = "router.json";
    //CONEXIONES CON EL ROUTER
    private ApiConnection con;
    private ProgressDialog progress;
    private final String TAG = "Routerapp";

    //VARIABLES DE ANUNCIO =========
    boolean  completo;
    private Boolean ADMOB = true;
    private AdView adview;
    //==============================

    //LIMITES
    int LIMITE_ROUTER = 1;//1 //esta en la ap;
    private DatabaseReference mDatabase;
    SharedPreferences prefences, router_file_pref;
    String uuid_app;
    String PORT = "22";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    RecyclerView recyclerViewwifi;
    private List<WifiData> wifiDataList;
    private WifiScanAdapter wifiScanAdapter;
    private ScrollView scrollscan;
    private ProgressBar progressBarWifi;

    SharedPreferences.Editor editor;
    private Gson gson;
    private List<RoutersMk> listaRouterSaved;
    private int PORT_USER = 8728;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router);
        prefences = Router.this.getSharedPreferences("clave_uuid_app",Context.MODE_PRIVATE);
        uuid_app = prefences.getString("uuid_app","N/A");
        ADMOB = prefences.getBoolean("ADMOB",true);
        router_file_pref = this.getSharedPreferences("router_file", Context.MODE_PRIVATE);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        adview = findViewById(R.id.adView);

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).child("ADMOB").exists()) {
                        prefences = Router.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                        editor = prefences.edit();
                        editor.putBoolean("ADMOB", (Boolean) snapshot.child(uuid_app).child("ADMOB").getValue());
                        editor.apply();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //ANUNCIO de video reward
        completo = false; //si se vio el anuncio completo
        if(ADMOB) {
            adview.setVisibility(View.VISIBLE);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {

                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            adview.loadAd(adRequest);
        }
        else {
            adview.setVisibility(View.GONE);
        }

        progress = new ProgressDialog(this);
        cargar_recycler_router();
        routerAdapter = new RoutersAdapter(routers, this, new RoutersAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(RoutersMk item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(RoutersMk item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_mk);
        numeroRouter(); //quitar cmentario para publicar
        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(routerAdapter);

        verificaPermisoEscanea();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){ //habilita el wifi si no esta activo
            wifiManager.setWifiEnabled(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // unregisterReceiver(wifiScanReceiver); //liberar los recursos de Broadcastreciver
    }

    public void numeroRouter(){
        long interval = 3600;
        if(BuildConfig.DEBUG)
            interval = 5;

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings frconf = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(frconf);
        HashMap<String,Object> actualizacion = new HashMap<>();
        actualizacion.put("limiterouter",LIMITE_ROUTER);

        remoteConfig.setDefaultsAsync(actualizacion);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(Router.this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        cargaLimiteRouter();
                    }
                });
    }

    private void cargaLimiteRouter() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        String liruter = remoteConfig.getString("limiterouter");
        if(liruter != "")
            LIMITE_ROUTER = Integer.parseInt(liruter);

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).exists() && snapshot.child(uuid_app).child("MAX_ROUTER").exists()) {
                        LIMITE_ROUTER = Integer.parseInt(snapshot.child(uuid_app).child("MAX_ROUTER").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Router.this, MainActivity.class));
        finish();
    }

    public void cargar_recycler_router(){
        //CARGAMOS LOS ROUTER DE LA LISTA JSON
        editor = router_file_pref.edit();

        gson = new Gson();
        String json = router_file_pref.getString("routerlist", null);
        Type type = new TypeToken<ArrayList<RoutersMk>>(){}.getType();
        routers = gson.fromJson(json,type);
        if(routers == null) {
            routers = new ArrayList<>();
        }
    }
    public void agregar_router_archivo(String nombre, String ip,String puerto, String mac, String admin, String version, String pass){

        for(RoutersMk rmk: routers){
            if(rmk.getIp().split(" ")[0].equals(ip.split(" ")[0])){
                Toast.makeText(this, "Ya hay un router con la misma IP", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        routers.add(new RoutersMk(nombre,mac,ip,puerto,"Mikrotik | "+admin,"Versión: "+version,pass));
        editor = router_file_pref.edit();
        Gson gson1 = new Gson();
        String json1 = gson1.toJson(routers);
        editor.putString("routerlist", json1);
        editor.apply();
        routerAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Router agregado", Toast.LENGTH_SHORT).show();
    }
    public void eliminar_elemento_json(RoutersMk routeractual){
        //CARGAMOS LOS ROUTER DE LA LISTA JSON
        if(routers.size() > 0) {
            for(RoutersMk rmk: routers){
                if (rmk.getMac() == routeractual.getMac()) {
                    routers.remove(rmk);
                    Toast.makeText(this, "Router eliminado", Toast.LENGTH_SHORT).show();
                    editor = router_file_pref.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(routers);
                    editor.putString("routerlist", json);
                    editor.apply();
                    routerAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
    private void menuOptionItem(RoutersMk item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(RoutersMk item,View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_router);
        popupmenu.show();
        itemActual = item;
    }
    private void moveToDescription(RoutersMk item) {
        Intent i = new Intent(Router.this, Admin.class);
        i.putExtra("ip", item.getIp().split(" ")[0]);
        i.putExtra("puerto", item.getPuerto());
        i.putExtra("admin", item.getAdmin().split(" ")[2]);
        i.putExtra("pass", item.getContrasenia());
        i.putExtra("version", item.getVersion().split(":")[1]);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_router,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_router:
                if(routers.size() < LIMITE_ROUTER) {
                    AlertDialog.Builder alerta = new AlertDialog.Builder(Router.this);
                    LayoutInflater inflater = getLayoutInflater();

                    View view = inflater.inflate(R.layout.modal_agregar_router, null);
                    alerta.setView(view);
                    EditText ip = view.findViewById(R.id.ip_modal_router);
                    EditText puerto = view.findViewById(R.id.puerto_modal_router);
                    EditText usuario = view.findViewById(R.id.usuario_modal_router);
                    EditText contrasenia = view.findViewById(R.id.pass_modal_router);
                    EditText comentario = view.findViewById(R.id.comentario_modal_router);

                    Button btnAgregar = view.findViewById(R.id.btn_agregar_modal_router);
                    Button btnCancelar = view.findViewById(R.id.btn_cancelar_modal_router);

                    AlertDialog dialog = alerta.create();
                    dialog.setCancelable(false);
                    dialog.show();
                    if(!puerto.getText().toString().isEmpty())
                        PORT_USER = Integer.parseInt(puerto.getText().toString());


                    ip.requestFocus();

                    btnAgregar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String mac = "";
                            if (!ip.getText().toString().isEmpty() &&
                                    !usuario.getText().toString().isEmpty() &&
                                    !comentario.getText().toString().isEmpty()
                            ) {
                                if (Pattern.matches("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$",
                                        ip.getText().toString())) {
                                    TaskConexion conectar = new TaskConexion(con, ip.getText().toString(), PORT_USER,
                                            usuario.getText().toString(), contrasenia.getText().toString(),
                                            mac, comentario.getText().toString());
                                    conectar.execute();
                                    dialog.dismiss();
                                } else {
                                    ip.requestFocus();
                                    Toast.makeText(Router.this, "La ip debe ser en un formato válido", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(Router.this, "Llene todos los campos", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                    btnCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
                else{
                    Toast.makeText(Router.this,"Límite de router es: "+LIMITE_ROUTER,Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.analiza_router:
                AlertDialog.Builder alerta = new AlertDialog.Builder(Router.this);
                LayoutInflater inflater = getLayoutInflater();

                wifiDataList = new ArrayList<>();
                wifiScanAdapter = new WifiScanAdapter(wifiDataList, this, new WifiScanAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(WifiData item) {

                    }

                    @Override
                    public void OnItemClickMenu(WifiData item, View view) {

                    }
                });

                View view = inflater.inflate(R.layout.modal_wifiscan, null);
                alerta.setView(view);
                recyclerViewwifi = view.findViewById(R.id.wifi_recyclerview);
                scrollscan = view.findViewById(R.id.scrollscan);
                progressBarWifi = view.findViewById(R.id.progressBarWifi);

                recyclerViewwifi.setHasFixedSize(true);
                recyclerViewwifi.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewwifi.setAdapter(wifiScanAdapter);

                Button btnBuscar = view.findViewById(R.id.btn_wifi_buscar);
                Button btnCancelar = view.findViewById(R.id.btn_wifi_cancelar);

                AlertDialog dialog = alerta.create();
                dialog.setCancelable(false);
                dialog.show();

                btnBuscar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       // verificaPermisoEscanea();
                    }
                });

                btnCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verificaPermisoEscanea(){

        // Verificar si la aplicación tiene permiso para acceder a la ubicación aproximada del dispositivo (necesario para escanear redes Wi-Fi)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            // La aplicación tiene el permiso ACCESS_COARSE_LOCATION
            // Aquí puedes realizar las operaciones que requieren el permiso, como escanear redes Wi-Fi
            //Log.d("Wifix","hoola");
            //TaskUsuarioSSH conectar = new TaskUsuarioSSH("192.168.1.69","admin","477426997Aa");
            //conectar.execute();

        } else {
            // La aplicación no tiene el permiso ACCESS_COARSE_LOCATION
            // Aquí puedes solicitar el permiso al usuario utilizando el método requestPermissions()
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT}, 101);
        }

    }

    private void scanWifiNetworks() {
         // Escanear redes Wi-Fi

        Log.d("WifiScanner", "==========================");
         wifiScanReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                    //scrollscan.setVisibility(View.GONE);
                   // progressBarWifi.setVisibility(View.VISIBLE);
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    wifiDataList.clear();
                    for (ScanResult result : scanResults) {
                        if(result.BSSID.startsWith("2c:3a:e8")){
                            String ssid = result.SSID; // Nombre de la red Wi-Fi (SSID)
                            String bssid = result.BSSID; // Dirección MAC del punto de acceso
                            int signalStrength = result.level; // Nivel de potencia de la señal en dBm
                            int frequency = result.frequency; // Frecuencia de la señal en MHz
                            String capabilities = result.capabilities; // Características de seguridad de la red Wi-Fi
                            WifiInfo info = wifiManager.getConnectionInfo();
                            int ipaddrs = info.getIpAddress();
                            String modelo = info.getSSID();

                            String ip = "192.168.0.1";

                            wifiDataList.add(new WifiData(ssid,bssid,signalStrength, frequency, ip));
                            // Imprimir la información en la consola
                            Log.d("WifiScanner", "SSID: " + ssid + ", BSSID: " + bssid
                                    + ", Potencia de señal: " + signalStrength + "dBm, Frecuencia: "
                                    + frequency + "MHz, Características de seguridad: " + capabilities
                                    +" Modelo: "+modelo);

                        }

                    }
                    wifiScanAdapter.notifyDataSetChanged();

                }
            }
        };

        IntentFilter intentFilter= new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);

        boolean scanStarted = wifiManager.startScan();

        if(scanStarted){
            Log.d("WifiScanner", "siii");
            wifiDataList.clear();
            wifiScanAdapter.notifyDataSetChanged();
        }
        else{
            Log.d("WifiScanner", "nooo");
            wifiDataList.clear();
            wifiScanAdapter.notifyDataSetChanged();
        }

     }

     @Override
     public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         switch (requestCode) {
             case PERMISSION_REQUEST_CODE: {
                 // Verificar si el usuario concedió el permiso
                 if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                         grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                         grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                         grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                         grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                         grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                         grantResults[6] == PackageManager.PERMISSION_GRANTED) {
                     // El usuario concedió el permiso ACCESS_COARSE_LOCATION
                     // Aquí puedes realizar las operaciones que requieren el permiso, como escanear redes Wi-Fi
                     //scanWifiNetworks();
                 } else {
                     // El usuario no concedió el permiso ACCESS_COARSE_LOCATION
                     // Aquí puedes mostrar un mensaje al usuario o realizar otra acción
                     Toast.makeText(Router.this, "Es necesario permitir los permisos para un mejor funcionamiento", Toast.LENGTH_SHORT).show();
                 }
                 return;
             }
         }
     }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
           /* case R.id.editar_router:
                return true;*/
            case R.id.eliminar_router:
                eliminar_elemento_json(itemActual);
                return true;
             default:
                return false;
        }

    }

    class TaskConexion extends AsyncTask<String, String, ApiConnection>{

        private ApiConnection conn;
        private String ip;
        private int puerto;
        private String user;
        private String pass;
        private String mac;
        private String com;
        private String version;
        private String tipo;
        private boolean exito = false;

        public TaskConexion(ApiConnection conn, String ip, int puerto, String user, String pass, String mac, String com) {
            this.conn = conn;
            this.ip = ip;
            this.puerto = puerto;
            this.user = user;
            this.pass = pass;
            this.mac = mac;
            this.com = com;
            this.version = "";
            this.tipo = "";
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Conectando con router...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected ApiConnection doInBackground(String[] objects) {
            try {
               // con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip, puerto, ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(user, pass); // log in to router

                if (con.isConnected()) {
                    List<Map<String, String>> rs = con.execute("/system/routerboard/print");
                    for (Map<String,String> r : rs) {
                        mac = r.get("serial-number").toString();
                    }
                    rs = con.execute("/system/resource/print");
                    for (Map<String,String> r : rs) {
                        version = r.get("version").toString();
                        tipo = r.get("board-name").toString();
                    }
                    con.close();
                    publishProgress("xd","true");
                } else {

                    publishProgress("xd","false");
                }

            } catch (MikrotikApiException e) {
                publishProgress(e.getMessage(),"false");
            }

            return con;
        }

        @Override
        protected void onPostExecute(ApiConnection con) {
            super.onPostExecute(con);
            progress.dismiss();
            Log.d("XD= ", exito+"");
            if(exito)
                agregar_router_archivo(com,ip+" | "+tipo, PORT_USER+"", mac,user,version,pass);
            else
                Toast.makeText(Router.this,"Conexión fallida, revise los datos de conexión",Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[1].toString() != "false")
                exito = true;
        }
    }

    class TaskUsuarioSSH extends AsyncTask<String, String, List<String>>{

        private String mHost;
        private String mUsername;
        private String mPasssword;

        public TaskUsuarioSSH(String mHost, String mUsername, String mPasssword) {
            this.mHost = mHost;
            this.mUsername = mUsername;
            this.mPasssword = mPasssword;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(String... strings) {
           // MikrotikSSHClient client = new MikrotikSSHClient("192.168.1.69",22, "admin", "477426997Aa");
            //List<String> result = null;

           /* try {
                client.connect();
                if(client.getConnectionStatus().success){
                    result = client.executeCommand("/ip hotspot user profile print");
                    client.disconnect();
                   // Log.d("Wifix",result.toString());
                    if (result != null) {
                        for (String row : result) {
                            //String[] columns = row.split("\\s+");
                            //Log.d("Wifix:",row);
                        }
                    }

                }
                else{
                    Log.d("Wifix", client.getConnectionStatus().errorMessage);
                }

            } catch (JSchException | IOException e) {
                Log.d("Wifix", e.getMessage());
            }*/
/*

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null,null);
                TrustManagerFactory trustManagerFactory= TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                sslContext.init(null, trustManagerFactory.getTrustManagers(),null);
                SSLSocketFactory socketFactory= sslContext.getSocketFactory();

                ApiConnection con = ApiConnection.connect(socketFactory,"192.168.1.69", ApiConnection.DEFAULT_TLS_PORT,
                        ApiConnection.DEFAULT_CONNECTION_TIMEOUT); // connect to router
                con.login("admin", "477426997Aa"); // log in to router

                if (con.isConnected()) {
                    List<Map<String, String>> rs = con.execute("/system/routerboard/print");
                    con.close();
                    for (Map<String,String> r : rs) {
                        Log.d("Wifix",r.get("serial-number").toString());
                    }
                }else{
                    Log.d("Wifix","no");
                }

            } catch (MikrotikApiException e) {
                Log.d("Wifix",e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
*/
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
        }


    }



}