package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

public class ScriptAdminHotspot extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private ApiConnection con;
    private String ip,admin,pass,version,puerto;
    private ScriptHotspotAdapter scriptHotspotAdapter;
    private List<ScriptHotspot> scriptHotspotsOnline;
    private ScriptHotspot scriptHotspotActual;
    private ProgressDialog progress;
    private Button btnAgregar, btnCancelar;

    private String titulo_script="", info_script="", cmd_script="";

    private DatabaseReference mDatabase;
    SharedPreferences admob_preference;
    SharedPreferences.Editor editor;
    String uuid_app;
    private boolean SUB = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_admin_hotspot);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");

        admob_preference = this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        uuid_app = admob_preference.getString("uuid_app","N/A");

        progress = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(uuid_app).exists() ) {
                    SUB = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        scriptHotspotsOnline = new ArrayList<>();
        scriptHotspotAdapter = new ScriptHotspotAdapter(scriptHotspotsOnline, this, new ScriptHotspotAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(ScriptHotspot item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(ScriptHotspot item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_script);

        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(scriptHotspotAdapter);

        mDatabase.child("SCRIPT_LIST_HOTSPOT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scriptHotspotsOnline.clear();
                for(DataSnapshot sn: snapshot.getChildren()){
                    if(sn.child("INFO").exists() && sn.child("SCRIPT_CMD").exists()
                            && sn.child("SUB").exists() && sn.child("TITULO").exists()) {
                        scriptHotspotsOnline.add(new ScriptHotspot(sn.child("INFO").getValue().toString(),
                                sn.child("SCRIPT_CMD").getValue().toString(), (boolean) sn.child("SUB").getValue(),
                                sn.child("TITULO").getValue().toString()));
                    }
                    //Log.d("url", sn.child("URL").getValue().toString());
                }
                scriptHotspotAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ScriptAdminHotspot.this, Admin.class);
        i.putExtra("ip", ip);
        i.putExtra("puerto", puerto);
        i.putExtra("admin", admin);
        i.putExtra("pass", pass);
        i.putExtra("version", version);
        startActivity(i);
        finish();
    }
    private void menuOptionItem(ScriptHotspot item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(ScriptHotspot item,View view){
       /* PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_usuario_activo);
        popupmenu.show();*/
        scriptHotspotActual = item;
    }

    private void moveToDescription(ScriptHotspot item) {

        if(item.getSubs() && SUB){
            ejecuta_script(item);
        }
        else if(item.getSubs() && !SUB){
            Toast.makeText(this,"Este script requiere una suscripción activa",Toast.LENGTH_SHORT).show();
        }
        else if(!item.getSubs()){
            ejecuta_script(item);
        }
    }

    private void ejecuta_script(ScriptHotspot item){
        scriptHotspotActual = item;
        titulo_script=item.getTitulo();
        info_script=item.getInfo();
        cmd_script=item.getScript_cmd();
        //este metodo para cuando se haga click sobre un elemnto del recyclerview
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.modal_script_hotspot_cmd, null);
        alerta.setView(view);

        TextView titulo = view.findViewById(R.id.titulo_modal_script_router);
        TextView info = view.findViewById(R.id.info_modal_script_router);
        //TextInputEditText cmd = view.findViewById(R.id.cmd_modal_script_router);
        EditText cmd = view.findViewById(R.id.cmd_modal_script_router);

        btnAgregar = view.findViewById(R.id.btn_agregar_modal_script_hotspot);
        btnCancelar = view.findViewById(R.id.btn_cancelar_modal_script_hotspot);

        AlertDialog dialog = alerta.create();
        dialog.setCancelable(false);
        dialog.show();

        cmd.requestFocus();

        titulo.setText(titulo_script);
        info.setText(info_script);
        cmd.setText(cmd_script);
        cmd.setSelection(cmd.getText().length(),cmd.getText().length());

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ScriptAdminHotspot.this);
                builder.setMessage("¿Está seguro que desea ejecutar el Script?")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TaskExecuteCMD taskExecuteCMD = new TaskExecuteCMD();
                                taskExecuteCMD.execute(titulo.getText().toString(),cmd.getText().toString());

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog titulox = builder.create();
                titulox.setTitle("¡Aviso!");
                titulox.show();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    class TaskExecuteCMD extends AsyncTask<String, String, Void> {
        String msj = "";
        boolean done = false;
        String tit = "";
        @Override
        protected void onPreExecute() {
            progress.setMessage("Ejecutando Script espere...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            if(done){
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ScriptAdminHotspot.this);
                builder.setMessage("Script ejecutado con éxito y guardado en: /system/script/"+tit)
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                btnAgregar.setVisibility(View.GONE);
                                btnCancelar.setText("Salir");
                                dialogInterface.dismiss();
                            }
                        });
                androidx.appcompat.app.AlertDialog titulox = builder.create();
                titulox.setTitle("¡Genial!");
                titulox.show();
            }
            else{
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ScriptAdminHotspot.this);
                builder.setMessage("Verifica nuevamente y edita el script si es necesario.\n\n[!]: "+msj)
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                androidx.appcompat.app.AlertDialog titulox = builder.create();
                titulox.setTitle("¡No fue posible ejecutar el script!");
                titulox.show();
            }
        }

        @Override
        protected Void doInBackground(String... val) {
            String nombreScript ="";
            String idScrip = "";
            Boolean conexionPerdida = false;
            try {
               // con = ApiConnection.connect(ip);// connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    tit = val[0];
                    List<Map<String, String>> rs = con.execute("/system/script/print");
                    for (Map<String, String> r : rs) {
                        nombreScript = r.get("name").toString();
                        idScrip =  r.get(".id").toString();
                        Log.d("DD",nombreScript+"<<"+val[0]+">>"+idScrip);
                        if(nombreScript.equals(val[0])) {
                            con.execute("/system/script/remove .id=" + idScrip);
                            break;
                        }
                    }
                    con.execute("/system/script/add name="+val[0]+" source='"+val[1]+"'");
                    con.execute("/system/script/run .id="+val[0]);
                    done = true;

                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                e.printStackTrace();
                done = false;
                msj = e.getMessage();
                conexionPerdida = true;
            }

            return null;
        }
    }
}