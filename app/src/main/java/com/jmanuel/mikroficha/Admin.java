package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.net.SocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;


public class Admin extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private ImageView verPlanes;
    private ImageView verUsuarios;
    private ImageView verUsuariosActivo;
    private ImageView verServidores;
    private ImageView configPanel;
    private TextView ip_admin;
    private TextView version_admin;
    private TextView cpu_admin;
    private TextView memoria_admin;
    private TextView fecha_admin;
    private TextView hora_admin, sel_imp;
    private TextView usuarios_hotspot;
    private TextView usuarios_activo_hotspot;
    private TextView nservidoresHotspot;
    private TextView totalFicha;
    private TextView totalPlanes, total_ip_bindings;
    private ImageView generarFicha;
    private ImageView verPDF;
    private ImageView scriptAdmin;
    private ImageView verIpBinding;
    private ImageView reloj;
    private ImageView calendario_admin;
    private static final int DATA_COUNT = 30; // cantidad de datos a mostrar
    private static final int REFRESH_RATE = 1000; // tiempo de refresco en milisegundos
    private int mDataCounter = 0;
    private GraphView grafica;
    private ImageView background_ficha;
    public String ip, admin, pass, version, spiner_server = "", spiner_plan = "", puerto = "",
            spiner_interfaces, orientacion = "V", user_pin = "PIN", minus_mayus = "minuscula", spiner_imp;

    ArrayList<String> lista_servidores, lista_planes, lista_fichas_pdf, lista_interfaces, lista_impresoras;
    ArrayAdapter<CharSequence> adapter_planes, adapter_servidores, adapter_interfaces, adapter_impresoras;
    Spinner servidores, planes, interfaces, impresoras_ticket;
    ProgressBar cpubar;
    public BluetoothAdapter bluetoothAdapter;

    private int TOTAL_FICHA, ancho_diseno, alto_diseno;
    private final int HOJA_W = 595, HOJA_H = 845, ESPACIADO = 0;
    private int calculo = 32;
    //private final int HOJA_W = 612, HOJA_H = 792, ESPACIADO = 0;

    // private final int HOJA_W = 648, HOJA_H = 841, ESPACIADO = 0;
    private int POSXI = 20, POSYI = 20;

    private String cpu = "", memoria = "", data = "", nUser = "", nUserActive = "",
            nHostpot = "", nPlanes = "", txrx = "", nBinding = "";
    private String dateTime, ssid_hotspot;

    private ApiConnection con;
    private ProgressDialog progress, progres_imp;
    TaskRouter taskrouter;
    LineGraphSeries<DataPoint> seriesTX, seriesRX;
    int tiempo_graficax = 0; //el tiempo x de la grafica

    //VARIABLES DE LIMITES
    int LIMITE_FICHA = 5; //5esta en la app en linea
    int total_activo = 0, total_activo_control = 0;
    boolean mas_una = false; //verifica la primera vez que consulta los activos

    private boolean BACKUP = false; //permitir respaldos

    SharedPreferences prefences, admob_preference, ticket_preference, prefences_printer;
    SharedPreferences.Editor editor;
    Boolean enReposo = true, fichaCreada = false;

    //ADMOB
    private AdView adview;
    private Boolean ADMOB = true;
    private DatabaseReference mDatabase;
    String uuid_app;

    int hora_reloj = 0, minuto_reloj = 0;
    private boolean SUB = false;
    private boolean ticket = false;
    private Gson gson;
    private List<BtData> Btsaved;

    ConnectThread connectThread; //hilo de impresion
    private BluetoothDevice dispositivoBluetooth;
    public Handler handler;
    android.app.AlertDialog dialog; //modal de creacion de fichas

    //lista de bitmap pra imprimir en termica
    private List<Bitmap> bitmap_termica;

    public String nombre_empresa_tit = "MIKROFICHA", moneda_simb = "$";
    public Bitmap logo_empresa = null;
    public boolean mostrar_plan = false, mostrar_qr = false, mostrar_precio = false, mostrar_logo = true;
    Button btnAgregar = null;

    //VARIABLES DE ANUNCIO =========
    private static final String AD_UNIT_ID = "ca-app-pub-8581380224867392/9004583436";
    //"ca-app-pub-7572150509502514/4873582727"; //poriginal tienda
    //PRUEBA "ca-app-pub-3940256099942544/5224354917";
    private RewardedAd rewardedAd;
    boolean isLoading, completo, esTermica;
    private final String TAG = "Routerapp";
    //==============================
    private String NameFile = "";

    private TextView trafficInfo;

    PdfDocument pdfDocument = new PdfDocument();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");

        prefences = Admin.this.getSharedPreferences("reposo", Context.MODE_PRIVATE);
        editor = prefences.edit();
        editor.putBoolean("enReposo", enReposo);
        editor.apply();
        Log.d("estado", enReposo + "inicio");
        //verifico si tendra anuncios desde firebase
        admob_preference = Admin.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        uuid_app = admob_preference.getString("uuid_app", "N/A");
        ADMOB = admob_preference.getBoolean("ADMOB", true);

        esTermica = false; //digo si imprimi con la termica

        ticket_preference = this.getSharedPreferences("config_ticket", Context.MODE_PRIVATE);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child(uuid_app).exists()) {
                        SUB = true;
                        if (snapshot.child(uuid_app).child("BACKUP").exists()) {
                            BACKUP = (boolean) snapshot.child(uuid_app).child("BACKUP").getValue();
                        }
                    }
                    if (snapshot.child(uuid_app).child("ADMOB").exists()) {
                        prefences = Admin.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
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

        adview = findViewById(R.id.adView);

        //ANUNCIO
        completo = false; //si se vio el anuncio completo
        //admob
        if (ADMOB) {
            adview.setVisibility(View.VISIBLE);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {

                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            adview.loadAd(adRequest);
            loadRewardedAd(); //anuncio quitar ara publicar
        } else {
            adview.setVisibility(View.GONE);
        }
        //finADMOB

        verPlanes = findViewById(R.id.verPlanes);
        verUsuarios = findViewById(R.id.verUsuarios);
        verUsuariosActivo = findViewById(R.id.verUsuariosActivo);
        configPanel = findViewById(R.id.icon_config_panel);
        verServidores = findViewById(R.id.verServidores);
        ip_admin = findViewById(R.id.ip_admin);
        version_admin = findViewById(R.id.version_admin);
        cpu_admin = findViewById(R.id.cpu_admin);
        memoria_admin = findViewById(R.id.memoria_admin);
        fecha_admin = findViewById(R.id.fecha_admin);
        hora_admin = findViewById(R.id.hora_admin);
        usuarios_hotspot = findViewById(R.id.usuarios_hotspot_admin);
        usuarios_activo_hotspot = findViewById(R.id.usuarios_activo_hotspot_admin);
        nservidoresHotspot = findViewById(R.id.servidores_hotspot_admin);
        totalFicha = findViewById(R.id.total_fichas);
        totalPlanes = findViewById(R.id.planes_total);
        total_ip_bindings = findViewById(R.id.total_ip_bindings);
        generarFicha = findViewById(R.id.fichas_nuevas);
        verPDF = findViewById(R.id.pdf_admin);
        scriptAdmin = findViewById(R.id.script_admin);
        verIpBinding = findViewById(R.id.ip_binding);
        reloj = findViewById(R.id.reloj_admin);
        calendario_admin = findViewById(R.id.calendario_admin);
        interfaces = findViewById(R.id.interfaces_spiner_admin);
        cpubar = findViewById(R.id.cpubar);

        lista_interfaces = new ArrayList<String>();
        adapter_interfaces = new ArrayAdapter(Admin.this, R.layout.spinner_disenio, lista_interfaces);
        spiner_interfaces = "-1";


        interfaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spiner_interfaces = adapterView.getItemAtPosition(i).toString();
               /* editor = prefences.edit();
                editor.putString("Interface",spiner_interfaces);
                editor.apply();*/
                seriesTX.resetData(new DataPoint[]{});
                seriesRX.resetData(new DataPoint[]{});
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();


        //Grafica
        grafica = findViewById(R.id.graph_admin);

        seriesTX = new LineGraphSeries<>();
        seriesTX.setColor(Color.parseColor("#F44336")); // Rojo moderno
        seriesTX.setThickness(4);
        seriesTX.setDrawBackground(true);
        seriesTX.setBackgroundColor(Color.parseColor("#44F44336")); // Transparencia
        seriesTX.setDrawDataPoints(false);
        seriesTX.setTitle("Tx");

        seriesRX = new LineGraphSeries<>();
        seriesRX.setColor(Color.parseColor("#2196F3")); // Azul moderno
        seriesRX.setThickness(4);
        seriesRX.setDrawBackground(true);
        seriesRX.setBackgroundColor(Color.parseColor("#442196F3")); // Transparencia
        seriesRX.setDrawDataPoints(false);
        seriesRX.setTitle("Rx");

        grafica.addSeries(seriesTX);
        grafica.addSeries(seriesRX);
        grafica.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        grafica.getLegendRenderer().setVisible(true);
        grafica.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        grafica.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
        grafica.getLegendRenderer().setTextSize(30);

        grafica.setBackgroundColor(Color.WHITE);
        grafica.getGridLabelRenderer().setGridColor(Color.LTGRAY);
        grafica.getGridLabelRenderer().setHorizontalLabelsColor(Color.DKGRAY);
        grafica.getGridLabelRenderer().setVerticalLabelsColor(Color.DKGRAY);

        grafica.getViewport().setXAxisBoundsManual(true);
        grafica.getViewport().setMinX(0);
        grafica.getViewport().setMaxX(DATA_COUNT);
        grafica.getViewport().setYAxisBoundsManual(true);
        grafica.getViewport().setMinY(0);
        grafica.getViewport().setMaxY(1000);
        grafica.getViewport().setScalable(true);

        trafficInfo = findViewById(R.id.trafficInfo);


        ///fin grafica

        progress = new ProgressDialog(this);
        progres_imp = new ProgressDialog(this);
        taskrouter = new TaskRouter();
        lista_fichas_pdf = new ArrayList<>();

        ip_admin.setText(ip);
        version_admin.setText("Versión: " + version);

        calendario_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
                        mes += 1;
                        String date = makeDateString(dia, mes, anio);
                        TaskActualizarFecha taskActualizarFecha = new TaskActualizarFecha();
                        taskActualizarFecha.execute(date);
                    }
                };

                Calendar cal = Calendar.getInstance();
                int y = cal.get(Calendar.YEAR);
                int d = cal.get(Calendar.DAY_OF_MONTH);
                int m = cal.get(Calendar.MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(Admin.this, onDateSetListener, y, m, d);
                datePickerDialog.setCanceledOnTouchOutside(false);
                datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        taskrouter = new TaskRouter();
                        taskrouter.execute();
                    }
                });
                datePickerDialog.show();
            }
        });

        reloj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hora, int minuto) {
                        hora_reloj = hora;
                        minuto_reloj = minuto;
                        String horaes = String.format(Locale.getDefault(), "%2d:%2d:00", hora_reloj, minuto_reloj);
                        TaskActualizarHora taskActualizarHora = new TaskActualizarHora();
                        taskActualizarHora.execute(horaes);
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(Admin.this, onTimeSetListener, hora_reloj, minuto_reloj, true);
                timePickerDialog.setCanceledOnTouchOutside(false);
                timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        taskrouter = new TaskRouter();
                        taskrouter.execute();
                    }
                });
                timePickerDialog.show();
            }
        });

        verIpBinding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                Intent i = new Intent(Admin.this, IPBinding.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();

            }
        });

        verPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                Intent i = new Intent(Admin.this, FichasPDF.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();

            }
        });

        scriptAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                Intent i = new Intent(Admin.this, ScriptAdminHotspot.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();

            }
        });

        verPlanes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                Intent i = new Intent(Admin.this, Planes.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();
            }
        });

        verUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                Intent i = new Intent(Admin.this, Usuario.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();
            }
        });

        verUsuariosActivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskrouter.cancel(true);
                Intent i = new Intent(Admin.this, UsuarioActivo.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();

            }
        });

        verServidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Admin.this, "Servidores Hotspot", Toast.LENGTH_SHORT).show();
            }
        });

        bitmap_termica = new ArrayList<>();

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                if (bundle.getBoolean("exito")) {
                    Toast.makeText(Admin.this, "Conexión éxitosa", Toast.LENGTH_SHORT).show();
                    progres_imp.dismiss();
                    if (btnAgregar != null)
                        btnAgregar.setEnabled(true);
                    sel_imp.setText("Seleccione Impresora: Conectado ✅");
                } else if (bundle.getBoolean("salir")) {
                    if (connectThread != null) {
                        connectThread.cancel();
                        connectThread.disconnectBT();
                    }
                    progres_imp.dismiss();
                } else {
                    Toast.makeText(Admin.this, "Conexión fallida", Toast.LENGTH_SHORT).show();
                    progres_imp.dismiss();
                    if (connectThread != null) {
                        connectThread.cancel();
                        connectThread.disconnectBT();
                    }
                    sel_imp.setText("Seleccione la Impresora: Conectado ❌");
                }
            }
        };
        /*
        generarFicha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarFichas_metodo();

            }


        });
        */
        crearNotificacionCanal(); //notificaciones
        numeroFichas(); //limites de ficha actualizado desde inter quitar para publcar

        // taskrouter.execute();



    }

    //ANUNCIO METODOS
    private void loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    AD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage() + " Faild");
                            rewardedAd = null;
                            Admin.this.isLoading = false;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            Admin.this.rewardedAd = rewardedAd;
                            Log.d(TAG, "onAdLoaded");
                            Admin.this.isLoading = false;
                        }
                    });
        }
    }

    //ANUNCIO
    private void showRewardedVideo() {

        if (rewardedAd == null) {
            return;
        }
        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "onAdShowedFullScreenContent");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        Log.d(TAG, "onAdDismissedFullScreenContent");
                        rewardedAd = null;
                        if (!completo) {
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Admin.this);
                            builder.setMessage("Mira el VIDEO completo, así me ayudas a mantener la app gratis.")
                                    .setCancelable(false)
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                            androidx.appcompat.app.AlertDialog titulo = builder.create();
                            titulo.setTitle("¡Sigue usando la app gratis!");
                            titulo.show();
                        } else {
                            completo = true;
                        }
                        // Preload the next rewarded ad.
                        Admin.this.loadRewardedAd();
                    }
                });
        Activity activityContext = Admin.this;
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        completo = true;
                        Log.d(TAG, "The user earned the reward. completo");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                    }
                });
    }

    private void generarFichas_metodo() {
        esTermica = false;
        connectThread = null;
        taskrouter.cancel(true);
        enReposo = false;
        editor = prefences.edit();
        editor.putBoolean("enReposo", enReposo);
        editor.apply();
        Log.d("estado", enReposo + "_generar_metodo");

        android.app.AlertDialog.Builder alerta = new android.app.AlertDialog.Builder(Admin.this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.modal_agregar_ficha, null);
        alerta.setView(view);
        EditText nfichas = view.findViewById(R.id.n_fichas);
        EditText ancho = view.findViewById(R.id.ancho);
        EditText alto = view.findViewById(R.id.alto);
        EditText nombre_empresa = view.findViewById(R.id.nombre_empresa);
        EditText simbolo_moneda = view.findViewById(R.id.moneda_sombolo);
        background_ficha = view.findViewById(R.id.disenio);
        Button carga_img = view.findViewById(R.id.cambiar_disenio);
        servidores = view.findViewById(R.id.servidores_ficha);
        planes = view.findViewById(R.id.planes_ficha);
        TextView totaPorHoja = view.findViewById(R.id.total_por_hoja);
        TextView limite_ficha = view.findViewById(R.id.limite_ficha_admin);
        TextView texto_dimensiones = view.findViewById(R.id.texto_dimensiones);
        TextView dimensiones_tit = view.findViewById(R.id.dimensiones_tit);
        sel_imp = view.findViewById(R.id.sel_imp);
        LinearLayout ly = view.findViewById(R.id.layout_admin);
        LinearLayout ly2 = view.findViewById(R.id.info_hoja_admin);
        LinearLayout ly3 = view.findViewById(R.id.info_ticket_admin);
        LinearLayout ly4 = view.findViewById(R.id.impresoras_ticket_admin);
        btnAgregar = view.findViewById(R.id.btn_agregar_modal_ficha);
        CheckBox codigo_qr = view.findViewById(R.id.codigo_qr);
        CheckBox plan_ticket = view.findViewById(R.id.plan_ticket);
        CheckBox precio_ticket = view.findViewById(R.id.precio_ticket);
        CheckBox logo_ticket = view.findViewById(R.id.mostrar_logo);
        limite_ficha.setText("Genera " + LIMITE_FICHA + " fichas máximo al mismo tiempo");
        AdView adview_modalficha = view.findViewById(R.id.adView2);

        //admob
        if (ADMOB) {
            adview_modalficha.setVisibility(View.VISIBLE);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {

                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            adview_modalficha.loadAd(adRequest);
        } else {
            adview_modalficha.setVisibility(View.GONE);
        }
        //finADMOB

        //ticket
        impresoras_ticket = view.findViewById(R.id.impresoras_ticket_admin_ficha);

        //RadioGroup radiogrupo_ticket = view.findViewById(R.id.opciones_dimensiones_admin_ticket);

        RadioGroup radiogrupo = view.findViewById(R.id.opciones_grupo_radio_ficha);
        radiogrupo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.vertical_1:
                        orientacion = "V";
                        int res = (HOJA_W / (Integer.parseInt(ancho.getText().toString()) * calculo + ESPACIADO)) *
                                (HOJA_H / (Integer.parseInt(alto.getText().toString()) * calculo + ESPACIADO));
                        totaPorHoja.setText("Fichas por hoja, tamaño y orientación: " + res);
                        break;
                    case R.id.horizontal_1:
                        orientacion = "H";
                        int res1 = (HOJA_H / (Integer.parseInt(ancho.getText().toString()) * calculo + ESPACIADO)) *
                                (HOJA_W / (Integer.parseInt(alto.getText().toString()) * calculo + ESPACIADO));
                        totaPorHoja.setText("Fichas por hoja, tamaño y orientación: " + res1);
                        break;
                }
            }
        });

        RadioGroup pinUserPas = view.findViewById(R.id.opciones_user_pin_admin);
        pinUserPas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.pin_admin:
                        user_pin = "PIN";
                        break;
                    case R.id.user_contra_admin:
                        user_pin = "USPA";
                        break;
                }
            }
        });

        RadioGroup minusmayus = view.findViewById(R.id.opciones_minus_mayus);
        minusmayus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.minuscula_admin:
                        minus_mayus = "minuscula";
                        break;
                    case R.id.mayuscula_admin:
                        minus_mayus = "mayuscula";
                        break;
                }
            }
        });

        mostrar_plan = mostrar_qr = mostrar_precio = false;

        if (ticket) {

            btnAgregar.setEnabled(false);
            simbolo_moneda.setText(ticket_preference.getString("smoneda", "$"));
            moneda_simb = simbolo_moneda.getText().toString();
            if (!SUB) {
                limite_ficha.setText("Genera " + LIMITE_FICHA + " fichas máximo al mismo tiempo en modo ticket");
                //carga_img.setText("Cambiar Logo (PRO)");
                //carga_img.setEnabled(false);
                logo_ticket.setEnabled(false);
                logo_ticket.setText("Mostrar Logo (PRO)");
                nombre_empresa.setText("MIKROFICHA (PRO)");
                nombre_empresa.setEnabled(false);
            } else {
                LIMITE_FICHA = 100;
                limite_ficha.setText("Genera " + LIMITE_FICHA + " fichas máximo al mismo tiempo en modo ticket, (evitamos el calentamiento de tu impresora)");
                carga_img.setText("Cambiar Logo");
                carga_img.setEnabled(true);
                logo_ticket.setEnabled(true);
                logo_ticket.setText("Mostrar Logo");
                if (nombre_empresa.getText().length() == 0) {
                    nombre_empresa.setText("MIKROFICHA");
                }
                nombre_empresa.setEnabled(true);
                nombre_empresa.setText(ticket_preference.getString("nombre_empresa", "MIKROFICHA"));
            }

            texto_dimensiones.setText("Tamaño de impresión");
            texto_dimensiones.setVisibility(View.GONE);
            //radiogrupo_ticket.setVisibility(View.VISIBLE);
            dimensiones_tit.setVisibility(View.GONE);
            ly.setVisibility(View.GONE);
            ly2.setVisibility(View.GONE);
            ly3.setVisibility(View.VISIBLE);
            ly4.setVisibility(View.VISIBLE);
            background_ficha.setImageDrawable(getResources().getDrawable(R.drawable.logo_ticket));
            logo_empresa = ((BitmapDrawable) background_ficha.getDrawable()).getBitmap();
            background_ficha.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        carga_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/");
                startActivityForResult(intent.createChooser(intent, "Seleccione la aplicación"), 10);
            }
        });

        lista_servidores = new ArrayList<String>();
        adapter_servidores = new ArrayAdapter(Admin.this, R.layout.spinner_disenio, lista_servidores);

        lista_planes = new ArrayList<String>();
        adapter_planes = new ArrayAdapter(Admin.this, R.layout.spinner_disenio, lista_planes);

        servidores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spiner_server = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        planes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spiner_plan = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lista_impresoras = new ArrayList<String>();
        adapter_impresoras = new ArrayAdapter(Admin.this, R.layout.spinner_disenio, lista_impresoras);
        impresoras_ticket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spiner_imp = adapterView.getItemAtPosition(i).toString();
                if (spiner_imp.toString() == "Agregar impresora") {
                    //mandar a llamar agregar impresora
                    dialog.dismiss();
                    Intent ac = new Intent(Admin.this, BluethoothMain.class);
                    ac.putExtra("ip", ip);
                    ac.putExtra("puerto", puerto);
                    ac.putExtra("admin", admin);
                    ac.putExtra("pass", pass);
                    ac.putExtra("version", version);
                    startActivity(ac);
                    finish();
                } else if (spiner_imp.toString() == "Seleccione impresora") {
                } else {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (!bluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(Admin.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(Admin.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(Admin.this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 2);
                                return;
                            }
                        }

                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 100);
                    } else {
                        conectarImpreBlutooth();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lista_impresoras.add(0, "Seleccione impresora");
        lista_impresoras.add("Agregar impresora");
        prefences_printer = this.getSharedPreferences("printer_bt", Context.MODE_PRIVATE);
        editor = prefences_printer.edit();

        gson = new Gson();
        String json = prefences_printer.getString("btlist", null);
        Type type = new TypeToken<ArrayList<BtData>>() {
        }.getType();
        Btsaved = gson.fromJson(json, type);
        if (Btsaved == null) {
            Btsaved = new ArrayList<>();
        } else {
            for (BtData btd : Btsaved) {
                lista_impresoras.add(btd.getNombre_bt() + "\n" + btd.getMac_bt());
            }
        }

        impresoras_ticket.setAdapter(adapter_impresoras);
        adapter_impresoras.setNotifyOnChange(true);

        Button btnCancelar = view.findViewById(R.id.btn_cancelar_modal_ficha);

        dialog = alerta.create();
        dialog.show();
        dialog.setCancelable(false);

        TaskCargarServidores taskserverplanes = new TaskCargarServidores();
        taskserverplanes.execute();

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!nfichas.getText().toString().equals("") &&
                        !ancho.getText().toString().equals("") &&
                        !alto.getText().toString().equals("") &&
                        !spiner_server.equals("") &&
                        !spiner_plan.equals("")) {
                    if (Integer.parseInt(nfichas.getText().toString()) > 0 &&
                            Integer.parseInt(nfichas.getText().toString()) <= LIMITE_FICHA) {
                        if (Integer.parseInt(ancho.getText().toString()) >= 3 &&
                                Integer.parseInt(ancho.getText().toString()) <= 5 &&
                                Integer.parseInt(alto.getText().toString()) >= 2 &&
                                Integer.parseInt(alto.getText().toString()) <= 5) {
                            TOTAL_FICHA = Integer.parseInt(nfichas.getText().toString());
                            ancho_diseno = Integer.parseInt(ancho.getText().toString());
                            alto_diseno = Integer.parseInt(alto.getText().toString());
                            lista_fichas_pdf.clear();
                            if (ticket) {//verifico si es opcion de ticket
                                if (codigo_qr.isChecked())
                                    mostrar_qr = true;
                                if (plan_ticket.isChecked())
                                    mostrar_plan = true;
                                if (precio_ticket.isChecked())
                                    mostrar_precio = true;

                                moneda_simb = simbolo_moneda.getText().toString();
                                editor = ticket_preference.edit();
                                editor.putString("smoneda", simbolo_moneda.getText().toString());
                                editor.apply();

                                if (SUB) {
                                    if (logo_ticket.isChecked())
                                        mostrar_logo = true;
                                    else
                                        mostrar_logo = false;

                                    if (nombre_empresa.getText().length() == 0) {
                                        nombre_empresa.setText("MIKROFICHA");
                                    } else {
                                        nombre_empresa_tit = nombre_empresa.getText().toString();
                                        editor = ticket_preference.edit();
                                        editor.putString("nombre_empresa", nombre_empresa.getText().toString());
                                        editor.apply();
                                    }
                                }
                            }
                            TaskCrearFichas crearFichas = new TaskCrearFichas();
                            crearFichas.execute(nfichas.getText().toString(), spiner_server, spiner_plan);
                            //dialog.dismiss();

                        } else {
                            Toast.makeText(Admin.this, "Revise las dimensiones de la ficha", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(Admin.this, "Fichas mínimo: 1 y máximo: " + LIMITE_FICHA, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Admin.this, "Llene todos los campos o revise cada parámetro", Toast.LENGTH_LONG).show();
                }
                //codigo para pober en reposo
                //regresamos el foco a la pantalla admin y reanudamos la lectura del mikrotik
                /*enReposo = true;
                editor.putBoolean("enReposo",enReposo);
                editor.apply();
                Log.d("estado_ficha_generada",enReposo+"");*/
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelarCrearFicha();
            }
        });

    }

    //este metodo es cuando se presiona el boton de cancelar al momento de querer crear ficha modal
    private void cancelarCrearFicha() {
        dialog.dismiss();
        progres_imp.dismiss();
        taskrouter = new TaskRouter();
        taskrouter.execute();
        enReposo = true;
        editor = prefences.edit();
        editor.putBoolean("enReposo", enReposo);
        editor.apply();
        Log.d("estado", enReposo + "_cancelarficha");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread.disconnectBT();
        }
    }

    //este metodo es para cuando se selecciona una impresora dentro de as opciones
    //para empezar a aimprimir los tickets
    private void conectarImpreBlutooth() {
        progres_imp.setMessage("Conectado impresora...");
        progres_imp.setCanceledOnTouchOutside(false);
        progres_imp.show();
        //conectar con la impresora
        dispositivoBluetooth = bluetoothAdapter.getRemoteDevice(spiner_imp.toString().split("\n")[1]);
        connectThread = new Admin.ConnectThread(dispositivoBluetooth, getResources());
        connectThread.start();
    }

   /* private void guardar_url(Uri url){
        SharedPreferences.Editor editor = ticket_preference.edit();
        editor.putString("url_logo", url.toString());
        editor.commit();
    }

    private void obtener_url(){
        String mImageUri = ticket_preference.getString("url_logo", ""+getResources().getDrawable(R.drawable.logmikroficha));
        if(mImageUri != null){
            Uri uri = Uri.parse(mImageUri);
            background_ficha.setImageURI(uri);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) { //activar Bluetooth
                cancelarCrearFicha(); //cancelamos crear ficha
            } else if (requestCode == 10) { // resultado de buscar imagen
                Uri path = data.getData();
                if (background_ficha != null) {
                    background_ficha.setImageURI(path);
                    logo_empresa = ((BitmapDrawable) background_ficha.getDrawable()).getBitmap();
                } else {
                    Toast.makeText(Admin.this, "No se pudo aplicar la imagen. Vuelve a abrir el generador de fichas.", Toast.LENGTH_LONG).show();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == 100 || requestCode == 2) { //resultado de activar bluetooth
                cancelarCrearFicha(); //cancelamos crear ficha
            } else if (requestCode == 101) { //resultado de activar bluetooth
                Toast.makeText(Admin.this, "Es necesario los permisos de bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private String makeDateString(int dia, int mes, int anio) {
        return getMonthFormat(mes) + "/" + dia + "/" + anio;
    }

    private String getMonthFormat(int mes) {
        if (mes == 1)
            return "jan";
        if (mes == 2)
            return "feb";
        if (mes == 3)
            return "mar";
        if (mes == 4)
            return "apr";
        if (mes == 5)
            return "may";
        if (mes == 6)
            return "jun";
        if (mes == 7)
            return "jul";
        if (mes == 8)
            return "aug";
        if (mes == 9)
            return "sep";
        if (mes == 10)
            return "oct";
        if (mes == 11)
            return "nov";
        if (mes == 12)
            return "dec";

        return "jan";
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("estado", enReposo + "_pause");
        if (enReposo) { //si esta en pause y reposo es verdadero es decir que estandoo en t cancelo porqeu entro en pause vamos a cancelar por que entro en pause
            taskrouter.cancel(true);
            if (ADMOB)
                adview.pause();
            enReposo = true;
            editor = prefences.edit();
            editor.putBoolean("enReposo", enReposo);
            editor.apply();
            Log.d("estado", enReposo + "_pause2");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        enReposo = prefences.getBoolean("enReposo", true);
        Log.d("estado", enReposo + "_resume");

        if (completo && !esTermica) {
            Log.d(TAG, "completo_resumen");
            File filepath = new File(Admin.this.getFilesDir(), NameFile);
            Uri urlfile = FileProvider.getUriForFile(Admin.this, getApplicationContext().getPackageName() + ".provider", filepath);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            target.setDataAndType(urlfile, "application/pdf");

            Intent c = Intent.createChooser(target, "Seleccione la aplicación");
            try {
                startActivity(c);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(Admin.this, "No tiene aplicación para leer PDF´s", Toast.LENGTH_LONG);
            }

            completo = false;
        }

        if (fichaCreada) {
            Log.d(TAG, "creadas_resumen");
            enReposo = true;
            editor = prefences.edit();
            editor.putBoolean("enReposo", enReposo);
            editor.apply();
            fichaCreada = false;
        }

        if (enReposo) { //si esta en reposo amos leer el router
            crearNotificacionCanal(); //notificaciones
            numeroFichas(); //limites de ficha actualizado desde inter quitar para publcar
            if (ADMOB)
                adview.resume();
            spiner_interfaces = "-1";
            taskrouter = new TaskRouter();
            taskrouter.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectThread != null) {
            connectThread.cancel();
            connectThread.disconnectBT();
        }
        taskrouter.cancel(true);
    }

    private void generarNotificacion() {
        String canalId = getString(R.string.canal_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Admin.this, canalId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Nueva ficha activada")
                .setContentText("Nuevo usuario conectado")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Nuevo usuario conectado"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Admin.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());

    }

    private void crearNotificacionCanal(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String name = getString(R.string.nombre_canal);
            String canalId = getString(R.string.canal_id);
            String descricion = getString(R.string.canal_id_descrip);
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel canal = new NotificationChannel(canalId, name,importancia);
            canal.setDescription(descricion);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    public void numeroFichas(){
        long interval = 3600;
        if(BuildConfig.DEBUG)
            interval = 5;

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings frconf = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(frconf);
        HashMap<String,Object> actualizacion = new HashMap<>();
        actualizacion.put("limiteficha",LIMITE_FICHA);

        remoteConfig.setDefaultsAsync(actualizacion);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(Admin.this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        cargaLimiteFicha();
                    }
                });
    }

    private void cargaLimiteFicha() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        String limf = remoteConfig.getString("limiteficha");
        if(limf != "")
            LIMITE_FICHA = Integer.parseInt(limf);

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).exists() && snapshot.child(uuid_app).child("MAX_FICHA").exists()) {
                        LIMITE_FICHA = Integer.parseInt(snapshot.child(uuid_app).child("MAX_FICHA").getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void generarPDF() {
        // PdfDocument pdfDocument = new PdfDocument();
        pdfDocument = new PdfDocument();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");

        Paint paint = new Paint();
        Paint title = new Paint();
        Paint marca_agua = new Paint();
        Bitmap imagen, imagenEscala;
        //########DATOS DE ENTRADA##########
        int WX = 0, HY = 0;
        if(orientacion.equals("V")) {
            WX = HOJA_W;
            HY = HOJA_H; //tamanio de a hoja
        }
        else{
            WX = HOJA_H;
            HY = HOJA_W; //tamanio de a hoja
        }
        //SE RECALCULA LA POSISION DE INICIO EN X E Y DE LAS FICHAS
        POSXI = ((WX - ((WX / (ancho_diseno * calculo)) * (ancho_diseno * calculo))) - (ESPACIADO * (WX /(ancho_diseno * calculo)) )) / 2;
        POSYI = ((HY - ((HY / (alto_diseno * calculo)) * (alto_diseno * calculo))) - (ESPACIADO * (HY /(alto_diseno * calculo)) )) / 2;

        //POSXI Y POSYI posiciones iniciales de l ficha
        int anchoFicha = ancho_diseno, altoFicha = alto_diseno;//medidas de las fichas en CM
        float NFICHAS = TOTAL_FICHA; //nuero de fichas para imprimir----
        int espacio = ESPACIADO;
        //#################################

        title.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.black));
        float rel = (ancho_diseno * 10) / 5;
        float tam_letra = rel;
        title.setTextSize(tam_letra);
        marca_agua.setTextSize(tam_letra+5);
        marca_agua.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.NORMAL));
        marca_agua.setColor(Color.parseColor("#E8E8E8"));

        int dx = anchoFicha * calculo + ESPACIADO, dy= altoFicha * calculo + ESPACIADO;//dx dy tamaño de la ficha el 32 es un valor calcualdo
        int lx = 0,ly = 0; //
        int contx = WX / dx; //cuantas fich entran hrizontal
        int conty = HY / dy; //cuantas fich entran vertical
        float MAXFXH = contx * conty; //maximo de fichas por hojas
        int NHOJAS;
        if(NFICHAS < MAXFXH)
            NHOJAS = 1;
        else
            NHOJAS = (int) Math.ceil(NFICHAS / MAXFXH);

        Drawable img = background_ficha.getDrawable();
        //Drawable img = background_ficha.getDrawable();
        //imagen = ((BitmapDrawable) background_ficha.getDrawable()).getBitmap();
        //imagenEscala = Bitmap.createScaledBitmap(imagen, dx, dy, true);//getResizeBirmap(imagen,dx,dy);//Bitmap.createScaledBitmap(imagen, dx, dy, true);

        int fichaImpresa = 0;
        for(int k = 1; k <= NHOJAS; k++){
            lx = POSXI;
            ly=POSYI;
            PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(WX, HY, k).create();
            PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
            Canvas canvas = myPage.getCanvas();
            for (int j = 0; j < conty && fichaImpresa < NFICHAS; j++){
                for (int i = 0; i < contx  && fichaImpresa < NFICHAS; i++) {

                    img.setBounds(lx ,ly ,lx + dx,ly + dy);
                    img.draw(canvas);
                    if(!SUB) {
                        canvas.drawText("MIKROFICHA.COM", lx + 15, ly + (int) (dy / 2) - 8, marca_agua);
                        canvas.drawText("Remover con", lx + 20, ly + (int) (dy / 2)+15, marca_agua);
                        canvas.drawText("Suscripción", lx + 20, ly + (int) (dy / 2)+30, marca_agua);
                    }
                    //canvas.drawBitmap(imagenEscala, lx, ly, paint);
                    if(user_pin.equals("PIN"))
                        canvas.drawText("PIN: "+lista_fichas_pdf.get(fichaImpresa),lx + 20,ly+(int)(dy / 2),title);
                    else if(user_pin.equals("USPA")){
                        canvas.drawText("Usuario: "+lista_fichas_pdf.get(fichaImpresa),lx + 20,ly+(int)(dy / 2),title);
                        canvas.drawText("Contraseña: "+lista_fichas_pdf.get(fichaImpresa),lx + 20,ly+(int)(dy / 2) + 10,title);
                    }
                    lx += dx+espacio;
                    fichaImpresa++;
                }
                lx=POSXI;
                ly += dy+espacio;
            }

            pdfDocument.finishPage(myPage);
        }

        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        dateTime =  "_"+simpleDateFormat.format(calendar.getTime()).toString();
        NameFile = "MikroFicha_"+spiner_plan+dateTime+".pdf";
        //abirArchivoGenerado(pdfDocument);
        try {
            File file = new File(Admin.this.getFilesDir(), NameFile);
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();
            fichaCreada = true;
            if(ADMOB)
                showRewardedVideo(); //mostramos los anuncions
            else
                abirArchivoGenerado(pdfDocument);
        } catch (FileNotFoundException e) {
            Toast.makeText(Admin.this,e.getMessage(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(Admin.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void abirArchivoGenerado(PdfDocument pdfDocument){
        Log.d(TAG,"genramos");
        //File file = new File(Admin.this.getFilesDir(),NameFile);
        //pdfDocument.writeTo(new FileOutputStream(file));
        //Toast.makeText(Admin.this,"Documento guardado",Toast.LENGTH_SHORT).show();

        File filepath = new File(Admin.this.getFilesDir(),NameFile);
        Uri urlfile = FileProvider.getUriForFile(Admin.this,getApplicationContext().getPackageName()+".provider",filepath);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
        target.setDataAndType(urlfile,"application/pdf");

        Intent c = Intent.createChooser(target, "Seleccione la aplicación");
        try {
            startActivity(c);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(Admin.this, "No tiene aplicación para leer PDF", Toast.LENGTH_LONG);
        }

    }

    public void generarPDFTermica() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Admin.this);
        builder.setMessage("\uD83D\uDED1 No cierre este mensaje hasta que la impresora termine de imprimir, de lo contrario se cancelará su impresión.\n " +
                        "* Recuerde mantener cargada su impresora. \uD83D\uDD0B")
                .setCancelable(false)
                .setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        esTermica = true;
                        if(ADMOB)
                            showRewardedVideo(); //mostramos los anuncions
                        cancelarCrearFicha();
                    }
                });
        androidx.appcompat.app.AlertDialog titulo = builder.create();
        titulo.setTitle("¡Aviso de impresión! \uD83D\uDDA8");
        titulo.show();

        int cont = 1;
        for(String tic: lista_fichas_pdf) {
            try {
                connectThread.printCustom(nombre_empresa_tit, 3, 1);
                connectThread.printNewLine();
                if (mostrar_logo) {
                    int prop = logo_empresa.getWidth() / logo_empresa.getHeight();
                    if (prop >= 0 && prop <= 1)
                        connectThread.printPhoto(logo_empresa, 128, 128);
                    else
                        connectThread.printPhoto(logo_empresa, 150 * prop, 80 * prop);
                    connectThread.printNewLine();
                }
                if(!SUB){ connectThread.printCustom("* Remover con una suscripcion *", 0, 1);}

                if (user_pin.equals("PIN")) {
                    connectThread.printCustom("PIN: " + tic, 0, 1);
                    connectThread.printNewLine();
                    if (mostrar_precio && mostrar_plan) {
                        connectThread.printCustom("Plan: " + spiner_plan.split(":")[0] + " " +
                                ".... "+moneda_simb+" " + spiner_plan.split(":")[1], 1, 1);
                        connectThread.printNewLine();
                    } else if (mostrar_precio && !mostrar_plan) {
                        connectThread.printCustom("Precio: .... "+moneda_simb+" " + spiner_plan.split(":")[1], 1, 1);
                        connectThread.printNewLine();
                    } else if (mostrar_plan && !mostrar_precio) {
                        connectThread.printCustom("Plan: " + spiner_plan.split(":")[0], 1, 1);
                        connectThread.printNewLine();
                    }
                    if (mostrar_qr) {
                        connectThread.printQr("http://" + ssid_hotspot + "/login?username=" + tic + "&password=" + tic, 128, 1);
                        connectThread.printNewLine();
                    }
                } else if (user_pin.equals("USPA")) {
                    connectThread.printCustom("USR: " + tic + "  " + "PAS: " + tic, 0, 1);
                    connectThread.printNewLine();
                    if (mostrar_precio && mostrar_plan) {
                        connectThread.printCustom("Plan: " + spiner_plan.split(":")[0] + " " +
                                ".... "+moneda_simb+" " + spiner_plan.split(":")[1], 1, 1);
                        connectThread.printNewLine();
                    } else if (mostrar_precio && !mostrar_plan) {
                        connectThread.printCustom("Precio: "+moneda_simb+" " + spiner_plan.split(":")[1], 1, 1);
                        connectThread.printNewLine();
                    } else if (mostrar_plan && !mostrar_precio) {
                        connectThread.printCustom("Plan: " + spiner_plan.split(":")[0], 1, 1);
                        connectThread.printNewLine();
                    }
                    if (mostrar_qr) {
                        connectThread.printQr("http://" + ssid_hotspot + "/login?username=" + tic + "&password=" + tic, 128, 1);
                        connectThread.printNewLine();
                    }
                }
                connectThread.printCustom(connectThread.getDateTime()[0] + " " + connectThread.getDateTime()[1], 0, 1);
                connectThread.printCustom("Consulta el estado en:", 1, 1);
                connectThread.printCustom(ssid_hotspot + "/status", 1, 1);
                connectThread.printCustom("#"+cont, 0, 1);
                if (!SUB) {
                    connectThread.printCustom("....................", 1, 1);
                    connectThread.printCustom("https://mikroficha.com", 0, 1);
                    connectThread.printCustom("Remover con una suscripcion", 0, 1);
                    connectThread.printCustom("....................", 1, 1);
                }
                connectThread.printNewLine();
                connectThread.printNewLine();
                connectThread.printNewLine();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            cont ++;
         }
    }

    public void showPopMenu(View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_admin_config);
        popupmenu.show();
    }

    public void showPopMenuPrint(View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_admin_print);
        popupmenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.config_reiniciar:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Admin.this);
                builder.setMessage("¿Está seguro de reiniciar el router?")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                taskrouter.cancel(true);
                                TaskReiniciarRouter reiniciarRouter = new TaskReiniciarRouter();
                                reiniciarRouter.execute();
                                Toast.makeText(Admin.this, "Salga de esta pantalla...", Toast.LENGTH_SHORT).show();
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
            case R.id.config_backup:
                androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(Admin.this);
                builder1.setMessage("Se creará un respaldo del router")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(BACKUP) {
                                    taskrouter.cancel(true);
                                    TaskBackup taskBackup = new TaskBackup();
                                    taskBackup.execute();
                                    dialogInterface.dismiss();
                                }else{
                                    Toast.makeText(Admin.this,"Opción sólo para usuarios con suscripción Mensual o Anual", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog titulo1 = builder1.create();
                titulo1.setTitle("¡Aviso!");
                titulo1.show();
                return true;
            case R.id.config_print_termica:
                ticket = true;
                androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(Admin.this);
                builder2.setMessage("Esta opción requiere que tengas una impresora térmica Bluetooth")
                        .setCancelable(true)
                        .setPositiveButton("Si tengo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                generarFichas_metodo();
                            }
                        }).setNegativeButton("No tengo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                androidx.appcompat.app.AlertDialog titulo2 = builder2.create();
                titulo2.setTitle("¡Aviso! \uD83D\uDDA8");
                titulo2.show();
                //generarPDFTermica();
                return true;
            case R.id.config_print_inkjet:
                ticket = false;
                generarFichas_metodo();
                return true;
            default:
                return false;
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Admin.this);
        builder.setMessage("¿Está seguro que deseas salir?")
                .setCancelable(false)
                .setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (taskrouter != null) {
                            taskrouter.cancel(true);
                        }
                        startActivity(new Intent(Admin.this, MainActivity.class));
                        finish();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog titulo = builder.create();
        titulo.setTitle("Salir del Admin");

        // MUY IMPORTANTE: evita error WindowLeaked
        if (!isFinishing()) {
            titulo.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()){
            case R.id.usuario_hotspot:
                taskrouter.cancel(true);
                i = new Intent(Admin.this, Usuario.class);
                i.putExtra("ip", ip);
                i.putExtra("puerto", puerto);
                i.putExtra("admin", admin);
                i.putExtra("pass", pass);
                i.putExtra("version", version);
                startActivity(i);
                finish();
                break;
            case R.id.servidor_hotspot:
                taskrouter.cancel(true);
                startActivity(new Intent(Admin.this, Router.class));
                finish();
                break;
            case R.id.impr_blue:
                taskrouter.cancel(true);
                i = new Intent(Admin.this, BluethoothMain.class);
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

    private void esperarTarea(){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){}
    }

    class TaskReiniciarRouter extends AsyncTask<Void, String, Void>{
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
        protected Void doInBackground(Void... value) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    con.execute("/system/reboot");
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

    class TaskRouter extends AsyncTask<Void, String, Void> {


        @Override
        protected void onPreExecute() {
            progress.setMessage("Estableciendo la conexión...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            tiempo_graficax = 0;
            seriesTX.resetData(new DataPoint[]{});
            seriesRX.resetData(new DataPoint[]{});
            mas_una = false;
            lista_interfaces.clear();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[4].equals("1")) {
                Log.d("entro",values[0]);
                String hora = values[2].split(" ")[1];
                cpu_admin.setText(values[0].toString());
                memoria_admin.setText(values[1].toString() + " MB");
                hora_admin.setText(hora.split(":")[0] + ":" + hora.split(":")[1]);
                fecha_admin.setText(values[3].split(" ")[0]);
                usuarios_hotspot.setText(values[5].toString());
                usuarios_activo_hotspot.setText(values[6].toString());
                nservidoresHotspot.setText(values[7].toString());
                totalFicha.setText(values[5].toString());
                totalPlanes.setText(nPlanes);
                total_ip_bindings.setText(nBinding);
                cpubar.setProgress(Integer.parseInt(values[0].toString().split("%")[0]));

                Calendar cal = Calendar.getInstance();
                Date date = cal.getTime();
                float tx = Math.round((Float.parseFloat(txrx.split(":")[0]) * 0.001f)*100.0f) / 100.0f;
                float rx = Math.round((Float.parseFloat(txrx.split(":")[1]) * 0.001f)*100.0f) / 100.0f;
                Log.d("Grafica", "TX: "+tx+" RX: "+rx+"\n");
               // seriesTX.appendData(new DataPoint(date,tx),true,5);
              //  seriesRX.appendData(new DataPoint(date,rx),true,5);

                // agregar los nuevos datos a la serie
                seriesRX.appendData(new DataPoint(mDataCounter, rx), true, DATA_COUNT);
                seriesTX.appendData(new DataPoint(mDataCounter, tx), true, DATA_COUNT);

                String downloadStr = String.format(Locale.getDefault(), "%.0f kbps", rx);
                String uploadStr = String.format(Locale.getDefault(), "%.0f kbps", tx);
                trafficInfo.setText("Download: " + downloadStr + " Upload: " + uploadStr);

                // actualizar el contador de datos
                mDataCounter++;

              //  double minX = Math.max(0,mDataCounter - (3000 / 1000) * 60 );
                // ajustar los límites de la escala del eje X
                grafica.getViewport().setMinX(mDataCounter - DATA_COUNT);//minX);//mDataCounter - DATA_COUNT);
                grafica.getViewport().setMaxX(mDataCounter);

                int windowSize = 30; // cantidad de puntos recientes
                double txMax = Double.MIN_VALUE, txMin = Double.MAX_VALUE;
                double rxMax = Double.MIN_VALUE, rxMin = Double.MAX_VALUE;

                int txStart = Math.max(0, mDataCounter - windowSize);
                int rxStart = Math.max(0, mDataCounter - windowSize);

                Iterator<DataPoint> itTx = seriesTX.getValues(txStart, mDataCounter);
                while (itTx.hasNext()) {
                    DataPoint dp = itTx.next();
                    txMax = Math.max(txMax, dp.getY());
                    txMin = Math.min(txMin, dp.getY());
                }

                Iterator<DataPoint> itRx = seriesRX.getValues(rxStart, mDataCounter);
                while (itRx.hasNext()) {
                    DataPoint dp = itRx.next();
                    rxMax = Math.max(rxMax, dp.getY());
                    rxMin = Math.min(rxMin, dp.getY());
                }

                double minY = Math.min(rxMin, txMin);
                double maxY = Math.max(rxMax, txMax);

                // Evita que quede plano si los valores son iguales
                if (minY == maxY) {
                    // Si los valores son iguales, usa un rango base mínimo para evitar gráfico plano
                    minY = 0;
                    maxY = minY + 20; // mínimo 20 kbps de altura visual
                }
                double centerY = (minY + maxY) / 2;
                double rangeY = (maxY - minY) * 0.55;

                grafica.getViewport().setYAxisBoundsManual(true);
                grafica.getViewport().setMinY(centerY - rangeY);
                grafica.getViewport().setMaxY(centerY + rangeY);

                // actualizar la vista del gráfico
                grafica.onDataChanged(true, true);

                Log.d("Grafica","Control: "+total_activo_control+"\n");
                Log.d("Grafica","TotalActivo: "+total_activo+"\n");

                if(total_activo_control > total_activo && mas_una) { //notificacion de usuario nuevo
                    total_activo = total_activo_control;
                    generarNotificacion();
                }
                else if(total_activo > total_activo_control)
                    total_activo = total_activo_control;

            }
            else if(values[4].equals("2")) {
                lista_interfaces.add(values[8]);
                interfaces.setAdapter(adapter_interfaces);
                adapter_interfaces.setNotifyOnChange(true);
                Log.d("Inter", values[8]);
                progress.dismiss();
            }
            else if(values[4].equals("0")){
                progress.dismiss();
                Toast.makeText(Admin.this,"Se perdió la conexón con el Router", Toast.LENGTH_LONG).show();
            }
            else if(values[4].equals("-1")){
                progress.dismiss();
                Toast.makeText(Admin.this,"Error: "+values[5], Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected Void doInBackground(Void... strings) {
                Boolean conexionPerdida = false;
                String error ="";
                while(!conexionPerdida){
                    try {
                        //con = ApiConnection.connect(ip); // connect to router
                        con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                        con.login(admin, pass);
                        if (con.isConnected()) {
                            List<Map<String, String>> rs = con.execute("/system/resource/print");
                            for (Map<String, String> r : rs) {
                                memoria = "";
                                cpu = r.get("cpu-load").toString() + "%";
                                memoria += (Long.parseLong(r.get("free-memory").toString()) / 1000000);
                            }
                            rs = con.execute("/system/clock/print");
                            for (Map<String, String> r : rs) {
                                data = r.get("date").toString();
                                data += " " + r.get("time").toString();
                            }
                            rs = con.execute("/ip/hotspot/profile/print");
                            for (Map<String, String> r : rs) {
                                if (r.containsKey("dns-name"))
                                    ssid_hotspot = r.get("dns-name").toString();
                            }

                            rs = con.execute("/ip/hotspot/user/print");
                            nUser = rs.size() + "";
                            rs = con.execute("/ip/hotspot/active/print");
                            nUserActive = rs.size() + "";
                            rs = con.execute("/ip/hotspot/print");
                            nHostpot = rs.size() + "";
                            rs = con.execute("/ip/hotspot/user/profile/print");
                            nPlanes = rs.size() + "";
                            rs = con.execute("/ip/hotspot/ip-binding/print");
                            nBinding = rs.size() + "";
                            rs = con.execute("/ip/hotspot/active/print");
                            if (!mas_una) { //si es la rimera vez que lee los activos
                                total_activo = rs.size();
                                total_activo_control = rs.size();
                                mas_una = true;
                                rs = con.execute("/interface/print detail");
                                for (Map<String, String> r : rs) {
                                    publishProgress("", "", "", "", "2", "", "", "", r.get("name").toString()); //agrega a la lista de interfaces
                                }
                            } else
                                total_activo_control = rs.size();

                            //grafico la primera interfaz
                            if (!spiner_interfaces.equals("-1")){
                                rs = con.execute("/interface/monitor-traffic interface=" + spiner_interfaces + " once");
                                for (Map<String, String> r : rs) {
                                    if (r.containsKey("tx-bits-per-second") &&
                                            r.containsKey("rx-bits-per-second")) {
                                        txrx = r.get("tx-bits-per-second").toString();
                                        txrx += ":" + r.get("rx-bits-per-second").toString();
                                    }
                                }

                            }
                            else{
                                txrx = "0:0";
                            }
                            con.close();

                        }
                        else {
                            conexionPerdida = true;
                            con.close();
                        }

                    } catch (MikrotikApiException e) {
                        e.printStackTrace();
                        conexionPerdida = true;
                        error = e.getMessage();
                        publishProgress("","","","","-1",error,"","","");
                    }

                    if(conexionPerdida)
                        publishProgress("","","","","-1",error,"","","");
                    else
                        publishProgress(cpu,memoria,data,data,"1",nUser,nUserActive,nHostpot,"");

                    esperarTarea();
                    if(isCancelled())
                        break;
                }

            return null;
        }

        @Override
        protected void onPostExecute(Void string) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Admin.this);
            builder.setMessage("No es posible conectarse con el Router")
                    .setCancelable(false)
                    .setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(Admin.this, Router.class));
                            finish();
                            taskrouter.cancel(true);

                        }
                    })
                    .setNegativeButton("Reconectar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            taskrouter.cancel(true);
                            taskrouter = new TaskRouter();
                            taskrouter.execute();

                        }
                    });
            AlertDialog titulo = builder.create();
            titulo.setTitle("Aviso");
            titulo.show();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
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
            lista_planes.add(0,"");
            adapter_servidores.setNotifyOnChange(true);
            servidores.setAdapter(adapter_servidores);
            adapter_planes.setNotifyOnChange(true);
            planes.setAdapter(adapter_planes);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[1].equals("0")){
                lista_servidores.add(values[0]);
            }
            else{
                lista_planes.add(values[2]);
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
                    rs = con.execute("/ip/hotspot/user/profile/print");
                    for (Map<String,String> r : rs) {
                        publishProgress("","1",
                                    r.get("name").toString(),
                                    "1");
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

    class TaskCrearFichas extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Generando fichas espere...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(Admin.this,values[0],Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            if(!ticket) {
                generarPDF();
                dialog.dismiss();
            }
            else{
                generarPDFTermica();
            }
        }

        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            List<String> user_en_rb = new ArrayList<>();
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);
                if (con.isConnected()) {
                    String tiempo="";
                    String perfil="";
                    List<Map<String, String>> rs = con.execute("/ip/hotspot/user/profile/print");
                    for (Map<String,String> r : rs) {
                        if (r.get("name").toString().contains(":")){
                            if (r.get("name").toString().equals(value[2])) {
                                if(r.containsKey("session-timeout")) {
                                    tiempo = r.get("session-timeout").toString();
                                }
                                else {
                                    tiempo = "-";
                                }
                                perfil = r.get("name").toString();
                                break;
                            }
                        }
                        else{
                            if (r.get("name").toString().equals(value[2])) {
                                if(r.containsKey("session-timeout")) {
                                    tiempo = r.get("session-timeout").toString();
                                }
                                else {
                                    tiempo = "-";
                                }
                                perfil = r.get("name").toString();

                                break;
                            }
                        }
                    }
                    int maximo = Integer.parseInt(value[0]);
                    int i = 0;
                    //obtenemos todos los usuarios que estan el RB para no meter uno que ya este
                    rs = con.execute("/ip/hotspot/user/print");
                    for (Map<String,String> r : rs) {
                        user_en_rb.add(r.get("name"));
                    }
                    //Variables para Generar el ID de Forma Aleatoria//
                    Random aleatorio = new Random();
                    String alfa;
                    if(minus_mayus.equals("mayuscula"))
                        alfa = "ABCDEFGHIJKLMNOPQRSTVWXYZ$%&@";
                    else
                        alfa = "abcdefghijklmnopqrstuvwxyz$%&@";
                    String cadena = "";    //Inicializamos la Variable//
                    int numero;
                    int forma;
                    //Establecemos el Método para que relacione los números con las letras:
                    //Método para el Cálculo de Código//

                    while(i < maximo) {
                        cadena = "";

                        cadena += alfa.charAt(aleatorio.nextInt(alfa.length()));
                        cadena += alfa.charAt(aleatorio.nextInt(alfa.length()));
                        cadena += alfa.charAt(aleatorio.nextInt(alfa.length()));

                        int num = aleatorio.nextInt(100); // de 0 a 99
                        String numeroFormateado = String.valueOf(num);

                        // Si el número tiene menos de 3 dígitos, rellenamos con números aleatorios
                        while (numeroFormateado.length() < 3) {
                            int digitoAleatorio = aleatorio.nextInt(10); // del 0 al 9
                            numeroFormateado = digitoAleatorio + numeroFormateado;
                        }

                        cadena += numeroFormateado;
                        String usuario = cadena;
                        String contrasenia = cadena;

                        if(!user_en_rb.contains(usuario)){
                            try{
                                if(tiempo.equals("-")) { //para aquellos planes que no tienen tiempo
                                    if(user_pin.equals("PIN")){
                                        con.execute("/ip/hotspot/user/" +
                                                "add name=" + usuario +" profile=" + perfil +
                                                " server=" + value[1] +
                                                " comment=MikroFicha-Power-By-mikroficha.com");
                                        lista_fichas_pdf.add(usuario);
                                        i++;
                                    }
                                    else
                                    {
                                        con.execute("/ip/hotspot/user/" +
                                                "add name=" + usuario + " password=" + contrasenia + " profile=" + perfil +
                                                " server=" + value[1] +
                                                " comment=MikroFicha-Power-By-mikroficha.com");
                                        lista_fichas_pdf.add(usuario);
                                        i++;
                                    }

                                }
                                else{
                                    if(user_pin.equals("PIN")){
                                        con.execute("/ip/hotspot/user/" +
                                                "add name=" + usuario + " profile=" + perfil +
                                                " limit-uptime=" + tiempo + " server=" + value[1] +
                                                " comment=MikroFicha-Power-By-mikroficha.com");
                                        lista_fichas_pdf.add(usuario);
                                        i++;
                                    }
                                    else {
                                        con.execute("/ip/hotspot/user/" +
                                                "add name=" + usuario + " password=" + contrasenia + " profile=" + perfil +
                                                " limit-uptime=" + tiempo + " server=" + value[1] +
                                                " comment=MikroFicha-Power-By-mikroficha.com");
                                        lista_fichas_pdf.add(usuario);
                                        i++;
                                    }
                                }

                            }catch(MikrotikApiException e){}
                        }

                    }
                    con.close();
                    publishProgress("Fichas creadas");
                }
                else {
                    conexionPerdida = true;
                    publishProgress("Se perdió la conexión");
                }

            } catch (MikrotikApiException e) {
                publishProgress(e.getMessage());
                conexionPerdida = true;
            }
            return null;
        }
    }

    class TaskActualizarHora extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Actualizando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            taskrouter = new TaskRouter();
            taskrouter.execute();
        }


        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);

                if (con.isConnected()) {
                    con.execute("/system/clock/set time="+value[0]);
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

    class TaskActualizarFecha extends AsyncTask<String, String, Void>{
        @Override
        protected void onPreExecute() {
            progress.setMessage("Actualizando...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            taskrouter = new TaskRouter();
            taskrouter.execute();
        }


        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
                //con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);

                if (con.isConnected()) {
                    con.execute("/system/clock/set date="+value[0]);
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

    class TaskBackup extends AsyncTask<String, String, Void>{
        String date1 = "";
        @Override
        protected void onPreExecute() {
            progress.setMessage("Creando respaldo...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat;
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            date1 = simpleDateFormat.format(calendar.getTime()).toString();
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.dismiss();
            taskrouter = new TaskRouter();
            taskrouter.execute();
        }


        @Override
        protected Void doInBackground(String... value) {
            Boolean conexionPerdida = false;
            try {
               // con = ApiConnection.connect(ip); // connect to router
                con = ApiConnection.connect(SocketFactory.getDefault(), ip,Integer.parseInt(puerto), ApiConnection.DEFAULT_COMMAND_TIMEOUT);
                con.login(admin, pass);

                if (con.isConnected()) {
                    con.execute("/export file=MikrofichaBackup-"+date1+" compact");
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
                if (ActivityCompat.checkSelfPermission(Admin.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(Admin.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(Admin.this, new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                mmSocket = tmp;
                outputStream=mmSocket.getOutputStream();
                inputStream=mmSocket.getInputStream();

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            if (ActivityCompat.checkSelfPermission(Admin.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(Admin.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(Admin.this, new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
                String info ="";
                for(int i = 0; i < lista_fichas_pdf.size(); i++) {
                    info += "\n\n";
                    format[2] = ((byte)(0x8 | arrayOfByte1[2]));
                    info = "MIKROFICHA\n";
                    info += "\n";
                    info += lista_fichas_pdf.get(i).toString() + "\n";
                    info += "\n\n";
                    outputStream.write(ESC_CANCEL_BOLD);
                    outputStream.write(info.getBytes());
                }
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

                terminar();

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void terminar(){
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putBoolean("salir",true);
            message.setData(bundle);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            handler.sendMessage(message);
        }

        public void printDataTest() {
            try{
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
                outputStream.flush();
                //outputStream.write(cc);
                //printNewLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //print photo
        public void printPhoto(Bitmap bmp, int width, int height) {
            try {
                //((BitmapDrawable) logo_img.getDrawable()).getBitmap();//
                //Bitmap bmp = BitmapFactory.decodeResource(getResources(),img);
                if(bmp!=null){
                    //byte[] command = Utils.decodeBitmap(bmp);
                    byte[] command = Utils.bitmapToBytes(bmp,false,width,height); //este funciona
                    //byte[] command = Utils.bitmapToBytes(bmp,false);
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    printText(command);
                    outputStream.flush();
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
                outputStream.flush();
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
                outputStream.flush();
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
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //print text
        private void printText(String msg) {
            try {
                // Print normal text
                outputStream.write(msg.getBytes());
                outputStream.flush();
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
            dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE)+":"+ c.get(Calendar.SECOND);
            return dateTime;
        }

        //recibe los datos, tamaño y alineacion 0:izq, 1:cnt, 2:der
        private void printQr(String data, int size, int align){
            try {
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
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            printText(Utils.QRCodeDataToBytes(data, size));
        }
    }

}