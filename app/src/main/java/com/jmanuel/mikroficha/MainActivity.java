package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
/*
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
*/
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.bumptech.glide.Glide;

import android.Manifest;
import android.app.NotificationManager;
import android.provider.Settings;
//import io.grpc.util.AdvancedTlsX509KeyManager;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private CardView btn_router;
    private CardView btn_config;
    private CardView btn_planes;
    private CardView btn_web;
    private CardView btn_youtube;

    private CardView btn_utileria;
    private CardView btn_chatbot;

    private CardView btn_temply;

    private CardView btn_reward_bono;
    private ImageCarousel carousel;
    int version_app;
    private int mostrarBoton;
    private boolean mostrar_mikrobot;
    SharedPreferences prefences,admob_preference;
    SharedPreferences.Editor editor;
    String uuid_app;
    private DatabaseReference mDatabase;
    String fecha = "-";
    String date = "";
    private BillingClient billingClient;
    private Map<String, Object> datos;

    //=====precios p2=====
    private int p2mf = 0;
    private int p2mp = 0;
    private int p2mr = 0;

    //=====precios p3=====
    private int p3mf = 0;
    private int p3mp = 0;
    private int p3mr = 0;
    private boolean scf = true;

    private AdView adview;
    private Boolean ADMOB = true;
    private Boolean MIKROBOT_ON = false;
    private SimpleDateFormat dateFormat;

    private String proxima_renov = "No cuenta con una suscripción", plansub_c = "";

    //_____esto es para comprobar fecha de eujecuion de funcion por dia_____________
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    private String currentDate = sdf.format(new Date());
    private String lastRunDate ="";
    private String lastRenewDate ="";
    private String orderIDSub ="";
    private String TplanSub = "";
    //_____________________________________________

    private static final int REQUEST_PERMISSION_SETTINGS = 3;
    private static final long MAX_LOGO_SIZE = 300 * 1024; // 300KB en bytes

    private static final int REQUEST_CODE_UPDATE = 1234;

    private Boolean REWARDS_BUTTON_ENABLED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar y pedir permiso para notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = dateFormat.format(new Date());
        prefences = MainActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        editor = prefences.edit();
        uuid_app = prefences.getString("uuid_app","N/A");
        admob_preference = MainActivity.this.getSharedPreferences("clave_uuid_app",Context.MODE_PRIVATE);


        adview = findViewById(R.id.adView);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        actualizarEstadoAdmobFinal();
        ADMOB = admob_preference.getBoolean("ADMOB",true);

        MIKROBOT_ON = admob_preference.getBoolean("MIKROBOT",false);
        REWARDS_BUTTON_ENABLED = prefences.getBoolean("REWARDS_BUTTON", false);

        if(uuid_app.equals("N/A")) {
            editor.putString("uuid_app", generarRandomUUID());
            editor.putBoolean("ADMOB", true);
            editor.apply();
        }
        datos = new HashMap<>();

        PackageInfo pi = null;
        try {
            pi = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version_app = pi.versionCode;
        String version_name = pi.versionName;
        mostrarBoton = 1;
        mostrar_mikrobot = false;

        btn_router = findViewById(R.id.btn_ver_router);
        carousel = findViewById(R.id.carousel);
        btn_planes = findViewById(R.id.btn_planes_main);
        btn_config = findViewById(R.id.btn_config_main);
        btn_web = findViewById(R.id.btn_web_main);
        btn_youtube = findViewById(R.id.btn_youtube_main);
        btn_utileria = findViewById(R.id.btn_utileria_main);
        btn_chatbot = findViewById(R.id.btn_gpt);
        btn_temply = findViewById(R.id.btn_temply);
        btn_reward_bono = findViewById(R.id.btn_reward_bono);

        if (adview != null && ADMOB) {
            adview.setVisibility(View.VISIBLE);
            MobileAds.initialize(this, initializationStatus -> {});
            AdRequest adRequest = new AdRequest.Builder().build();
            adview.loadAd(adRequest);
        } else if (adview != null) {
            adview.setVisibility(View.GONE);
        }

        if(MIKROBOT_ON || mostrar_mikrobot) {
            btn_chatbot.setVisibility(View.VISIBLE);
        }
        else {
            btn_chatbot.setVisibility(View.GONE);
        }

        if (REWARDS_BUTTON_ENABLED) {
            btn_reward_bono.setVisibility(View.VISIBLE);
        } else {
            btn_reward_bono.setVisibility(View.GONE);
        }


        btn_router.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               startActivity(new Intent(MainActivity.this,Router.class));
                finish();
                /*Toast.makeText(MainActivity.this,"Beta V: 1.2 Acacia, Sin funciones... visita" +
                        " Facebook.com/mikroficha para más información Gracias.", Toast.LENGTH_LONG).show();*/
            }
        });

        btn_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prefences = MainActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                uuid_app = prefences.getString("uuid_app", "N/A");

                // Asegúrate que estas variables estén definidas antes (ya lo están)
                // version_name, version_app, proxima_renov

                TextView mensaje = new TextView(MainActivity.this);
                mensaje.setText(Html.fromHtml(
                        "¡Hola!, ¿Cómo estás? Espero que mi app te ayude mucho. Gracias por formar parte de la comunidad MikroFicha.<br><br>" +
                                "<b>Autor:</b> J. Manuel<br>" +
                                "<b>Contacto:</b> <a href='https://facebook.com/mikroficha'>facebook.com/mikroficha</a><br>" +
                                "<b>E-mail:</b> <a href='mailto:hola@mikroficha.com'>hola@mikroficha.com</a><br>" +
                                "<b>Web:</b> <a href='https://mikroficha.com'>mikroficha.com</a><br>" +
                                "<b>Telegram:</b> <a href='https://t.me/Mikroficha'>t.me/Mikroficha</a><br><br>" +
                                "\uD83D\uDD11 <b>UUID_APP:</b> " + uuid_app + "<br><br>" +
                                proxima_renov.replace("\n", "<br>") + "<br><br>" +
                                "<b>Ver:</b> " + version_name + "/" + version_app
                ));
                mensaje.setMovementMethod(LinkMovementMethod.getInstance());
                mensaje.setTextIsSelectable(true);
                mensaje.setPadding(40, 40, 40, 40);
                mensaje.setTextSize(16);

// Cambiar color de texto según tema
                int colorTexto;
                int colorLink;
                if ((getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                        == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    colorTexto = getResources().getColor(android.R.color.white, getTheme());
                    colorLink = getResources().getColor(android.R.color.holo_blue_light, getTheme());
                } else {
                    colorTexto = getResources().getColor(android.R.color.black, getTheme());
                    colorLink = getResources().getColor(android.R.color.holo_blue_dark, getTheme());
                }

                mensaje.setTextColor(colorTexto);
                mensaje.setLinkTextColor(colorLink);

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                builder.setView(mensaje)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Calificar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri url = Uri.parse("https://play.google.com/store/apps/details?id=com.jmanuel.mikroficha");
                                startActivity(new Intent(Intent.ACTION_VIEW, url));
                                dialogInterface.dismiss();
                            }
                        });

                androidx.appcompat.app.AlertDialog titulo = builder.create();
                titulo.setTitle("¡Hola amig@!");
                titulo.show();
            }
        });

        btn_planes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, Precios.class));
                finish();
            }
        });

        btn_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uriURL = Uri.parse("https://mikroficha.com");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriURL);
                startActivity(launchBrowser);
            }
        });

        btn_youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uriURL = Uri.parse("https://youtube.com/@mikrotikutility");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriURL);
                startActivity(launchBrowser);
            }
        });

        btn_utileria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uriURL = Uri.parse("https://mikrotikutility.com");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriURL);
                startActivity(launchBrowser);
            }
        });

        btn_chatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, activity_chat.class));
                finish();
            }
        });

        btn_temply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, TemplateDesignerActivity.class));
                finish();
            }
        });

        btn_reward_bono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, RewardsActivity.class));
                finish();
            }
        });

        mDatabase.child("PRECIOS").child("P2").child("CONF").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                p2mf = Integer.parseInt(snapshot.getValue().toString().split(":")[0]);
                p2mp = Integer.parseInt(snapshot.getValue().toString().split(":")[1]);
                p2mr = Integer.parseInt(snapshot.getValue().toString().split(":")[2]);

                mDatabase.child("PRECIOS").child("P3").child("CONF").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        p3mf = Integer.parseInt(snapshot.getValue().toString().split(":")[0]);
                        p3mp = Integer.parseInt(snapshot.getValue().toString().split(":")[1]);
                        p3mr = Integer.parseInt(snapshot.getValue().toString().split(":")[2]);
                        if(Integer.parseInt(snapshot.getValue().toString().split(":")[3]) == 1)
                            scf = true;
                        else
                            scf = false;

                        // Obtén el objeto SharedPreferences
                        SharedPreferences sharedPreferences_dai = getSharedPreferences("MyAppPreferencesSubs", Context.MODE_PRIVATE);
                        lastRunDate = sharedPreferences_dai.getString("lastRunDate", "");
                        lastRenewDate = sharedPreferences_dai.getString("lastRenewDate", "");
                        orderIDSub = sharedPreferences_dai.getString("orderIDSub", "");
                        TplanSub = sharedPreferences_dai.getString("planSub", "");
                        Log.d("testOffer fecha ultima", lastRunDate);
                        Log.d("testOffer plax1", TplanSub);

                        checkSubcripcion();

                        /*if (!currentDate.equals(lastRunDate)) {
                            // Guarda la fecha actual en SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                            editor.putString("lastRunDate", currentDate);
                            editor.apply();

                            // Ejecuta la función


                        }*/
                        proxima_renov ="\nRenovacion: " +lastRenewDate+" \uD83D\uDD04\n"+orderIDSub+"\n\nPlan: "+TplanSub;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error al leer datos de P3", error.toException());

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer datos de P2", error.toException());

            }
        });


        carousel.registerLifecycle(getLifecycle());
        carousel.setAutoPlay(true);
        carousel.setAutoPlayDelay(10000);
        carousel.setInfiniteCarousel(true);
        List<CarouselItem> imgcarousel = new ArrayList<>();

        mDatabase.child("CARROUSEL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imgcarousel.clear();
                for(DataSnapshot sn: snapshot.getChildren()){
                    if(sn.child("URL").exists() && sn.child("LABEL").exists()) {
                        imgcarousel.add(new CarouselItem(sn.child("URL").getValue().toString(),
                                sn.child("LABEL").getValue().toString()));
                    }
                }
                carousel.setData(imgcarousel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Verificar si el usuario tiene desactivadas las notificaciones manualmente
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!notificationManager.areNotificationsEnabled()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Activar notificaciones")
                        .setMessage("Para mantenerte informado, activa las notificaciones de esta app.")
                        .setPositiveButton("Ir a ajustes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        }


        verificarTodasLasActualizaciones();
        if (BuildConfig.DEBUG) {
            sincronizarConfiguracionesFirebase();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_tyc,menu);
        MenuItem profileItem = menu.findItem(R.id.profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Uri photoUri = user.getPhotoUrl();

            Glide.with(this)
                    .asBitmap()
                    .load(photoUri)
                    .circleCrop()
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull android.graphics.Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                            profileItem.setIcon(new android.graphics.drawable.BitmapDrawable(getResources(), resource));
                        }

                        @Override
                        public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) { }
                    });
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            View view = findViewById(R.id.profile);
            showPopupMenu(view);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(Menu.NONE, 1, 1, "\uD83D\uDCDC T y C de uso");
        popup.getMenu().add(Menu.NONE, 2, 2, "\uD83D\uDD12 Cerrar sesión");
        popup.getMenu().add(Menu.NONE, 3, 3, "\uD83D\uDCF5 Eliminar cuenta");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    startActivity(new Intent(this, TerConPoli.class));
                    return true;
                case 2:
                    cerrarSesion();
                    return true;
                case 3:
                    confirmarEliminacionCuenta();
                    return true;
            }
            return false;
        });

        popup.show();
    }

    private void confirmarEliminacionCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar cuenta?")
                .setMessage("Esta acción eliminará tu cuenta de la App Mikroficha y no podrás recuperar tu acceso a menos que inicies sesión de nuevo con Google. ¿Deseas continuar?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCuentaFirebase())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuentaFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tu cuenta fue eliminada", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, activity_login.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "No se pudo eliminar la cuenta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void configurarRemoteConfig(FirebaseRemoteConfig remoteConfig) {
        long interval = BuildConfig.DEBUG ? 5 : 3600;
        FirebaseRemoteConfigSettings frconf = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(frconf);
    }




    private void cerrarSesion() {
        // Cerrar sesión Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

        // Limpiar preferencias si es necesario
        SharedPreferences.Editor editor = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Ir al login
        Intent intent = new Intent(MainActivity.this, activity_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.d("Actualización", "El usuario canceló la actualización.");
                // Aquí podrías volver a preguntar más tarde o mostrar un mensaje
            }
        }
    }

    private void actualizarEstadoAdmobFinal() {
        final boolean[] tieneSuscripcion = {false};

        // Paso 1: verificar suscripción primero
        SharedPreferences prefs = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        boolean admobPorSubs = !prefs.getBoolean("ADMOB", true);

        if (admobPorSubs) {
            tieneSuscripcion[0] = true;
            // ya tiene suscripción → no mostrar anuncios
            ADMOB = false;
            adview.setVisibility(View.GONE);
            return;
        }

        // Paso 2: si no hay suscripción, revisar adsFreeUntil
        mDatabase.child("REWARDS").child(uuid_app).child("adsFreeUntil")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean showAds = true;
                        if (snapshot.exists()) {
                            long now = System.currentTimeMillis();
                            long adsFreeUntil = snapshot.getValue(Long.class);
                            showAds = now > adsFreeUntil;
                        }

                        ADMOB = showAds;
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("ADMOB", showAds);
                        editor.apply();

                        adview.setVisibility(showAds ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error al leer adsFreeUntil", error.toException());
                    }
                });
    }

    private void verificarTodasLasActualizaciones() {
        // Primero, verificamos actualizaciones con Google Play
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnCompleteListener(task -> {
            try {
                AppUpdateInfo appUpdateInfo = task.getResult();
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Si hay una actualización disponible, la iniciamos
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            REQUEST_CODE_UPDATE
                    );
                } else {
                    // Si no hay actualización de Google Play o hubo un error, continuamos con Firebase
                    sincronizarConfiguracionesFirebase();
                }
            } catch (Exception e) {
                Log.e("Actualización", "Error al verificar actualizaciones con Google Play", e);
                // Si falla, continuamos con Firebase
                sincronizarConfiguracionesFirebase();
            }
        });
    }

    private void sincronizarConfiguracionesFirebase() {
        // Configurar Firebase Remote Config
        long interval = BuildConfig.DEBUG ? 5 : 3600;
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        // Establecer valores por defecto
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("versioncode", version_app);
        defaults.put("mostrarbotonentrar", mostrarBoton);
        defaults.put("mostrarmikrobot", mostrar_mikrobot);
        defaults.put("admob", true);
        defaults.put("botonmikrobot", true);
        defaults.put("rewards_button_enabled", false);
        remoteConfig.setDefaultsAsync(defaults);

        // Obtener configuraciones actualizadas
        remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Actualizar configuraciones de usuario
                actualizarConfiguracionesUsuario(remoteConfig);

                // Verificar si hay una actualización de versión disponible
                verificarVersionApp(remoteConfig);
            } else {
                Log.e("Firebase", "Error al obtener configuración remota", task.getException());
            }
        });
    }

    private void actualizarConfiguracionesUsuario(FirebaseRemoteConfig remoteConfig) {
        // Guardar configuraciones globales
        prefences = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        editor = prefences.edit();

        // Guarda el valor de botonmikrobot en las preferencias
        boolean remoteMikrobotValue = remoteConfig.getBoolean("botonmikrobot");
        Log.d("MikrobotDebug", "Valor remoto de botonmikrobot: " + remoteMikrobotValue);
        editor.putBoolean("MIKROBOT", remoteMikrobotValue);
        //editor.putBoolean("ADMOB", remoteConfig.getBoolean("admob"));
        // Guarda el valor de rewards_button_enabled en las preferencias
        boolean rewardsButtonValue = remoteConfig.getBoolean("rewards_button_enabled");
        Log.d("RewardsDebug", "Valor remoto de rewards_button_enabled: " + rewardsButtonValue);
        editor.putBoolean("REWARDS_BUTTON", rewardsButtonValue);

        editor.apply();

        // Actualiza las variables en memoria inmediatamente
        MIKROBOT_ON = remoteMikrobotValue;
        REWARDS_BUTTON_ENABLED = rewardsButtonValue;
        ADMOB = remoteConfig.getBoolean("admob");

        // Aplica las configuraciones a la UI inmediatamente con los valores predeterminados
        aplicarConfiguracionesUI();

        // Verificar configuraciones específicas del usuario
        mDatabase.child("UUID_APP").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean configurationChanged = false;

                if (snapshot.exists() && snapshot.child(uuid_app).exists()) {
                    /*if (snapshot.child(uuid_app).child("ADMOB").exists()) {
                        ADMOB = (Boolean) snapshot.child(uuid_app).child("ADMOB").getValue();
                        editor.putBoolean("ADMOB", ADMOB);
                        configurationChanged = true;
                    }*/

                    if (snapshot.child(uuid_app).child("MIKROBOT").exists()) {
                        Boolean mikrobotValue = (Boolean) snapshot.child(uuid_app).child("MIKROBOT").getValue();
                        Log.d("MikrobotDebug", "Valor específico de usuario para MIKROBOT: " + mikrobotValue);
                        MIKROBOT_ON = mikrobotValue;
                        editor.putBoolean("MIKROBOT", MIKROBOT_ON);
                        configurationChanged = true;
                    }

                    if (configurationChanged) {
                        editor.apply();
                        // Aplicar configuraciones a la UI de nuevo si cambiaron
                        runOnUiThread(() -> aplicarConfiguracionesUI());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer configuraciones de usuario", error.toException());
            }
        });
    }

    private void aplicarConfiguracionesUI() {
        Log.d("MikrobotDebug", "Aplicando configuraciones UI: MIKROBOT_ON=" + MIKROBOT_ON +
                ", mostrar_mikrobot=" + mostrar_mikrobot);

        // Actualizar visibilidad de AdMob basado en configuraciones actualizadas
        if (adview != null) {
            adview.setVisibility(ADMOB ? View.VISIBLE : View.GONE);
        }

        // Actualizar visibilidad del botón mikrobot
        if (MIKROBOT_ON || mostrar_mikrobot) {
            Log.d("MikrobotDebug", "Mostrando botón chatbot");
            btn_chatbot.setVisibility(View.VISIBLE);
        } else {
            Log.d("MikrobotDebug", "Ocultando botón chatbot");
            btn_chatbot.setVisibility(View.GONE);
        }

        // Actualizar visibilidad del botón de recompensas
        if (REWARDS_BUTTON_ENABLED) {
            Log.d("RewardsDebug", "Mostrando botón de recompensas");
            btn_reward_bono.setVisibility(View.VISIBLE);
        } else {
            Log.d("RewardsDebug", "Ocultando botón de recompensas");
            btn_reward_bono.setVisibility(View.GONE);
        }
    }

    private void verificarVersionApp(FirebaseRemoteConfig remoteConfig) {
        // Verificar si la Activity ya está finalizada o en proceso de destrucción
        if (isFinishing() || isDestroyed()) {
            return;
        }

        String nversion = remoteConfig.getString("versioncode");
        String nuevobotonmostrar = remoteConfig.getString("mostrarbotonentrar");

        if (nuevobotonmostrar.equals("")) {
            nuevobotonmostrar = "1";
        } else {
            if (Integer.parseInt(nuevobotonmostrar) == 1) {
                if (version_app < Integer.parseInt(nversion)) {
                    // Mostrar diálogo de actualización
                    mostrarDialogoActualizacion();
                }
            } else {
                btn_router.setVisibility(View.GONE);
            }
        }
    }

    private void mostrarDialogoActualizacion() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Actualiza la app y no te pierdas las nuevas funcionalidades")
                .setCancelable(false)
                .setPositiveButton("Actualizar", (dialogInterface, i) -> {
                    Uri url = Uri.parse("https://play.google.com/store/apps/details?id=com.jmanuel.mikroficha");
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                    dialogInterface.dismiss();
                });
        androidx.appcompat.app.AlertDialog titulo = builder.create();
        titulo.setTitle("¡Hay una nueva actualización!");
        titulo.show();
    }

    public String generarRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public String generarRandomString(int legth){
        String CHAR_LOWER = "abcdfghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;

        SecureRandom random = new SecureRandom();
        if(legth < 1 ) throw  new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(legth);
        for( int i = 0; i < legth; i++){
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }

        return sb.toString();
    }

    private void checkSubcripcion(){
        // Al inicio del método checkSubcripcion()
        final boolean[] subscriptionActive = {false};

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                Log.d("testOffer", "Servicio de facturación desconectado");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                            (billingResult1, list) -> {
                                if(billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                    Calendar calendar = Calendar.getInstance();
                                    Log.d("testOffer", list.size() +" elementos");
                                    String planSub = "", ordeID = "";
                                    long purchaseTime = 0;
                                    String purchaseToken = "";
                                    String packageName = "";
                                    String orderId = "";

                                    if(list.size() > 0){
                                        subscriptionActive[0] = true;
                                        //si tiene mas de una subscripcion verificar cual tiene
                                        int i = 0;
                                        for(Purchase purchase: list){
                                            // Supongamos que el JSON lo obtienes de purchase.getOriginalJson()
                                            String jsonString = purchase.getOriginalJson();

                                            try {
                                                // Crear un objeto JSONObject a partir de la cadena JSON
                                                JSONObject jsonObject = new JSONObject(jsonString);
                                                // Obtener elementos individuales del JSON
                                                orderId = jsonObject.getString("orderId");
                                                packageName = jsonObject.getString("packageName");
                                                purchaseTime = jsonObject.getLong("purchaseTime");
                                                purchaseToken = jsonObject.getString("purchaseToken");

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            planSub = purchase.getProducts().get(i).toString();
                                            ordeID = purchase.getOrderId();
                                            Log.d("testOffer", "ID: " + ordeID);

                                            i++;

                                            if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                                if (planSub.equals("mensual_01")) {//preguntamos cual ha contratado
                                                    //calendar.add(calendar.MONTH, 1);
                                                    Retrofit retrofit = null;
                                                    try {
                                                        retrofit = new Retrofit.Builder()
                                                                .baseUrl("https://us-central1-mikroficha.cloudfunctions.net/getSubscriptionDetails/")
                                                                .addConverterFactory(GsonConverterFactory.create())
                                                                .build();
                                                    }catch(Exception e){Log.d("testOffer exception", "Ex"+e.getMessage());}

                                                    ApiService apiService = retrofit.create(ApiService.class);
                                                    Call<SubscriptionDetails> call = apiService.getSubscriptionDetails(planSub, purchaseToken);
                                                    call.enqueue(new Callback<SubscriptionDetails>() {
                                                        @Override
                                                        public void onResponse(Call<SubscriptionDetails> call, Response<SubscriptionDetails> response) {
                                                            if (response.isSuccessful()) {
                                                                SubscriptionDetails details = response.body();
                                                                // Maneja los detalles de la suscripción aquí
                                                                if (details != null) {
                                                                    // Obtener el valor de expiryTimeMillis
                                                                    long expiryTimeMillis = details.getExpiryTimeMillis();

                                                                    // Convertir expiryTimeMillis a un objeto Date
                                                                    Date expiryDate = new Date(expiryTimeMillis);
                                                                    SimpleDateFormat dateexpires = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                                                    String formattedDate_ex = dateexpires.format(expiryDate);
                                                                    lastRenewDate = formattedDate_ex;
                                                                    TplanSub = "Mensual";

                                                                    SharedPreferences sharedPreferences_dai = getSharedPreferences("MyAppPreferencesSubs", Context.MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                                                                    editor.putString("lastRenewDate", formattedDate_ex);
                                                                    editor.putString("planSub", TplanSub);
                                                                    editor.apply();

                                                                    // Mostrar la fecha de expiración en logs o UI
                                                                    Log.d("testOffer 1Stails", "Expiry Date1: " + formattedDate_ex);
                                                                }
                                                            } else {
                                                                // Maneja errores de la respuesta aquí
                                                                Log.e("testOffer FirebaseFunction", "Error: " + response.code()+response.toString());
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<SubscriptionDetails> call, Throwable t) {
                                                            // Maneja fallos en la comunicación aquí
                                                            Log.e("testOffer FirebaseFunction", "Failed to connect to Firebase Function", t);
                                                        }
                                                    });

                                                    try {
                                                        calendar.setTime(dateFormat.parse(lastRenewDate)); //establecemos el calendario a esa fecha
                                                        calendar.add(calendar.MONTH, -1);
                                                    } catch (ParseException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                                else if (planSub.equals("anual_01")) {
                                                    //calendar.add(calendar.MONTH, 12);
                                                    Retrofit retrofit = new Retrofit.Builder()
                                                            .baseUrl("https://us-central1-mikroficha.cloudfunctions.net/getSubscriptionDetails/")
                                                            .addConverterFactory(GsonConverterFactory.create())
                                                            .build();

                                                    ApiService apiService = retrofit.create(ApiService.class);
                                                    Call<SubscriptionDetails> call = apiService.getSubscriptionDetails(planSub, purchaseToken);
                                                    call.enqueue(new Callback<SubscriptionDetails>() {
                                                        @Override
                                                        public void onResponse(Call<SubscriptionDetails> call, Response<SubscriptionDetails> response) {
                                                            if (response.isSuccessful()) {
                                                                SubscriptionDetails details = response.body();
                                                                // Maneja los detalles de la suscripción aquí
                                                                if (details != null) {
                                                                    // Obtener el valor de expiryTimeMillis
                                                                    long expiryTimeMillis = details.getExpiryTimeMillis();

                                                                    // Convertir expiryTimeMillis a un objeto Date
                                                                    Date expiryDate = new Date(expiryTimeMillis);
                                                                    SimpleDateFormat dateexpires = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                                                    String formattedDate_ex = dateexpires.format(expiryDate);
                                                                    lastRenewDate = formattedDate_ex;
                                                                    TplanSub = "Anual";

                                                                    SharedPreferences sharedPreferences_dai = getSharedPreferences("MyAppPreferencesSubs", Context.MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                                                                    editor.putString("lastRenewDate", formattedDate_ex);
                                                                    editor.putString("planSub", TplanSub);
                                                                    editor.apply();

                                                                    // Mostrar la fecha de expiración en logs o UI
                                                                    Log.d("testOffer 2Stails", "Expiry Date2: " + formattedDate_ex);
                                                                }
                                                            } else {
                                                                // Maneja errores de la respuesta aquí
                                                                Log.e("testOffer FirebaseFunction", "Error: " + response.code());
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<SubscriptionDetails> call, Throwable t) {
                                                            // Maneja fallos en la comunicación aquí
                                                            Log.e("testOffer FirebaseFunction", "Failed to connect to Firebase Function", t);
                                                        }
                                                    });

                                                    try {
                                                        calendar.setTime(dateFormat.parse(lastRenewDate)); //establecemos el calendario a esa fecha
                                                        calendar.add(calendar.MONTH, -12);
                                                    } catch (ParseException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }

                                                date = dateFormat.format(calendar.getTime());
                                                editor.putString("datePurchase", date + ":" + lastRenewDate);
                                                editor.apply();
                                                Log.d("testOffer", " <Fecha>: " + date + ":" + lastRenewDate);
                                                Log.d("testOffer", date + ":" + lastRenewDate+" -- Próxima renovación: " + lastRenewDate);
                                            }
                                        }

                                        String fechasusc = prefences.getString("datePurchase","2022-01-01:2022-01-02");
                                        SimpleDateFormat date_last_access = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Calendar c = Calendar.getInstance();
                                        Date currentTime = c.getTime();
                                        String last_access = date_last_access.format(currentTime);
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        if(planSub.equals("mensual_01")){
                                            plansub_c = "Mensual";
                                            datos.put("ADMOB", false);
                                            datos.put("BACKUP",true);
                                            datos.put("MIKROBOT",true);
                                            datos.put("ORDERID",ordeID);
                                            datos.put("COMENTARIO","Suscripción Mensual");
                                            datos.put("CORREO", "suscripcion_mediante_google_play");
                                            datos.put("FECHA",fechasusc);//fechaInit+":"+date);
                                            datos.put("MAX_FICHA",p2mf);
                                            datos.put("MAX_PLANES",p2mp);
                                            datos.put("MAX_ROUTER",p2mr);
                                            datos.put("ULT_ACCESO",last_access);

                                            if (user != null) {
                                                datos.put("CORREO", user.getEmail());
                                                datos.put("UID", user.getUid());
                                            } else {
                                                datos.put("CORREO", "sin_cuenta");
                                                datos.put("UID", "anónimo");
                                            }

                                            // Actualizar ADMOB inmediatamente
                                            prefences = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                                            editor = prefences.edit();
                                            editor.putBoolean("ADMOB", false);
                                            editor.apply();
                                            ADMOB = false;

                                            // Actualizar UI en el hilo principal
                                            runOnUiThread(() -> {
                                                if (adview != null) {
                                                    adview.setVisibility(View.GONE);
                                                }
                                            });

                                            Log.d("testOffer","entro en mensual");
                                            mDatabase.child("UUID_APP").child(uuid_app).setValue(datos);
                                        } else if(planSub.equals("anual_01")){
                                            plansub_c = "Anual";
                                            datos.put("ADMOB", false);
                                            datos.put("BACKUP",true);
                                            datos.put("MIKROBOT",true);
                                            datos.put("ORDERID",ordeID);
                                            datos.put("COMENTARIO","Suscripción Anual");
                                            datos.put("CORREO", "suscripcion_mediante_google_play");
                                            datos.put("FECHA",fechasusc);//fechaInit+":"+date);
                                            datos.put("MAX_FICHA",p3mf);
                                            datos.put("MAX_PLANES",p3mp);
                                            datos.put("MAX_ROUTER",p3mr);
                                            datos.put("SCRIPTFALLA",scf);
                                            datos.put("ULT_ACCESO",last_access);

                                            if (user != null) {
                                                datos.put("CORREO", user.getEmail());
                                                datos.put("UID", user.getUid());
                                            } else {
                                                datos.put("CORREO", "sin_cuenta");
                                                datos.put("UID", "anónimo");
                                            }

                                            // Actualizar ADMOB inmediatamente
                                            prefences = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                                            editor = prefences.edit();
                                            editor.putBoolean("ADMOB", false);
                                            editor.apply();
                                            ADMOB = false;

                                            // Actualizar UI en el hilo principal
                                            runOnUiThread(() -> {
                                                if (adview != null) {
                                                    adview.setVisibility(View.GONE);
                                                }
                                            });

                                            Log.d("testOffer","entro en anual");
                                            mDatabase.child("UUID_APP").child(uuid_app).setValue(datos);
                                        }

                                        SharedPreferences sharedPreferences_dai = getSharedPreferences("MyAppPreferencesSubs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                                        orderIDSub = ordeID;
                                        editor.putString("orderIDSub", orderIDSub);
                                        editor.apply();
                                        Log.d("testOffer plax2", TplanSub);

                                        proxima_renov ="\nRenovacion: " +lastRenewDate+" \uD83D\uDD04\n"+orderIDSub+"\n\nPlan: "+TplanSub;
                                    } else {
                                        SharedPreferences sharedPreferences_dai = getSharedPreferences("MyAppPreferencesSubs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                                        editor.putString("planSub", "No cuenta con una suscripción");
                                        editor.apply();
                                        //si no tiene subcripcion hacer algo....
                                        Log.d("testOffer", "sin suscripcion");
                                        DatabaseReference refDatabase = FirebaseDatabase.getInstance()
                                                .getReference()
                                                .child("UUID_APP")
                                                .child(uuid_app)
                                                .child("CS");
                                        refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    Log.d("testOffer", "Existe configuración especial, no se elimina");
                                                } else {
                                                    Log.d("testOffer", "No existe configuración especial, se elimina");
                                                    mDatabase.child("UUID_APP").child(uuid_app).removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e("testOffer", "Error al verificar CS: " + error.getMessage());
                                            }
                                        });
                                    }

                                    // Llamar al método para verificar la fecha en Firebase
                                    verificarFechaEnFirebase(subscriptionActive[0]);
                                }
                            }
                    );
                } else {
                    Log.e("testOffer", "Error en BillingClient setup: " + billingResult.getResponseCode());
                    // Si hay un error, llamamos igual a verificarFechaEnFirebase pero con false
                    verificarFechaEnFirebase(false);
                }
            }
        });
    }

    private void verificarFechaEnFirebase(final boolean tieneSubscripcionActiva) {

        // Primero actualizamos el estado de AdMob según la suscripción
        /*if (tieneSubscripcionActiva) {
            // Si tiene suscripción activa, ocultar anuncios
            prefences = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
            editor = prefences.edit();
            editor.putBoolean("ADMOB", false);
            editor.apply();
            ADMOB = false;

            runOnUiThread(() -> {
                if (adview != null) {
                    adview.setVisibility(View.GONE);
                }
            });
        } else {
            // Si no tiene suscripción, mostrar anuncios (a menos que tenga configuración especial)
            prefences = getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
            editor = prefences.edit();
            editor.putBoolean("ADMOB", true);
            editor.apply();
            ADMOB = true;

            runOnUiThread(() -> {
                if (adview != null) {
                    adview.setVisibility(View.VISIBLE);
                    if (adview.getAdSize() == null) { // Si no se ha cargado un anuncio todavía
                        AdRequest adRequest = new AdRequest.Builder().build();
                        adview.loadAd(adRequest);
                    }
                }
            });
        }*/


        mDatabase.child("UUID_APP").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).exists()){
                        if(snapshot.child(uuid_app).child("FECHA").exists()) {
                            fecha = snapshot.child(uuid_app).child("FECHA").getValue().toString().split(":")[1];
                            Log.d("testt", "Fecha expiración: " + fecha);

                            if(!fecha.equals("-")) {
                                try {
                                    Date fechaFinal = dateFormat.parse(fecha);
                                    Date fechaInicial = dateFormat.parse(date);
                                    int dif = (int) TimeUnit.DAYS.convert(fechaFinal.getTime() - fechaInicial.getTime(), TimeUnit.MILLISECONDS);
                                    Log.d("testt", "Días restantes: " + dif);

                                    // Solo mostrar mensajes si NO hay suscripción activa
                                    if(!tieneSubscripcionActiva) {
                                        if (dif <= 5 && dif >= 1) {
                                            Toast.makeText(MainActivity.this, "En " + dif + " días termina tu suscripción.", Toast.LENGTH_SHORT).show();
                                        } else if (dif <= 0) {
                                            Toast.makeText(MainActivity.this, "Renueva tu suscripción", Toast.LENGTH_LONG).show();

                                            // Verificar si hay registro de configuración especial (CS)
                                            DatabaseReference refDatabase = FirebaseDatabase.getInstance().getReference("UUID_APP/" + uuid_app + "/CS");
                                            Log.d("testt", "Verificando CS: " + refDatabase);

                                            refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    Log.d("testt", "Snapshot CS: " + snapshot);
                                                    if (!snapshot.exists()) {
                                                        Log.d("testt", "CS no existe, eliminando datos de usuario");
                                                        mDatabase.child("UUID_APP").child(uuid_app).removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e("testt", "Error al verificar CS: " + error.getMessage());
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d("testt", "Usuario tiene suscripción activa, no mostrando mensajes de renovación");
                                    }
                                } catch (ParseException e) {
                                    Log.e("testt", "Error al parsear fechas: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if(!tieneSubscripcionActiva) {
                                Toast.makeText(MainActivity.this, "Obtén más beneficios con una suscripción.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.d("testOffer", "UUID_APP no existe en la base de datos");
                    }
                } else {
                    Log.d("testOffer", "No hay datos en la ruta UUID_APP");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("testOffer", "Error al leer datos de Firebase: " + error.getMessage());
            }
        });
    }


}