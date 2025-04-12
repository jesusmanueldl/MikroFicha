package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
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
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

    private static final int REQUEST_CODE_UPDATE = 1234;
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
        ADMOB = admob_preference.getBoolean("ADMOB",true);
        MIKROBOT_ON = admob_preference.getBoolean("MIKROBOT",false);

        if(uuid_app.equals("N/A")) {
            editor.putString("uuid_app", generarRandomUUID());
            editor.putBoolean("ADMOB", true);
            editor.apply();
        }
        datos = new HashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        adview = findViewById(R.id.adView);

        PackageInfo pi = null;
        try {
            pi = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version_app = pi.versionCode;
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

                prefences = MainActivity.this.getSharedPreferences("clave_uuid_app",Context.MODE_PRIVATE);
                uuid_app = prefences.getString("uuid_app","N/A");
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                builder.setMessage("¡Hola!, Cómo estas, espero que mi app te ayude mucho, gracias por formar parte de la comunidad MikroFicha.\n" +
                                "\nAutor: @ J. Manuel  \nContacto: facebook.com/mikroficha\nE-mail: hola@mikroficha.com\nWeb: mikroficha.com\nTelegram: t.me/Mikroficha\n\n"+
                                "\uD83D\uDD11 UUID_APP: "+ uuid_app+"\n\n"+proxima_renov+"\n\nVer:"+String.valueOf(BuildConfig.VERSION_NAME)+"/"+String.valueOf(BuildConfig.VERSION_CODE))
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

                        if (!currentDate.equals(lastRunDate+1)) {
                            // Guarda la fecha actual en SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                            editor.putString("lastRunDate", currentDate);
                            editor.apply();

                            // Ejecuta la función
                            checkSubcripcion();

                        }
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

        habilita_remoto_config();
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

        if(MIKROBOT_ON || mostrar_mikrobot) {
            btn_chatbot.setVisibility(View.VISIBLE);
        }
        else {
            btn_chatbot.setVisibility(View.GONE);
        }

        verificarActualizacionGooglePlay();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            verificarActualizacion();
        },5000);

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

    private void verificarActualizacionGooglePlay() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            REQUEST_CODE_UPDATE
                    );
                } catch (Exception e) {
                    Log.e("Actualización", "Error al iniciar la actualización", e);
                }
            }
        });
    }

    public void verificarActualizacion(){
        long interval = 3600;
        if(BuildConfig.DEBUG)
            interval = 5;

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings frconf = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(frconf);
        HashMap<String,Object> actualizacion = new HashMap<>();
        actualizacion.put("versioncode",version_app);
        actualizacion.put("mostrarbotonentrar",mostrarBoton);
        actualizacion.put("mostrarmikrobot",mostrar_mikrobot);

        remoteConfig.setDefaultsAsync(actualizacion);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        mostrarMensaje();
                    }
                });
    }

    public void habilita_remoto_config(){
        long interval = 3600;
        if(BuildConfig.DEBUG)
            interval = 5;

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings frconf = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(interval)
                .build();
        remoteConfig.setConfigSettingsAsync(frconf);
        HashMap<String,Object> actualizacion = new HashMap<>();
        actualizacion.put("ADMOB",true);
        actualizacion.put("MIKROBOT",true);

        remoteConfig.setDefaultsAsync(actualizacion);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        prefences = MainActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                        editor = prefences.edit();
                        editor.putBoolean("ADMOB", (Boolean) remoteConfig.getBoolean("admob"));
                        editor.putBoolean("MIKROBOT", (Boolean) remoteConfig.getBoolean("botonmikrobot"));
                        editor.apply();

                        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if(snapshot.child(uuid_app).child("ADMOB").exists()) {
                                        prefences = MainActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                                        editor = prefences.edit();
                                        editor.putBoolean("ADMOB", (Boolean) snapshot.child(uuid_app).child("ADMOB").getValue());
                                        editor.apply();
                                    }

                                    if(snapshot.child(uuid_app).child("MIKROBOT").exists()) {
                                        prefences = MainActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                                        editor = prefences.edit();
                                        editor.putBoolean("MIKROBOT", (Boolean) snapshot.child(uuid_app).child("MIKROBOT").getValue());
                                        editor.apply();
                                    }

                                    ADMOB = admob_preference.getBoolean("ADMOB",true);
                                    MIKROBOT_ON = admob_preference.getBoolean("MIKROBOT",false);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
    }

    private void mostrarMensaje() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        String nversion = remoteConfig.getString("versioncode");
        String nuevobotonmostrar = remoteConfig.getString("mostrarbotonentrar");
        boolean nuevobotonmikrobot = remoteConfig.getBoolean("botonmikrobot");
        if(nuevobotonmostrar.equals(""))
            nuevobotonmostrar = "1";
        else {
            if (Integer.parseInt(nuevobotonmostrar) == 1) {
                if (version_app < Integer.parseInt(nversion)) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Actualiza la app y no te pierdas las nuevas funcionalidades")
                            .setCancelable(false)
                            .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri url = Uri.parse("https://play.google.com/store/apps/details?id=com.jmanuel.mikroficha");
                                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                                    dialogInterface.dismiss();
                                }
                            });
                    androidx.appcompat.app.AlertDialog titulo = builder.create();
                    titulo.setTitle("¡Hay una nueva actulización!");
                    titulo.show();
                }
            } else {
                btn_router.setVisibility(View.GONE);
            }
        }

         if(nuevobotonmikrobot || MIKROBOT_ON)
             btn_chatbot.setVisibility(View.VISIBLE);

         else {
             btn_chatbot.setVisibility(View.GONE);
         }
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

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){

                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),(billingResult1, list) -> {
                                if(billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){

                                    Calendar calendar = Calendar.getInstance();
                                    Log.d("testOffer", list.size() +" elementos");
                                    String planSub = "", ordeID = "";
                                    long purchaseTime = 0;
                                    String purchaseToken = "";
                                    String packageName = "";
                                    String orderId = "";

                                    if(list.size() > 0){
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
                                                                .baseUrl("https://us-central1-mikroficha.cloudfunctions.net/getSubscriptionDetails/") // Asegúrate de reemplazar con tu URL
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

                                                                    // Puedes manejar la fecha de expiración como necesites
                                                                    // Por ejemplo, mostrarla en un TextView, compararla con la fecha actual, etc.
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
                                                            .baseUrl("https://us-central1-mikroficha.cloudfunctions.net/getSubscriptionDetails/") // Asegúrate de reemplazar con tu URL
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

                                                                    // Puedes manejar la fecha de expiración como necesites
                                                                    // Por ejemplo, mostrarla en un TextView, compararla con la fecha actual, etc.
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


                                            Log.d("testOffer","entro en mensual");
                                            mDatabase.child("UUID_APP").child(uuid_app).setValue(datos);

                                        }else if(planSub.equals("anual_01")){
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

                                    }else
                                    {

                                        SharedPreferences sharedPreferences_dai = getSharedPreferences("MyAppPreferencesSubs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences_dai.edit();
                                        editor.putString("planSub", "No cuenta con una suscripción");
                                        editor.apply();
                                        //si no tiene subcripcion hacer algo....
                                        Log.d("testOffer", "sin suscripcion");
                                        DatabaseReference refDatabase = FirebaseDatabase.getInstance().getReference("UUID_APP/"+uuid_app+"/CS");
                                        refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){

                                                }else{
                                                    mDatabase.child("UUID_APP").child(uuid_app).removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                }
                            }
                    );
                }

            }
        });
//addValueEventListener
        mDatabase.child("UUID_APP").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).exists()){
                        if(snapshot.child(uuid_app).child("FECHA").exists()) {
                            fecha = snapshot.child(uuid_app).child("FECHA").getValue().toString().split(":")[1];
                            Log.d("testt", fecha);
                            if(!fecha.equals("-")) {
                                try {
                                    Date fechaFinal = dateFormat.parse(fecha+"");
                                    Date fechaInicial = dateFormat.parse(date+"");
                                    int dif = (int) TimeUnit.DAYS.convert(fechaFinal.getTime() - fechaInicial.getTime(), TimeUnit.MILLISECONDS);// / 86400000;
                                    Log.d("testt", dif+"");
                                    if(dif <= 5 && dif >= 1)
                                        Toast.makeText(MainActivity.this,"En "+dif+" días termina tu suscripción." ,Toast.LENGTH_SHORT).show();
                                    else if(dif <= 0){
                                        Toast.makeText(MainActivity.this,"Renueva tu suscripción" ,Toast.LENGTH_LONG).show();

                                        DatabaseReference refDatabase = FirebaseDatabase.getInstance().getReference("UUID_APP/"+uuid_app+"/CS");
                                        Log.d("testt", refDatabase+"");
                                        refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Log.d("testt", snapshot+"");
                                                if(snapshot.exists()){
                                                    Log.d("testt", "zii");

                                                }else{
                                                    Log.d("testt",""+snapshot.exists() );
                                                    mDatabase.child("UUID_APP").child(uuid_app).removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this,"Obtén más veneficios con una suscripción.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Log.d("testOffer", "no esta el UUID_APP");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}