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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.SocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

public class Planes extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener  {
    private PlanesAdapter planesAdapter;
    private List<PlanesMk> planes;
    private PlanesMk planActual;

    private ApiConnection con;
    private ProgressDialog progress;
    public String ip,admin,pass,version,puerto;
    ArrayList<String> listaServidores;
    ArrayAdapter<CharSequence> adapterSevidor;
    //Spinner servidores;
    public String diaa="0",horaa="00",minn="00",segg="00",tipoTiempo="-";
    TaskPerfiles taskperfil;
    //LIMITES
    int LIMITE_PLANES =1; //1 esta en linea en la app quitar
    private DatabaseReference mDatabase;
    SharedPreferences admob_preference;
    SharedPreferences.Editor editor;
    String uuid_app;
    private boolean SCRIPT_FALLA = false;
    String perfilAnteriorNombre ="",perfilAnteriorPrecio = "",perfilAnteriorTiempo="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planes);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        admob_preference = Planes.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        uuid_app = admob_preference.getString("uuid_app","N/A");

        progress = new ProgressDialog(this);
        listaServidores =  new ArrayList<String>();
        adapterSevidor = new ArrayAdapter(this,android.R.layout.simple_spinner_item,listaServidores);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");

        planes = new ArrayList<>();
        planesAdapter = new PlanesAdapter(planes, this, new PlanesAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(PlanesMk item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(PlanesMk item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_planes);

        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(planesAdapter);

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).exists() && snapshot.child(uuid_app).child("SCRIPTFALLA").exists()) {
                        SCRIPT_FALLA = (boolean) snapshot.child(uuid_app).child("SCRIPTFALLA").getValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        numeroPlanes(); //esta en linea

        taskperfil = new TaskPerfiles();
        taskperfil.execute();

    }

    public void numeroPlanes(){
        long interval = 3600;
        if(BuildConfig.DEBUG)
            interval = 5;

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings frconf = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(frconf);
        HashMap<String,Object> actualizacion = new HashMap<>();
        actualizacion.put("limiteplanes",LIMITE_PLANES);

        remoteConfig.setDefaultsAsync(actualizacion);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(Planes.this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        cargaLimitePlanes();
                    }
                });
    }

    private void cargaLimitePlanes() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        LIMITE_PLANES = Integer.parseInt(remoteConfig.getString("limiteplanes"));

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).exists() && snapshot.child(uuid_app).child("MAX_PLANES").exists()) {
                        LIMITE_PLANES = Integer.parseInt(snapshot.child(uuid_app).child("MAX_PLANES").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void crear_perfil(String... values){
        if(!values[5].equals("0")) {
            if (values[0].toString().contains(":")) {
                planes.add(new PlanesMk(values[0].toString().split(":")[0],
                        "$ " + values[0].toString().split(":")[1],
                        "Usuarios: " + values[1]+ " por plan",
                        "Vel: " + values[2] ,
                        "Tiempo: " + values[3],
                        values[4]));

            }
            else
            {
                planes.add(new PlanesMk(values[0],
                        "$ Actualizar-precio",
                        "Ususarios: " + values[1]+ " por plan",
                        "Vel: " + values[2] ,
                        "Tiempo: " + values[3],
                        values[4]));
            }
        }
        else {
            Toast.makeText(this,"Se perdio la conexión, intente de nuevo", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(Planes.this, Admin.class);
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
        getMenuInflater().inflate(R.menu.menu_planes,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_plan:
                if(planes.size() < LIMITE_PLANES) {
                    editar_agregar(true); //agregamos true
                }
                else{
                    Toast.makeText(Planes.this,"Límite máximo de Planes: "+LIMITE_PLANES,Toast.LENGTH_LONG).show();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void menuOptionItem(PlanesMk item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(PlanesMk item,View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_planes);
        popupmenu.show();
        planActual = item;
    }

    private void moveToDescription(PlanesMk item) {
       /* AlertDialog.Builder alerta = new AlertDialog.Builder(Router.this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.modal_agregar_router, null);
        alerta.setView(view);

        AlertDialog dialog = alerta.create();
        dialog.show();*/

       // startActivity(new Intent(Planes.this, Admin.class));

    }

    public void editar_agregar(Boolean vall){

            AlertDialog.Builder alerta = new AlertDialog.Builder(Planes.this);
            LayoutInflater inflater = getLayoutInflater();

            View view = inflater.inflate(R.layout.modal_agregar_plan, null);
            alerta.setView(view);
            alerta.setCancelable(false);
            EditText nombrePlan = view.findViewById(R.id.nombre_plan);
            //servidores = view.findViewById(R.id.espiner_servidor);
            EditText precioPlan = view.findViewById(R.id.costo_plan);
            EditText usuarioPorPlan = view.findViewById(R.id.usuario_en_plan);
            EditText velocidadPlan = view.findViewById(R.id.velocidad_plan);

            Spinner dia = view.findViewById(R.id.dia_perfil);
            Spinner hora = view.findViewById(R.id.hora_perfil);
            Spinner minuto = view.findViewById(R.id.minuto_perfil);
            Spinner segundo = view.findViewById(R.id.segundo_perfil);

            RadioGroup radiogrupo = view.findViewById(R.id.opciones_grupo_radio);
            radiogrupo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    switch (i){
                        case R.id.tiempo_pausado:
                            tipoTiempo = "00:05:00";
                            break;
                        case R.id.tiempo_corrido:
                            tipoTiempo = String.format("%sd%s:%s:%s", diaa, horaa, minn, segg);
                            break;
                    }
                }
            });

            nombrePlan.requestFocus();

            //LLENAR SPINER DE SELECCION
            ArrayList<String> lista_dia =  new ArrayList<String>();

            for(int i = 0; i < 366; i++)
               lista_dia.add(i+"");
            ArrayAdapter<CharSequence> adapter_dia = new ArrayAdapter(this,android.R.layout.simple_spinner_item,lista_dia);
            adapter_dia.setNotifyOnChange(true);
            dia.setAdapter(adapter_dia);

            ArrayList<String> lista =  new ArrayList<String>();
            for(int i = 0; i < 60; i++) {
                if (i < 10)
                    lista.add("0" + i);
                else
                    lista.add(i + "");
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lista);
            adapter.setNotifyOnChange(true);
            minuto.setAdapter(adapter);
            segundo.setAdapter(adapter);

            //LLENAR SPINER DE SELECCION
            ArrayList<String> lista_hora =  new ArrayList<String>();
            for(int i = 0; i < 25; i++)
                if (i < 10)
                    lista_hora.add("0" + i);
                else
                    lista_hora.add(i + "");
            ArrayAdapter<CharSequence> adapter_hora = new ArrayAdapter(this,android.R.layout.simple_spinner_item,lista_hora);
            adapter_hora.setNotifyOnChange(true);
            hora.setAdapter(adapter_hora);

            RadioButton tiempoCorrido = view.findViewById(R.id.tiempo_corrido);
            RadioButton tiempoPausado = view.findViewById(R.id.tiempo_pausado);


            Button btnAgregar = view.findViewById(R.id.btn_agregar_plan);
            Button btnCancelar = view.findViewById(R.id.btn_cancelar_plan);

            AlertDialog dialog = alerta.create();
            dialog.setCancelable(false);
            dialog.show();


            dia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    diaa = adapterView.getItemAtPosition(i).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            hora.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    horaa = adapterView.getItemAtPosition(i).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            minuto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    minn = adapterView.getItemAtPosition(i).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            segundo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    segg = adapterView.getItemAtPosition(i).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(!vall){
                btnAgregar.setText("Editar");
                nombrePlan.setText(planActual.getNombre());
                perfilAnteriorNombre = planActual.getNombre();
                perfilAnteriorPrecio = planActual.getCosto().split(" ")[1];
                if(planActual.getDuracionFicha().length() > 8)
                    perfilAnteriorTiempo = planActual.getDuracionFicha().split(" ")[1];
                else
                    perfilAnteriorTiempo = "0d00:00:00";
                precioPlan.setText(planActual.getCosto().split(" ")[1]);
                usuarioPorPlan.setText(planActual.getNumerosDeUsuarios().split(" ")[1]);
                velocidadPlan.setText(planActual.getVelocidad().split(" ")[1]);
            }


            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });

            btnAgregar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (vall){
                        btnAgregar.setText("Agregar");
                        String tpficha;
                        tpficha = String.format("%sd%s:%s:%s", diaa, horaa, minn, segg);
                        if (!nombrePlan.getText().toString().isEmpty() &&
                                !precioPlan.getText().toString().isEmpty() &&
                                !usuarioPorPlan.getText().toString().isEmpty() &&
                                !velocidadPlan.getText().toString().isEmpty() &&
                                !tpficha.equals("0d00:00:00")
                        ) {
                            //^[0-9]+[^MmKk]{1}
                            if (Pattern.matches("^[0-9]+(M|m|K|k)\\/[0-9]+(M|m|K|k)$",
                                    velocidadPlan.getText().toString())) {
                                if(tipoTiempo.equals("-"))
                                    tipoTiempo = tpficha;
                                            TaskaAgregarPerfil tagp = new TaskaAgregarPerfil();
                                tagp.execute(nombrePlan.getText().toString() + ":" + precioPlan.getText().toString(),
                                        usuarioPorPlan.getText().toString(), velocidadPlan.getText().toString(), tpficha, tipoTiempo);
                                dialog.dismiss();
                                planesAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(Planes.this, "Corregir velocidad, ejemplo: 2M/2M ó 512K/2M...", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(Planes.this, "Llene todos los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        String tpficha;
                        tpficha = String.format("%sd%s:%s:%s", diaa, horaa, minn, segg);
                        if (!nombrePlan.getText().toString().isEmpty() &&
                                !precioPlan.getText().toString().isEmpty() &&
                                !usuarioPorPlan.getText().toString().isEmpty() &&
                                !velocidadPlan.getText().toString().isEmpty() &&
                                !tpficha.equals("0d00:00:00")
                        ) {
                            //^[0-9]+[^MmKk]{1}
                            if (Pattern.matches("^[0-9]+(M|m|K|k)\\/[0-9]+(M|m|K|k)$",
                                    velocidadPlan.getText().toString())) {
                                if(tipoTiempo.equals("-"))
                                    tipoTiempo = tpficha;
                                TaskActualizaPerfil actualizarPerfil = new TaskActualizaPerfil();
                                actualizarPerfil.execute(nombrePlan.getText().toString() + ":" + precioPlan.getText().toString(),
                                        usuarioPorPlan.getText().toString(), velocidadPlan.getText().toString(), tpficha, tipoTiempo,planActual.getIdMikro());
                                dialog.dismiss();
                            } else {
                                Toast.makeText(Planes.this, "Corregir velocidad, ejemplo: 2M/2M ó 512K/2M...", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(Planes.this, "Llene todos los datos", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.editar_plan:
                editar_agregar(false); //editamos es false
                return true;
            case R.id.script_falla_electrica:
                androidx.appcompat.app.AlertDialog.Builder builder_falla = new androidx.appcompat.app.AlertDialog.Builder(Planes.this);
                builder_falla.setMessage("Esta opción es * únicamente * para planes de tiempo corrido\n\n"+
                                "* Recuerda tener la hora del Router al día\n\n¿Está seguro que desea continuar?")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(SCRIPT_FALLA) {
                                    TaskaScriptFallaElectrica scriptFallaElectrica = new TaskaScriptFallaElectrica();
                                    scriptFallaElectrica.execute();
                                }else{
                                    Toast.makeText(Planes.this,"Opción sólo para usuarios con suscripción Anual", Toast.LENGTH_LONG).show();
                                }

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog titulo_falla = builder_falla.create();
                titulo_falla.setTitle("¡Ejecutará un Script!");
                titulo_falla.show();
                return true;
            case R.id.eliminar_plan:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Planes.this);
                builder.setMessage("¿Está seguro de eliminar este Plan?")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TaskEliminarPerfil eliminarPerfilActual = new TaskEliminarPerfil();
                                eliminarPerfilActual.execute(planActual.getIdMikro(),planActual.getNombre()+":"+planActual.getCosto().split(" ")[1]);
                                planes.remove(planActual);
                                planesAdapter.notifyDataSetChanged();

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

    class TaskPerfiles extends AsyncTask<Void, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Cargando perfiles...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            planes.clear();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            planesAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            crear_perfil(values[0],values[1],values[2],values[3],values[4],values[5]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs = con.execute("/ip/hotspot/user/profile/print");
                    for (Map<String,String> r : rs) {
                        if(r.containsKey("session-timeout")) {
                            if (r.containsKey("name") &&
                                    r.containsKey("shared-users") &&
                                    r.containsKey("rate-limit") &&
                                    r.containsKey("session-timeout")) {
                                publishProgress(
                                        r.get("name").toString(),
                                        r.get("shared-users").toString(),
                                        r.get("rate-limit").toString(),
                                        r.get("session-timeout").toString(),
                                        r.get(".id").toString(), "1"
                                );

                            }
                        }
                        else{
                            if (r.containsKey("name") &&
                                    r.containsKey("shared-users") &&
                                    r.containsKey("rate-limit")
                                    ) {
                                publishProgress(
                                        r.get("name").toString(),
                                        r.get("shared-users").toString(),
                                        r.get("rate-limit").toString(),
                                        "",
                                        r.get(".id").toString(), "1"
                                );

                            }
                        }
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

            if(conexionPerdida)
                publishProgress("0","0","0","0","0","0","0");

            return null;
        }
    }

    class TaskEliminarPerfil extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Eliminando...");
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
                String id = "";
                if (con.isConnected()) {
                    con.execute("/ip/hotspot/user/profile/remove .id="+value[0]);

                    List<Map<String, String>> rs;
                    rs = con.execute("/system/scheduler/print");
                    for (Map<String,String> r : rs) {
                        if(r.get("name").equals("Elimina-Tiempo-de-"+value[1])){
                            id = r.get(".id");
                            con.execute("/system/scheduler/remove .id="+id);
                            break;
                        }
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

    class TaskActualizaPerfil extends AsyncTask<String, String, Void>{

        String date = "";
        String fecha, hora;
        boolean  ya_esta_eschu = false;
        @Override
        protected void onPreExecute() {
            progress.setMessage("Actualizando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyy HH:mm:ss");
            date = dateFormat.format(new Date());
            fecha = quemeses(date.toString().split(" ")[0].split("/")[0])+"/"+
                    date.toString().split(" ")[0].split("/")[1]+"/"+
                    date.toString().split(" ")[0].split("/")[2];
            hora = date.toString().split(" ")[1];

        }

        public String quemeses(String mes){
            String actual ="";
            switch (mes){
                case "01":
                    actual = "Jun";
                    break;
                case "02":
                    actual = "Feb";
                    break;
                case "03":
                    actual = "Mar";
                    break;
                case "04":
                    actual = "Apr";
                    break;
                case "05":
                    actual = "May";
                    break;
                case "06":
                    actual = "Jun";
                    break;
                case "07":
                    actual = "Jul";
                    break;
                case "08":
                    actual = "Ago";
                    break;
                case "09":
                    actual = "Sep";
                    break;
                case "10":
                    actual = "Oct";
                    break;
                case "11":
                    actual = "Nov";
                    break;
                case "12":
                    actual = "Dec";
                    break;
            }
            return actual;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            tipoTiempo = "-";
            taskperfil = new TaskPerfiles();
            taskperfil.execute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(Planes.this, values[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;

            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs;

                    rs = con.execute("/ip/hotspot/user/profile/set .id="+value[5]+" name="+value[0]+
                            " shared-users="+value[1].toString()+" rate-limit="+value[2].toString()+
                            " session-timeout="+value[3].toString()+" idle-timeout="+value[4].toString()+" transparent-proxy=yes");

                    rs = con.execute("/system/scheduler/print");
                    for (Map<String,String> r : rs) {
                        if(r.get("name").equals("Elimina-Tiempo-de"+"-"+perfilAnteriorNombre+":"+perfilAnteriorPrecio)){
                            con.execute("/system/scheduler/set .id="+r.get(".id")+" name=Elimina-Tiempo-de-"+value[0]+
                                    " start-date="+fecha+" start-time="+hora+" interval=01:00:00 comment=MikroFicha-Elimina-Tiempo-de-"+value[0]+
                                    " on-event=\"/ip hotspot user remove [find uptime="+value[3]+" limit-uptime="+value[3]+"]\"");
                            ya_esta_eschu = true;
                            break;
                        }
                    }
                    if(!ya_esta_eschu) {
                        con.execute("/system/scheduler/add name=Elimina-Tiempo-de-"+value[0]+
                                " start-date=" + fecha + " start-time=" + hora + " interval=01:00:00 comment=MikroFicha-Elimina-Tiempo-de-"+value[0]+
                                " on-event=\"/ip hotspot user remove [find uptime=" + value[3] + " limit-uptime=" + value[3] + "]\"");
                    }
                    ya_esta_eschu = false;

                    rs = con.execute("/ip/hotspot/user/print");
                    String id_user ="";
                    for (Map<String,String> r : rs) {
                        if(r.containsKey("profile")) {
                            if (r.get("profile").equals(value[0])) {
                                id_user = r.get(".id");
                                con.execute("/ip/hotspot/user/set .id=" + id_user + " limit-uptime=" + value[3]);
                            }
                        }
                    }


                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                publishProgress(e.getMessage()+"Elimina-Tiempo-de-"+perfilAnteriorTiempo+"-"+perfilAnteriorNombre+":"+perfilAnteriorPrecio);
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskaAgregarPerfil extends AsyncTask<String, String, Void>{

        String date = "";
        String fecha, hora;
        boolean ya_esta_eschu = false;
        @Override
        protected void onPreExecute() {
            progress.setMessage("Nuevo perfil en curso...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyy HH:mm:ss");
            date = dateFormat.format(new Date());
            fecha = quemeses(date.toString().split(" ")[0].split("/")[0])+"/"+
                    date.toString().split(" ")[0].split("/")[1]+"/"+
                    date.toString().split(" ")[0].split("/")[2];
            hora = date.toString().split(" ")[1];
        }

        public String quemeses(String mes){
            String actual ="";
            switch (mes){
                 case "01":
                    actual = "Jun";
                 break;
                case "02":
                    actual = "Feb";
                break;
                case "03":
                    actual = "Mar";
                break;
                case "04":
                    actual = "Apr";
                break;
                case "05":
                    actual = "May";
                break;
                case "06":
                    actual = "Jun";
                break;
                case "07":
                    actual = "Jul";
                break;
                case "08":
                    actual = "Ago";
                break;
                case "09":
                    actual = "Sep";
                break;
                case "10":
                    actual = "Oct";
                break;
                case "11":
                    actual = "Nov";
                break;
                case "12":
                    actual = "Dec";
                break;
            }
            return actual;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            taskperfil = new TaskPerfiles();
            taskperfil.execute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(Planes.this, values[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;

            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    List<Map<String, String>> rs;

                    rs = con.execute("/ip/hotspot/user/profile/add name="+value[0]+
                            " shared-users="+value[1].toString()+" rate-limit="+value[2].toString()+
                            " session-timeout="+value[3].toString()+" idle-timeout="+value[4].toString()+" transparent-proxy=yes");

                  /*  rs = con.execute("/system/scheduler/print");
                    for (Map<String,String> r : rs) {
                        if(r.get("name").equals("Elimina-Tiempo-de-"+value[3]+"-"+value[0])){
                            con.execute("/system/scheduler/set .id="+r.get(".id")+" name=Elimina-Tiempo-de-"+value[3]+"-"+value[0]+
                                    " start-date="+fecha+" start-time="+hora+" interval=01:00:00 comment=MikroFicha-Elimina-Ficha-Tiempo-"+value[3]+"-"+value[0]+
                                    " on-event=\"/ip hotspot user remove [find uptime="+value[3]+" limit-uptime="+value[3]+"]\"");
                            ya_esta_eschu = true;
                            break;
                        }
                    }
                    if(!ya_esta_eschu) {*/
                        con.execute("/system/scheduler/add name=Elimina-Tiempo-de-"+value[0]+
                                " start-date="+fecha+" start-time="+hora+" interval=01:00:00 comment=MikroFicha-Elimina-Ficha-Tiempo-"+value[0]+
                                " on-event=\"/ip hotspot user remove [find uptime="+value[3]+" limit-uptime="+value[3]+"]\"");
                   // }

                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                publishProgress(e.getMessage());
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskaScriptFallaElectrica extends AsyncTask<String, String, Void>{

        String date = "";
        String fecha, hora;
        String dia = "0";
        String duracion = "0";
        @Override
        protected void onPreExecute() {
            progress.setMessage("Ejecutando falla eléctrica...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyy HH:mm:ss");
            date = dateFormat.format(new Date());
            fecha = quemeses(date.toString().split(" ")[0].split("/")[0]) + "/" +
                    date.toString().split(" ")[0].split("/")[1] + "/" +
                    date.toString().split(" ")[0].split("/")[2];
            hora = date.toString().split(" ")[1];

            int dIa = 0;

            if(planActual.getDuracionFicha().length() > 8){
                if(planActual.getDuracionFicha().contains("w")){
                    dIa += Integer.parseInt(planActual.getDuracionFicha().split(" ")[1].split("w")[0]);
                    dIa *= 7;
                }

                if (planActual.getDuracionFicha().contains("d") && planActual.getDuracionFicha().contains("w")) {
                    dIa += Integer.parseInt(planActual.getDuracionFicha().split(" ")[1].split("w")[1].split("d")[0]);
                }

                if (planActual.getDuracionFicha().contains("d") && !planActual.getDuracionFicha().contains("w")) {
                    dIa += Integer.parseInt(planActual.getDuracionFicha().split(" ")[1].split("d")[0]);
                }

                dia = dIa+"";

                if (planActual.getDuracionFicha().contains("h") &&
                        planActual.getDuracionFicha().contains("w") &&
                        planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("w")[1].split("d")[1];
                }
                else if (planActual.getDuracionFicha().contains("h") &&
                        !planActual.getDuracionFicha().contains("w") &&
                        !planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1];

                }else if(planActual.getDuracionFicha().contains("h") &&
                        planActual.getDuracionFicha().contains("w")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("w")[1];
                }
                else if(planActual.getDuracionFicha().contains("h") &&
                        planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("d")[1];

                }
                else if(planActual.getDuracionFicha().split(" ")[1].contains("m") &&
                        planActual.getDuracionFicha().contains("w") &&
                        planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("w")[1].split("d")[1];

                }
                else if(planActual.getDuracionFicha().split(" ")[1].contains("m") &&
                        !planActual.getDuracionFicha().contains("w") &&
                        !planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1];

                }else if(planActual.getDuracionFicha().split(" ")[1].contains("m") &&
                        planActual.getDuracionFicha().contains("w")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("w")[1];

                }else if(planActual.getDuracionFicha().split(" ")[1].contains("m") &&
                        planActual.getDuracionFicha().contains("d")){
                    Log.d("d",planActual.getDuracionFicha().split(" ")[1].contains("m")+"");

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("d")[1];

                }else if(planActual.getDuracionFicha().contains("s") &&
                        planActual.getDuracionFicha().contains("w") &&
                        planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("w")[1].split("d")[1];

                }else if(planActual.getDuracionFicha().contains("s") &&
                        !planActual.getDuracionFicha().contains("w") &&
                        !planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1];

                }else if(planActual.getDuracionFicha().contains("s") &&
                        planActual.getDuracionFicha().contains("w")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("w")[1];
                }
                else if(planActual.getDuracionFicha().contains("s") &&
                        planActual.getDuracionFicha().contains("d")){

                    duracion = planActual.getDuracionFicha().split(" ")[1].split("d")[1];

                }
                else{
                    duracion = "00:00:00";
                }

            }else{
                progress.dismiss();
                Toast.makeText(Planes.this,"¡Este plan no tiene tiempo definido!", Toast.LENGTH_LONG).show();
                this.cancel(true);
            }

        }

        public String quemeses(String mes){
            String actual ="";
            switch (mes){
                case "01":
                    actual = "jun";
                    break;
                case "02":
                    actual = "feb";
                    break;
                case "03":
                    actual = "mar";
                    break;
                case "04":
                    actual = "apr";
                    break;
                case "05":
                    actual = "may";
                    break;
                case "06":
                    actual = "jun";
                    break;
                case "07":
                    actual = "jul";
                    break;
                case "08":
                    actual = "ago";
                    break;
                case "09":
                    actual = "sep";
                    break;
                case "10":
                    actual = "oct";
                    break;
                case "11":
                    actual = "nov";
                    break;
                case "12":
                    actual = "dec";
                    break;
            }
            return actual;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            Toast.makeText(Planes.this,"¡Excelente, perfil en linea!", Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(Planes.this, values[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;

            try {

                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {


                    List<Map<String, String>> rs = con.execute("/ip/hotspot/user/profile/set .id="+planActual.getIdMikro()+
                            " on-login=':local tiempoTotal "+duracion+";" +
                            " :local hora ([/system clock get time]+$tiempoTotal);" +
                            " :local fecha [/system clock get date];" +
                            " :local date [/system clock get date];" +
                            " :local days "+dia+";" +
                            " :local mdays  {31;28;31;30;31;30;31;31;30;31;30;31};" +
                            " :local months {\"jan\"=1;\"feb\"=2;\"mar\"=3;\"apr\"=4;\"may\"=5;\"jun\"=6;\"jul\"=7;\"aug\"=8;\"sep\"=9;\"oct\"=10;\"nov\"=11;\"dec\"=12};" +
                            " :local monthr  {\"jan\";\"feb\";\"mar\";\"apr\";\"may\";\"jun\";\"jul\";\"aug\";\"sep\";\"oct\";\"nov\";\"dec\"};" +
                            " :local dd  [:tonum [:pick $date 4 6]];" +
                            " :local yy [:tonum [:pick $date 7 11]];" +
                            " :local month [:pick $date 0 3];" +
                            " :local mm (:$months->$month);" +
                            " :set dd ($dd+$days);" +
                            " :local dm [:pick $mdays ($mm-1)];" +
                            " :if ($mm=2 && (($yy&3=0 && ($yy/100*100 != $yy)) || $yy/400*400=$yy) ) do={ :set dm 29 };" +
                            ":while ($dd>$dm) do={" +
                            "  :set dd ($dd-$dm);" +
                            "  :set mm ($mm+1);" +
                            "  :if ($mm>12) do={" +
                            "    :set mm 1;" +
                            "    :set yy ($yy+1);" +
                            "  };" +
                            " :set dm [:pick $mdays ($mm-1)];" +
                            " :if ($mm=2 &&  (($yy&3=0 && ($yy/100*100 != $yy)) || $yy/400*400=$yy) ) do={ :set dm 29 };" +
                            " };" +
                            " :local res \"$[:pick $monthr ($mm-1)]/\";" +
                            " :if ($dd<10) do={ :set res ($res.\"0\") };" +
                            " :set $res \"$res$dd/$yy\";" +
                            " /system scheduler add name=$user start-date=$res" +
                            " start-time=$hora interval=00:01:00 " +
                            " comment=\"MikroFicha-J-Manuel-Fecha-RespaldoTC-hora-para-eliminarse-$res-$hora\" on-event=\"/ip hotspot active remove [find name=$user];" +
                            " /ip hotspot user remove [find name=$user];" +
                            " /system scheduler remove [find name=$user]\";'");


                    con.close();
                }
                else {
                    conexionPerdida = true;
                }


            } catch (MikrotikApiException e) {
                publishProgress(e.getMessage());
                conexionPerdida = true;
            }
            return null;
        }
    }


}