package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Precios extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private CardView cv1,cv2,cv3;
    private TextView titulo1, titulo2, titulo3,
            info1,info2,info3,costo1,costo2,costo3,
            condicion,mensaje, slogan1,slogan2,slogan3,precio_play1,precio_play2;
    private BillingClient billingClient;
    private Handler handler;
    private List<ProductDetails> productDetailsList;
    private Boolean planflag;
    private SharedPreferences prefences;
    private SharedPreferences.Editor editor;
    private String uuid_app;
    private Map<String, Object> datos;
    private ProgressBar progressBar;
    public String datePurchase ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precios);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        prefences = Precios.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        editor = prefences.edit();
        uuid_app = prefences.getString("uuid_app","N/A");
        datos = new HashMap<>();

        progressBar = findViewById(R.id.progressBar);

        cv1 = findViewById(R.id.card1);
        slogan1 = findViewById(R.id.slogan1);
        titulo1 = findViewById(R.id.titulo1);
        info1 = findViewById(R.id.info1);
        costo1 = findViewById(R.id.costo1);

        cv2 = findViewById(R.id.card2);
        slogan2 = findViewById(R.id.slogan2);
        titulo2 = findViewById(R.id.titulo2);
        info2 = findViewById(R.id.info2);
        precio_play1 = findViewById(R.id.costo_play1);
        costo2 = findViewById(R.id.costo2);

        cv3 = findViewById(R.id.card3);
        slogan3 = findViewById(R.id.slogan3);
        titulo3 = findViewById(R.id.titulo3);
        info3 = findViewById(R.id.info3);
        precio_play2 = findViewById(R.id.costo_play2);
        costo3 = findViewById(R.id.costo3);

        condicion = findViewById(R.id.condiciones);
        mensaje = findViewById(R.id.mensaje);


        //INTEGRACIOND E LAS COMPRAS
        planflag = false;
        productDetailsList = new ArrayList<>();
        handler = new Handler();

         billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            for (Purchase purchase : list) {
                                verifySubPurchase(purchase);
                            }
                        }
                    }
                }).build();

         establishConnection();

        //#########################


        mDatabase.child("PRECIOS").child("P1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cv1.setCardBackgroundColor(Color.parseColor(snapshot.child("COLOR").getValue().toString()));

                String cad = snapshot.child("INFO").getValue().toString();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    info1.setText(Html.fromHtml(cad, Html.FROM_HTML_MODE_LEGACY));
                    titulo1.setText(Html.fromHtml(snapshot.child("TITULO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                    slogan1.setText(Html.fromHtml(snapshot.child("SUBTITULO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                    costo1.setText(Html.fromHtml(snapshot.child("COSTO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                }
                else{
                    info1.setText(Html.fromHtml(cad));
                    titulo1.setText(Html.fromHtml(snapshot.child("TITULO").getValue().toString()));
                    slogan1.setText(Html.fromHtml(snapshot.child("SUBTITULO").getValue().toString()));
                    costo1.setText(Html.fromHtml(snapshot.child("COSTO").getValue().toString()));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("PRECIOS").child("P2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cv2.setCardBackgroundColor(Color.parseColor(snapshot.child("COLOR").getValue().toString()));
                String cad = snapshot.child("INFO").getValue().toString();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    info2.setText(Html.fromHtml(cad, Html.FROM_HTML_MODE_LEGACY));
                    titulo2.setText(Html.fromHtml(snapshot.child("TITULO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                    slogan2.setText(Html.fromHtml(snapshot.child("SUBTITULO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                    costo2.setText(Html.fromHtml(snapshot.child("COSTO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                }
                else{
                    info2.setText(Html.fromHtml(cad));
                    titulo2.setText(Html.fromHtml(snapshot.child("TITULO").getValue().toString()));
                    slogan2.setText(Html.fromHtml(snapshot.child("SUBTITULO").getValue().toString()));
                    costo2.setText(Html.fromHtml(snapshot.child("COSTO").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("PRECIOS").child("P3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cv3.setCardBackgroundColor(Color.parseColor(snapshot.child("COLOR").getValue().toString()));
                String cad = snapshot.child("INFO").getValue().toString();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    info3.setText(Html.fromHtml(cad, Html.FROM_HTML_MODE_LEGACY));
                    titulo3.setText(Html.fromHtml(snapshot.child("TITULO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                    slogan3.setText(Html.fromHtml(snapshot.child("SUBTITULO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                    costo3.setText(Html.fromHtml(snapshot.child("COSTO").getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                }
                else{
                    info3.setText(Html.fromHtml(cad));
                    titulo3.setText(Html.fromHtml(snapshot.child("TITULO").getValue().toString()));
                    slogan3.setText(Html.fromHtml(snapshot.child("SUBTITULO").getValue().toString()));
                    costo3.setText(Html.fromHtml(snapshot.child("COSTO").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("PRECIOS").child("CONDICIONES").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    condicion.setText(Html.fromHtml(snapshot.getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                }
                else{
                    condicion.setText(Html.fromHtml(snapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("PRECIOS").child("MENSAJE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    mensaje.setText(Html.fromHtml(snapshot.getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                }
                else{
                    mensaje.setText(Html.fromHtml(snapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //PAGO MENSUAL
        cv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                planflag = false;
                lauchPurchaseFlow(productDetailsList.get(1));
               /* Intent correo = new Intent(Intent.ACTION_SENDTO);
                correo.setData(Uri.parse("mailto:"));
                correo.putExtra(Intent.EXTRA_EMAIL, new String[]{"hola@mikroficha.com"});
                correo.putExtra(Intent.EXTRA_SUBJECT,"Me interesa el plan Mensual");
                correo.putExtra(Intent.EXTRA_TEXT,"Hola, me gustaría contratar el plan mensual, me proporciona más información por favor.");
                try{
                    startActivity(correo);
                }catch(ActivityNotFoundException e){

                }*/
            }
        });

        //PAGO ANUAL
        cv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                planflag = true;
                lauchPurchaseFlow(productDetailsList.get(0));
              /*  Intent correo = new Intent(Intent.ACTION_SENDTO);
                correo.setData(Uri.parse("mailto:"));
                correo.putExtra(Intent.EXTRA_EMAIL, new String[]{"hola@mikroficha.com"});
                correo.putExtra(Intent.EXTRA_SUBJECT,"Me interesa el plan Anual");
                correo.putExtra(Intent.EXTRA_TEXT,"Hola, me gustaría contratar el plan anual, me proporciona más información por favor.");
                try{
                    startActivity(correo);
                }catch(ActivityNotFoundException e){

                }*/

            }
        });


    }

    private void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                establishConnection();
                Log.d("testOffer", "Estableciendo conexion");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    showProducts();
                    Log.d("testOffer", "ver productos");
                }
            }
        });
    }
@SuppressLint("SetTextI18n")
    private void showProducts() {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                //producto 1
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("mensual_01")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                //producto 2
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("anual_01")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();
        Log.d("testOffer","Cargando productos");

        billingClient.queryProductDetailsAsync(
                params, (billingResult, list) -> {
                    handler.postDelayed(() -> {
                        productDetailsList.clear();

                        if (list == null || list.isEmpty()) {
                            Log.e("testOffer", "No se cargaron productos de suscripción.");
                            Toast.makeText(this, "No se encontraron productos de suscripción", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        ProductDetails productDetailsAnual = null;
                        ProductDetails productDetailsMensual = null;

                        for (ProductDetails pd : list) {
                            if (pd.getProductId().equals("anual_01")) {
                                productDetailsAnual = pd;
                            } else if (pd.getProductId().equals("mensual_01")) {
                                productDetailsMensual = pd;
                            }
                            productDetailsList.add(pd);
                        }

                        if (productDetailsMensual != null && productDetailsMensual.getSubscriptionOfferDetails() != null) {
                            precio_play1.setText(productDetailsMensual
                                    .getSubscriptionOfferDetails().get(0)
                                    .getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
                        } else {
                            precio_play1.setText("No disponible");
                            Log.w("testOffer", "Detalles del producto mensual no encontrados o incompletos.");
                        }

                        if (productDetailsAnual != null && productDetailsAnual.getSubscriptionOfferDetails() != null) {
                            precio_play2.setText(productDetailsAnual
                                    .getSubscriptionOfferDetails().get(0)
                                    .getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
                        } else {
                            precio_play2.setText("No disponible");
                            Log.w("testOffer", "Detalles del producto anual no encontrados o incompletos.");
                        }

                        progressBar.setVisibility(View.GONE);
                        cv1.setVisibility(View.VISIBLE);
                        cv2.setVisibility(View.VISIBLE);
                        cv3.setVisibility(View.VISIBLE);
                        condicion.setVisibility(View.VISIBLE);
                        mensaje.setVisibility(View.VISIBLE);

                    }, 2000);
                }
        );
/*

    billingClient.queryProductDetailsAsync(
                params,(billingResult, list) -> {
                    //Prcesar el resultado
                    productDetailsList.clear();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("testOffer","Cargamos los productos total: "+ list.size());
                            if (list != null && list.size() >= 2) {
                                productDetailsList.addAll(list);

                                //optenemos el primer producto
                                ProductDetails productDetails_anual = list.get(0);
                                ProductDetails productDetails_mensual = list.get(1);

                                //optenemos el detalle del producto
                                precio_play1.setText(productDetails_mensual.getSubscriptionOfferDetails().get(0)
                                        .getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
                                precio_play2.setText(productDetails_anual.getSubscriptionOfferDetails().get(0)
                                        .getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());


                                //aqui ponemos el precio a mostrar
                               // Log.d("testOffer","--"+productName);
                                progressBar.setVisibility(View.GONE);
                                cv1.setVisibility(View.VISIBLE);
                                cv2.setVisibility(View.VISIBLE);
                                cv3.setVisibility(View.VISIBLE);
                                condicion.setVisibility(View.VISIBLE);
                                mensaje.setVisibility(View.VISIBLE);
                            } else {
                                Log.e("testOffer", "No se cargaron correctamente los productos. Lista vacía o incompleta");
                                Toast.makeText(Precios.this, "Error al cargar los productos de suscripción", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }, 2000);
                }
        );*/
    }

    private void lauchPurchaseFlow(ProductDetails productDetails){
        Log.d("testOffer","empieza el flujo de compra");
        assert productDetails.getSubscriptionOfferDetails() != null;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
    }

    private void verifySubPurchase(Purchase purchases){

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                Log.d("testOffer","Valido");
                String date = "";
                String  date2= "";
                Calendar c = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                date = dateFormat.format(c.getTime());

               if(planflag)
                    c.add(Calendar.MONTH, 12);
                else
                    c.add(Calendar.MONTH, 1);
                date2 = dateFormat.format(c.getTime());
                Log.d("testOffer", "periodo: "+date+":"+date2);
                Log.d("testOffer", "plangflag: "+planflag);

                editor.putString("datePurchase", date+":"+date2);
                editor.apply();
                Toast.makeText(Precios.this,"Suscripcion activada!", Toast.LENGTH_LONG).show();

            }
        });


        Log.d("testOffer", "Token: "+ purchases.getPurchaseToken());
        Log.d("testOffer", "Time: "+ purchases.getPurchaseTime());
        Log.d("testOffer", "ORderID: "+ purchases.getOrderId());
    }


    @Override
    protected void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResult, list) -> {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        for(Purchase purchase: list){
                            if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()){
                                verifySubPurchase(purchase);
                            }
                        }
                    }
                }
        );

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Precios.this, MainActivity.class));
        finish();
    }
}