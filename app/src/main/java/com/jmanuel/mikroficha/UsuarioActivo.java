package com.jmanuel.mikroficha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

public class UsuarioActivo extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private UsuariosActivoAdapter usuariosActivoAdapter;
    private List<UsuarioActivoMk> usuariosActivo;
    private UsuarioActivoMk usuarioActivoActual;

    private ApiConnection con;
    private ProgressDialog progress;
    private String ip = "", admin = "", pass = "", version, puerto ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_activo);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version", version);

        progress = new ProgressDialog(this);

        usuariosActivo = new ArrayList<>();
        usuariosActivoAdapter = new UsuariosActivoAdapter(usuariosActivo, this, new UsuariosActivoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(UsuarioActivoMk item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(UsuarioActivoMk item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_usuario_activo);

        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(usuariosActivoAdapter);

        TaskUsuariosActivos userActivo = new TaskUsuariosActivos();
        userActivo.execute();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(UsuarioActivo.this, Admin.class);
        i.putExtra("ip", ip);
        i.putExtra("puerto", puerto);
        i.putExtra("admin", admin);
        i.putExtra("pass", pass);
        i.putExtra("version", version);
        startActivity(i);
        finish();

    }

    private void crea_usuario(String... datos){
        usuariosActivo.add(new UsuarioActivoMk("Nombre: "+datos[0], "Servidor: "+datos[1],
                "IP: "+datos[2],"Uptime: "+datos[3],datos[4]));
    }

    private void menuOptionItem(UsuarioActivoMk item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(UsuarioActivoMk item,View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_usuario_activo);
        popupmenu.show();
        usuarioActivoActual = item;
    }

    private void moveToDescription(UsuarioActivoMk item) {
       /* AlertDialog.Builder alerta = new AlertDialog.Builder(Router.this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.modal_agregar_router, null);
        alerta.setView(view);

        AlertDialog dialog = alerta.create();
        dialog.show();*/

        // startActivity(new Intent(Planes.this, Admin.class));

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){

            case R.id.eliminar_usuario_activo:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UsuarioActivo.this);
                builder.setMessage("¿Está seguro de eliminar este usuario?\nSólo se quitará de la lista de activos")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TaskEliminarUsuarioActivo eliminarUsuarioActivo = new TaskEliminarUsuarioActivo();
                                eliminarUsuarioActivo.execute(usuarioActivoActual.getIdus());
                                usuariosActivo.remove(usuarioActivoActual);
                                usuariosActivoAdapter.notifyDataSetChanged();

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

    class TaskEliminarUsuarioActivo extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Estableciendo la conexión...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
        }


        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    con.execute("/ip/hotspot/active/remove .id="+value[0]);
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

    class TaskUsuariosActivos extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            progress.setMessage("Estableciendo la conexión...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            usuariosActivoAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            crea_usuario(values[0],values[1],values[2],values[3],values[4]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip);// connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs = con.execute("/ip/hotspot/active/print");
                    for (Map<String,String> r : rs) {
                        String server1 ="";
                        if(r.containsKey("server")) {
                            if (!r.get("server").isEmpty())
                                server1 = r.get("server").toString();
                            else
                                server1 = "all";
                        }else
                            server1 ="all";

                            publishProgress(
                                    r.get("user").toString(),
                                    server1,
                                    r.get("address").toString()+" | "+ r.get("mac-address").toString(),
                                    r.get("uptime").toString(),r.get(".id").toString());
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
}