package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.SocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

public class IPBinding extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private IPBindingAdapter ipBindingAdapter;
    private List<IPBindingMk> ipBindings;
    private IPBindingMk ipBindingActual;

    private ApiConnection con;
    private ProgressDialog progress;
    String version="", ip="", admin="", pass="", puerto="";

    ArrayList<String> lista_servidores;
    ArrayAdapter<CharSequence> adapter_servidores;
    Spinner servidores;
    String spiner_server="",spiner_tipo="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipbinding);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");


        progress = new ProgressDialog(this);

        ipBindings = new ArrayList<>();
        ipBindingAdapter = new IPBindingAdapter(ipBindings, this, new IPBindingAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(IPBindingMk item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(IPBindingMk item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_binding);

        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(ipBindingAdapter);

        TaskIPBinding taskIPBinding = new TaskIPBinding();
        taskIPBinding.execute();

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(IPBinding.this, Admin.class);
        i.putExtra("ip", ip);
        i.putExtra("puerto", puerto);
        i.putExtra("admin", admin);
        i.putExtra("pass", pass);
        i.putExtra("version", version);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ipbinding,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_ipbinding:
                editar_agregar(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void moveToDescription(IPBindingMk item) {


    }

    private void menuOptionItem(IPBindingMk item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(IPBindingMk item,View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_ipbinding);
        popupmenu.show();
        ipBindingActual = item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.editar_binding:
                editar_agregar(false);
                return true;
            case R.id.habilitar_binding:
                TaskEnableIPBinding taskEnableIPBinding = new TaskEnableIPBinding();
                taskEnableIPBinding.execute(ipBindingActual.getId_binding());

                return true;
            case R.id.deshabilitar_binding:
                TaskDisableIPBinding taskDisableIPBinding = new TaskDisableIPBinding();
                taskDisableIPBinding.execute(ipBindingActual.getId_binding());

                return true;
            case R.id.eliminar_binding:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(IPBinding.this);
                builder.setMessage("¿Está seguro de eliminar esta IP Binding?")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TaskEliminarIPBinding taskEliminarIPBinding = new TaskEliminarIPBinding();
                                taskEliminarIPBinding.execute(ipBindingActual.getId_binding());

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog titulo = builder.create();
                titulo.setTitle("¡Aviso!");
                titulo.show();
                return true;
            default:
                return false;
        }

    }

    public void editar_agregar(Boolean opcion){

        AlertDialog.Builder alerta = new AlertDialog.Builder(IPBinding.this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.modal_agregar_ipbinding, null);
        alerta.setView(view);
        alerta.setCancelable(false);
        EditText mac = view.findViewById(R.id.mac_modal_ipbinding);
        EditText ip = view.findViewById(R.id.ip_modal_ipbinding);
        EditText to = view.findViewById(R.id.to_modal_ipbinding);
        EditText comentario = view.findViewById(R.id.comentario_modal_ipbinding);

        servidores = view.findViewById(R.id.servidor_spiner_ipbinding);
        Spinner tipo = view.findViewById(R.id.tipo_spiner_ipbinding);


        mac.requestFocus();

        //LLENAR SPINER DE SELECCION
        ArrayList<String> lista_tipo =  new ArrayList<String>();

        lista_tipo.add("");
        lista_tipo.add("blocked");
        lista_tipo.add("bypassed");
        lista_tipo.add("regular");
        ArrayAdapter<CharSequence> adapter_tipo = new ArrayAdapter(IPBinding.this,R.layout.spinner_disenio,lista_tipo);
        adapter_tipo.setNotifyOnChange(true);
        tipo.setAdapter(adapter_tipo);

        lista_servidores =  new ArrayList<String>();
        adapter_servidores = new ArrayAdapter(IPBinding.this,R.layout.spinner_disenio,lista_servidores);


        servidores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spiner_server = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spiner_tipo = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button btnAgregar = view.findViewById(R.id.btn_agregar_modal_ipbinding);
        Button btnCancelar = view.findViewById(R.id.btn_cancelar_modal_ipbinding);

        if(!opcion) {
            btnAgregar.setText("Editar");
            if(ipBindingActual.getMac().split("-").length >= 2 )
                mac.setText(ipBindingActual.getMac().split("-")[1]);
            if(ipBindingActual.getIp_binding().split("-").length >= 2 )
                ip.setText(ipBindingActual.getIp_binding().split("-")[1]);
            if(ipBindingActual.getTo_address().split("-").length >= 3)
                to.setText(ipBindingActual.getTo_address().split("-")[2]);
            if(ipBindingActual.getComentario().split(";;").length >= 2 )
                comentario.setText(ipBindingActual.getComentario().split(";;")[1]);
        }

        AlertDialog dialog = alerta.create();
        dialog.setCancelable(false);
        dialog.show();

        TaskCargarServidores taskserver = new TaskCargarServidores();
        taskserver.execute();
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int completo = 0;

                if (!mac.getText().toString().isEmpty()) {
                    if (!Pattern.matches(
                            "^([0-9A-Fa-f]{2}[:])" +
                                    "{5}([0-9A-Fa-f]{2})|" +
                                    "([0-9a-fA-F]{4}\\." +
                                    "[0-9a-fA-F]{4}\\." +
                                    "[0-9a-fA-F]{4})$",
                            mac.getText().toString())) {
                        Toast.makeText(IPBinding.this, "Corregir la direción mac", Toast.LENGTH_SHORT).show();
                        mac.requestFocus();
                        return;
                    } else completo++;
                }

                if (!ip.getText().toString().isEmpty()) {
                    if (!Pattern.matches(
                            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
                                    "{3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
                            ip.getText().toString())) {
                        Toast.makeText(IPBinding.this, "Corregir la direción ip", Toast.LENGTH_SHORT).show();
                        ip.requestFocus();
                        return;
                    } else {
                        completo++;
                    }
                }

                if (!to.getText().toString().isEmpty()){
                    if (!Pattern.matches(
                            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
                                    "{3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
                            to.getText().toString())) {
                        Toast.makeText(IPBinding.this, "Corregir to-address", Toast.LENGTH_SHORT).show();
                        to.requestFocus();
                        return;

                    } else {
                        completo++;
                    }
                }

                if (!spiner_server.isEmpty() && !spiner_tipo.isEmpty()) {
                    if (completo > 0) {

                        if(opcion) {

                            if(comentario.getText().toString().isEmpty())
                                comentario.setText("Power-by-MikroFicha.com");
                            TaskAgregarIPBinding taskAgregarIPBinding = new TaskAgregarIPBinding();
                            taskAgregarIPBinding.execute(mac.getText().toString(), ip.getText().toString(),
                                    to.getText().toString(), spiner_server, spiner_tipo, comentario.getText().toString().replace(" ",""));

                        }
                        else{
                                if (comentario.getText().toString().isEmpty())
                                    comentario.setText("Power-by-MikroFicha.com");
                                TaskActualizaIPBinding taskActualizaIPBinding = new TaskActualizaIPBinding();
                                taskActualizaIPBinding.execute(ipBindingActual.getId_binding(), mac.getText().toString(), ip.getText().toString(),
                                        to.getText().toString(), spiner_server, spiner_tipo, comentario.getText().toString().replace(" ", ""));

                        }

                    } else {
                        Toast.makeText(IPBinding.this, "Debe llenar al menos uno de los datos", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(IPBinding.this, "Seleccione el servidor y Tipo de condición", Toast.LENGTH_SHORT).show();

                }

                dialog.cancel();
            }
        });

    }

    private void crea_ipbinding(String... datos){
        ipBindings.add(new IPBindingMk("IP-"+datos[0],"Server-"+datos[1],
                "MAC-"+datos[2],"Type-"+datos[3],"Disabled-"+datos[4],datos[5],";;"+datos[6],"To-Address-"+datos[7]));
    }

    class TaskIPBinding extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            progress.setMessage("Estableciendo la conexión...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            ipBindings.clear();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            ipBindingAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            crea_ipbinding(values[0],values[1],values[2],values[3],values[4],values[5],values[6],values[7]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean conexionPerdida = false;
           try {
               // con = ApiConnection.connect(ip);// connect to router
               con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
               con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs = con.execute("/ip/hotspot/ip-binding/print");
                    String ip_bind ="",server = "", mac = "", type = "", estado = "", id = "", comentario = "", to_add = "";

                    for (Map<String,String> r : rs) {
                        ip_bind ="";server = ""; mac = "";type = "";
                        estado = "";id = ""; comentario = ""; to_add = "";

                        if(r.containsKey("address"))
                            ip_bind = r.get("address").toString();
                        if(r.containsKey("server")) {
                            if (!r.get("server").isEmpty())
                                server = r.get("server").toString();
                            else
                                server = "all";
                        }else
                            server = "all";

                        if(r.containsKey("mac-address"))
                            mac = r.get("mac-address").toString();
                        if(r.containsKey("type"))
                            type = r.get("type").toString();
                        if(r.containsKey(".id"))
                            id = r.get(".id").toString();
                        if(r.containsKey("comment"))
                            comentario = r.get("comment").toString();
                        if(r.containsKey("to-address"))
                            to_add = r.get("to-address").toString();
                        if(r.containsKey("disabled"))
                            estado = r.get("disabled");

                        publishProgress(
                                ip_bind,server,mac,type,estado,id,comentario,to_add);
                    }
                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                e.printStackTrace();
                conexionPerdida = true;
            }

            return null;
        }
    }

    class TaskEliminarIPBinding extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Eliminando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            TaskIPBinding taskIPBinding = new TaskIPBinding();
            taskIPBinding.execute();
        }


        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                String id = "";
                if (con.isConnected()) {
                    con.execute("/ip/hotspot/ip-binding/remove .id="+value[0]);
                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                e.printStackTrace();
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskDisableIPBinding extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Deshablitando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            TaskIPBinding taskIPBinding = new TaskIPBinding();
            taskIPBinding.execute();
        }


        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
               // con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                String id = "";
                if (con.isConnected()) {
                    con.execute("/ip/hotspot/ip-binding/disable .id="+value[0]);
                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                e.printStackTrace();
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskEnableIPBinding extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Habilitando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            TaskIPBinding taskIPBinding = new TaskIPBinding();
            taskIPBinding.execute();
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
               // con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                String id = "";
                if (con.isConnected()) {
                    con.execute("/ip/hotspot/ip-binding/enable .id="+value[0]);
                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                e.printStackTrace();
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskCargarServidores extends AsyncTask<Void, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Estableciendo la conexión...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            lista_servidores.add(0,"all");
            lista_servidores.add(0,"");
            adapter_servidores.setNotifyOnChange(true);
            servidores.setAdapter(adapter_servidores);

        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[1].equals("0")){
                lista_servidores.add(values[0]);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs = con.execute("/ip/hotspot/print");
                    for (Map<String,String> r : rs) {
                        publishProgress(r.get("name").toString(),"0");
                    }
                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                e.printStackTrace();
                conexionPerdida = true;
            }

            return null;
        }
    }

    class TaskActualizaIPBinding extends AsyncTask<String, String, Void>{

        @Override
        protected void onPreExecute() {
            progress.setMessage("Actualizando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            TaskIPBinding taskIPBinding = new TaskIPBinding();
            taskIPBinding.execute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[0].equals("-1"))
                Toast.makeText(IPBinding.this, values[1], Toast.LENGTH_LONG).show();
            else
                Toast.makeText(IPBinding.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;

            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs = null;

                    if(!value[1].isEmpty() && value[2].isEmpty() && value[3].isEmpty() && value[6].isEmpty())
                       rs = con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                "address=0.0.0.0 to-address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(value[1].isEmpty() && !value[2].isEmpty() && value[3].isEmpty() && value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address=00:00:00:00:00:00 address="+value[2]+
                                " to-address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(value[1].isEmpty() && value[2].isEmpty() && !value[3].isEmpty() && value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address=00:00:00:00:00:00 to-address="+value[3]+
                                " address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && value[2].isEmpty() && !value[3].isEmpty() && value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                 "address=0.0.0.0 to-address="+value[3]+" server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && value[2].isEmpty() && value[3].isEmpty() && !value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                " address=0.0.0.0 to-address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && !value[2].isEmpty() && value[3].isEmpty() && value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                " address="+value[2]+" to-address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(value[1].isEmpty() && !value[2].isEmpty() && !value[3].isEmpty() && value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+
                                " mac-address=00:00:00:00:00:00 address="+value[2]+" to-address="+value[3]+" server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(value[1].isEmpty() && value[2].isEmpty() && !value[3].isEmpty() && !value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address=00:00:00:00:00:00 address=0.0.0.0 to-address="+value[3]+
                                " server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(value[1].isEmpty() && !value[2].isEmpty() && value[3].isEmpty() && !value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+
                                " mac-address=00:00:00:00:00:00 address="+value[2]+" to-address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(value[1].isEmpty() && !value[2].isEmpty() && !value[3].isEmpty() && !value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+
                                " mac-address=00:00:00:00:00:00 address="+value[2]+" to-address="+value[3]+" server="+value[4]+
                                " type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && value[2].isEmpty() && !value[3].isEmpty() && !value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                " address=0.0.0.0 to-address="+value[3]+" server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && !value[2].isEmpty() && value[3].isEmpty() && !value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                " address="+value[2]+" to-address=0.0.0.0 server="+value[4]+" type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && !value[2].isEmpty() && !value[3].isEmpty() && value[6].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                                " address="+value[2]+" to-address="+value[3]+" server="+value[4]+
                                " type="+value[5]+" comment="+value[6]);
                    else if(!value[1].isEmpty() && !value[2].isEmpty() && !value[3].isEmpty() && !value[6].isEmpty())
                        rs = con.execute("/ip/hotspot/ip-binding/set .id="+value[0]+" mac-address="+value[1]+
                            " address="+value[2]+" to-address="+value[3]+" server="+value[4]+
                           " type="+value[5]+" comment="+value[6]);

                    con.close();
                    publishProgress("0","IP Binding actualizada");
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                publishProgress("-1",e.getMessage());
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskAgregarIPBinding extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Agregando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            TaskIPBinding taskIPBinding = new TaskIPBinding();
            taskIPBinding.execute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[0].equals("-1"))
                Toast.makeText(IPBinding.this, values[1], Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(IPBinding.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;

            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);

                if (con.isConnected()) {
                    List<Map<String, String>> rs = null;

                    if(!value[0].isEmpty() && value[1].isEmpty() && value[2].isEmpty() && value[5].isEmpty())
                        rs = con.execute("/ip/hotspot/ip-binding/add mac-address="+value[0]+
                                "address=0.0.0.0 to-address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[6]);
                    else if(value[0].isEmpty() && !value[1].isEmpty() && value[2].isEmpty() && value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address=00:00:00:00:00:00 address="+value[1]+
                                " to-address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(value[0].isEmpty() && value[1].isEmpty() && !value[2].isEmpty() && value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address=00:00:00:00:00:00 to-address="+value[2]+
                                " address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && value[1].isEmpty() && !value[2].isEmpty() && value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address="+value[0]+
                                "address=0.0.0.0 to-address="+value[2]+" server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && value[1].isEmpty() && value[2].isEmpty() && !value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address="+value[0]+
                                " address=0.0.0.0 to-address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && !value[1].isEmpty() && value[2].isEmpty() && value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address="+value[0]+
                                " address="+value[1]+" to-address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(value[0].isEmpty() && !value[1].isEmpty() && !value[2].isEmpty() && value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address=00:00:00:00:00:00 address="+value[1]+
                                " to-address="+value[2]+" server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(value[0].isEmpty() && value[1].isEmpty() && !value[2].isEmpty() && !value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address=00:00:00:00:00:00 address=0.0.0.0 to-address="+value[2]+
                                " server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(value[0].isEmpty() && !value[1].isEmpty() && value[2].isEmpty() && !value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address=00:00:00:00:00:00 address="+value[1]+
                                " to-address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(value[0].isEmpty() && !value[1].isEmpty() && !value[2].isEmpty() && !value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address=00:00:00:00:00:00 address="+value[1]+" to-address="+value[2]+" server="+value[3]+
                                " type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && value[1].isEmpty() && !value[2].isEmpty() && !value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address="+value[0]+
                                " address=0.0.0.0 to-address="+value[2]+" server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && !value[1].isEmpty() && value[2].isEmpty() && !value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address="+value[0]+
                                " address="+value[1]+" to-address=0.0.0.0 server="+value[3]+" type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && !value[1].isEmpty() && !value[2].isEmpty() && value[5].isEmpty())
                        rs =con.execute("/ip/hotspot/ip-binding/add mac-address="+value[1]+
                                " address="+value[1]+" to-address="+value[2]+" server="+value[3]+
                                " type="+value[4]+" comment="+value[5]);
                    else if(!value[0].isEmpty() && !value[1].isEmpty() && !value[2].isEmpty() && !value[5].isEmpty())
                        rs = con.execute("/ip/hotspot/ip-binding/add mac-address="+value[1]+
                                " address="+value[1]+" to-address="+value[2]+" server="+value[3]+
                                " type="+value[4]+" comment="+value[5]);

                    con.close();
                    publishProgress("0","Se agregó una IP Binding");
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                publishProgress("-1",e.getMessage());
                conexionPerdida = true;
            }
            return null;
        }
    }


}