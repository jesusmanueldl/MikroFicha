package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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

public class Usuario extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private UsuariosAdapter usuariosAdapter;
    private List<UsuarioMk> usuarios;
    private UsuarioMk usuarioActual;
    private ApiConnection con;
    private ProgressDialog progress;
    private String ip = "", admin = "", pass = "",version, puerto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");

        progress = new ProgressDialog(this);
        usuarios = new ArrayList<>();
        usuariosAdapter = new UsuariosAdapter(usuarios, this, new UsuariosAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(UsuarioMk item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(UsuarioMk item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_usuario);

        //mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(usuariosAdapter);

        TaskUsuarios taskusuario = new TaskUsuarios();
        taskusuario.execute();




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(Usuario.this, Admin.class);
        i.putExtra("ip", ip);
        i.putExtra("puerto", puerto);
        i.putExtra("admin", admin);
        i.putExtra("pass", pass);
        i.putExtra("version", version);
        startActivity(i);
        finish();
    }

    private void crea_usuario(String... datos){
        usuarios.add(new UsuarioMk("Usuario: "+datos[0]+"  ", "Servidor: "+datos[1],
                "Plan: "+datos[2],"Uptime: "+datos[3],"Comentario: "+datos[4],"Contraseña: "+datos[5]+"  ",datos[6]));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_usuario,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.usuarios_activos:
                Intent i = new Intent(Usuario.this, UsuarioActivo.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void menuOptionItem(UsuarioMk item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(UsuarioMk item,View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_usuario);
        popupmenu.show();
        usuarioActual = item;
    }

    private void moveToDescription(UsuarioMk item) {
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
            case R.id.editar_usario:
                Toast.makeText(this, "Sin cambios", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.eliminar_usuario:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Usuario.this);
                builder.setMessage("¿Está seguro de eliminar este Usuario?")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TaskEliminarUsuario eliminarUsuarioActual = new TaskEliminarUsuario();
                                eliminarUsuarioActual.execute(usuarioActual.getIdus());
                                usuarios.remove(usuarioActual);
                                usuariosAdapter.notifyDataSetChanged();

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

    class TaskEliminarUsuario extends AsyncTask<String, String, Void>{
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
                con = ApiConnection.connect(SocketFactory.getDefault(), ip, Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    con.execute("/ip/hotspot/user/remove .id="+value[0]);
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

    class TaskUsuarios extends AsyncTask<Void, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Estableciendo la conexión...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            usuariosAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[7].equals("1"))
                crea_usuario(values[0],values[1],values[2],values[3],values[4],values[5],values[6]);
            else
                Toast.makeText(Usuario.this,values[0],Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean conexionPerdida = false;

            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip, Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    String server1 = "", comentario1="";
                    List<Map<String, String>> rs = con.execute("/ip/hotspot/user/print");
                    for (Map<String,String> r : rs) {
                        if(r.containsKey("server")) {
                            if (!r.get("server").isEmpty())
                                server1 = r.get("server").toString();
                            else
                                server1 = "all";
                        }else
                            server1 ="all";

                        if(r.containsKey("comment")) {
                            if (!r.get("comment").isEmpty())
                                comentario1 = r.get("comment").toString();
                            else
                                comentario1 = "Sin comentario";
                        }else
                            comentario1 ="Sin comentario";

                        if(r.containsKey("password")) {
                            if (r.containsKey("name") &&
                                    r.containsKey("profile") &&
                                    r.containsKey("uptime")
                            ) {
                                publishProgress(
                                        r.get("name").toString(),
                                        server1,
                                        r.get("profile").toString(),
                                        r.get("uptime").toString(),
                                        ";;" + comentario1,
                                        r.get("password").toString(),
                                        r.get(".id").toString(), "1");
                            }
                        }
                        else
                        {
                            if (r.containsKey("name") &&

                                    r.containsKey("profile") &&
                                    r.containsKey("uptime")
                            ) {
                                publishProgress(
                                        r.get("name").toString(),
                                        server1,
                                        r.get("profile").toString(),
                                        r.get("uptime").toString(),
                                        ";;" + comentario1,
                                        "",
                                        r.get(".id").toString(), "1");
                            }

                        }

                    }
                    con.close();
                }
                else {
                    conexionPerdida = true;
                    publishProgress("Se perdio la conexión","","","","","","","0");
                }


            } catch (MikrotikApiException e) {
                publishProgress(e.getMessage(),"","","","","","","0");
                conexionPerdida = true;
            }

            return null;
        }
    }
}