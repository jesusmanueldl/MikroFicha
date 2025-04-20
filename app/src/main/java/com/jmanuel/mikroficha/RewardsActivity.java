package com.jmanuel.mikroficha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.rewarded.*;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;          // <-- lo sigues usando para Firestore en otros lados
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class RewardsActivity extends AppCompatActivity {

    // UI
    private TextView tvPoints, tvDayStatus, tvWeekStatus, tvProgressLabel;
    private ProgressBar progress;
    private Button btnWatchVideo;

    // ① campo nuevo
    private TextView tvRemaining;

    // Constantes
    private static final int VIDEO_POINTS = 5;
    private static final int COST_DAY     = 50;
    private static final int COST_WEEK    = 200;
    private static final int DAILY_LIMIT  = 20;

    // En campos UI
    private ImageView ivMedal;
    private TextView  tvMedalLabel;
    private TextView  tv_level_title;

    // Firebase Realtime DB
    private DatabaseReference userRef =
            FirebaseDatabase.getInstance()
                    .getReference("REWARDS")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    // AdMob
    private RewardedAd rewardedAd;

    private TextView tvMedalSub;
    private ProgressBar pbMedal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        tvPoints  = findViewById(R.id.tv_points);
        tvDayStatus  = findViewById(R.id.tv_option_day_status);
        tvWeekStatus = findViewById(R.id.tv_option_week_status);
        tvProgressLabel = findViewById(R.id.tv_progress_label);
        progress  = findViewById(R.id.progress_points);
        btnWatchVideo = findViewById(R.id.btn_watch_video);
        tvRemaining = findViewById(R.id.tv_remaining_time);
        ivMedal     = findViewById(R.id.iv_medal);
        tvMedalLabel= findViewById(R.id.tv_medal_label);
        tv_level_title= findViewById(R.id.tv_level_title);
        tvMedalSub = findViewById(R.id.tv_medal_sub);
        pbMedal    = findViewById(R.id.pb_medal);

        /* Escucha RTDB */
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {

                long pts = snap.child("points").getValue(Long.class) == null ? 0
                        : snap.child("points").getValue(Long.class);
//*****************
                long count = snap.child("dailyEarnCount").getValue(Long.class) == null ? 0
                        : snap.child("dailyEarnCount").getValue(Long.class);
//*****************

                Long until = snap.child("adsFreeUntil").getValue(Long.class);
                long now   = System.currentTimeMillis();

                boolean hasReward = (until != null) && (until > now);
                long   millisLeft = hasReward ? (until - now) : 0;

               // updateUI((int) pts, hasReward, millisLeft);
                updateUI((int) pts, hasReward, millisLeft, (int) count);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        btnWatchVideo.setOnClickListener(v -> showRewardedAd());

        View.OnClickListener validarYCanjear = v -> {
            SharedPreferences prefs = getSharedPreferences("clave_uuid_app", MODE_PRIVATE);
            boolean tieneSubscrip = prefs.getBoolean("TIENE_SUBSCRIPCION", false);

            if (tieneSubscrip) {
                Toast.makeText(this, "Ya tienes una suscripción activa. No necesitas canjear puntos.", Toast.LENGTH_LONG).show();
                return;
            }

            // Verificación adicional en Firebase (opcional pero más seguro)
            String uuid_app = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UUID_APP").child(uuid_app).child("ADMOB");

            ref.get().addOnSuccessListener(snapshot -> {
                boolean tieneSubscripcion = snapshot.exists() && !snapshot.getValue(Boolean.class); // false = no mostrar anuncios

                if (tieneSubscripcion) {
                    Toast.makeText(this, "Ya tienes una suscripción activa. No puedes usar puntos.", Toast.LENGTH_LONG).show();
                } else {
                    if (v.getId() == R.id.tv_option_day) {
                        redeem(COST_DAY, TimeUnit.DAYS.toMillis(1));
                    } else {
                        redeem(COST_WEEK, TimeUnit.DAYS.toMillis(7));
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "No se pudo verificar la suscripción. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                Log.e("REWARD", "Error al verificar suscripción", e);
            });
        };

        findViewById(R.id.tv_option_day).setOnClickListener(validarYCanjear);
        findViewById(R.id.tv_option_week).setOnClickListener(validarYCanjear);

        loadRewarded();
    }

    /* ---------- UI ---------- */
    private void updateUI(int pts, boolean hasReward, long millisLeft, int dailyCount) {

        tvPoints.setText("Tus puntos: " + pts);

        /* ─ Estado de canje ─────────────────────── */
        if (hasReward) {
            tvDayStatus .setText("No disponible");
            tvWeekStatus.setText("No disponible");
            tvDayStatus .setTextColor(getColor(android.R.color.darker_gray));
            tvWeekStatus.setTextColor(getColor(android.R.color.darker_gray));
            // Deshabilita clics mientras haya recompensa activa
            findViewById(R.id.tv_option_day ).setClickable(false);
            findViewById(R.id.tv_option_week).setClickable(false);
        } else {
            tvDayStatus .setText(pts >= COST_DAY  ? "Disponible" : "Te faltan " + (COST_DAY  - pts) + " pts");
            tvWeekStatus.setText(pts >= COST_WEEK ? "Disponible" : "Te faltan " + (COST_WEEK - pts) + " pts");
            tvDayStatus .setTextColor(getColor(android.R.color.holo_green_dark));
            tvWeekStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            findViewById(R.id.tv_option_day ).setClickable(true);
            findViewById(R.id.tv_option_week).setClickable(true);
        }

        /* ─ Barra de progreso ───────────────────── */
        int nextGoal = pts < COST_DAY ? COST_DAY : (pts < COST_WEEK ? COST_WEEK : COST_WEEK);
        progress.setMax(nextGoal);
        progress.setProgress(pts);
        tvProgressLabel.setText(pts + "/" + nextGoal + " pts para próxima recompensa");

        /* ─ Tiempo restante ─────────────────────── */
        if (hasReward) {
            long hrs = TimeUnit.MILLISECONDS.toHours(millisLeft);
            long min = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60;
            tvRemaining.setVisibility(View.VISIBLE);
            tvRemaining.setText("Sin anuncios activo: " + hrs + " h " + min + " m restantes");
        } else {
            tvRemaining.setVisibility(View.GONE);
        }



        // --- Medalla ---
        int medalRes;  String medalName;
        if (pts >= 2000) { medalRes = R.drawable.ic_medal_diamond;  medalName = "Diamante"; }
        else if (pts >= 1000) { medalRes = R.drawable.ic_medal_platinum; medalName = "Platino"; }
        else if (pts >= 500) { medalRes = R.drawable.ic_medal_gold;   medalName = "Oro"; }
        else if (pts >= 200) { medalRes = R.drawable.ic_medal_silver; medalName = "Plata"; }
        else { medalRes = R.drawable.ic_medal_bronze; medalName = "Bronce"; }

        ivMedal.setImageResource(medalRes);
        tvMedalLabel.setText(medalName);
        tv_level_title.setText("\uD83C\uDFC6 Nivel actual: "+ medalName);

        // tabla de umbrales
        int nextThreshold;
        switch (medalName) {
            case "Bronce":   nextThreshold = 200;  break;
            case "Plata":    nextThreshold = 500;  break;
            case "Oro":      nextThreshold = 1000; break;
            case "Platino":  nextThreshold = 2000; break;
            default:         nextThreshold = -1;   // Diamante no tiene siguiente
        }

        if (nextThreshold > 0) {
            int faltan = nextThreshold - pts;
            tvMedalSub.setText("Te faltan " + faltan + " pts para " +
                    (medalName.equals("Platino") ? "Diamante" :            // siguiente nombre
                            medalName.equals("Oro") ? "Platino" :
                                    medalName.equals("Plata") ? "Oro" : "Plata"));

            pbMedal.setMax(nextThreshold -
                    (medalName.equals("Platino") ? 1000 :
                            medalName.equals("Oro")     ? 500  :
                                    medalName.equals("Plata")   ? 200  : 0));   // rango actual
            pbMedal.setProgress(pts -
                    (medalName.equals("Platino") ? 1000 :
                            medalName.equals("Oro")     ? 500  :
                                    medalName.equals("Plata")   ? 200  : 0));
            pbMedal.setVisibility(View.VISIBLE);
        } else {                    // Diamante
            tvMedalSub.setText("¡Rango máximo desbloqueado! Sigue sumando puntos para futuras recompensas.");
            pbMedal.setVisibility(View.GONE);
        }


    }


    /* ---------- AdMob ---------- */
    private void loadRewarded() {
        RewardedAd.load(this, getString(R.string.admob_rewarded_id),
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError e) {
                        rewardedAd = null;
                    }
                });
    }

    private void showRewardedAd() {
        userRef.child("dailyEarnCount").get().addOnSuccessListener(snap -> {
            long count = snap.getValue(Long.class) == null ? 0 : snap.getValue(Long.class);

            if (count >= DAILY_LIMIT) {
                Toast.makeText(this,
                        "Límite diario alcanzado (20 videos). Vuelve mañana 😉",
                        Toast.LENGTH_LONG).show();
                return;                       // no muestra el anuncio
            }

            if (rewardedAd == null) {
                Toast.makeText(this,"Anuncio no listo",Toast.LENGTH_SHORT).show();
                return;
            }
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override public void onAdShowedFullScreenContent() {
                    rewardedAd = null;
                    loadRewarded();
                }
            });
            rewardedAd.show(this, rewardItem -> addPointsIfAllowed());
        });

    }

    /* ---------- Puntos ---------- */
    /* ---------- Puntos ---------- */
    private void addPointsIfAllowed() {
        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override public Transaction.Result doTransaction(@NonNull MutableData data) {

                long pts   = data.child("points").getValue(Long.class)  == null ? 0
                        : data.child("points").getValue(Long.class);
                String last = data.child("lastEarnDate").getValue(String.class);
                long count  = data.child("dailyEarnCount").getValue(Long.class) == null ? 0
                        : data.child("dailyEarnCount").getValue(Long.class);

                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                if (!today.equals(last)) { last = today; count = 0; }   // nuevo día

                if (count >= DAILY_LIMIT) {                // 20 videos vistos
                    return Transaction.abort();            // ⚠️  abortar, no sumar
                }

                // sumar puntos
                pts += VIDEO_POINTS;  count++;

                data.child("points").setValue(pts);
                data.child("dailyEarnCount").setValue(count);
                data.child("lastEarnDate").setValue(last);

                return Transaction.success(data);
            }

            @Override public void onComplete(@Nullable DatabaseError e,
                                             boolean committed,
                                             @Nullable DataSnapshot snap) {

                if (!committed) {   // alcanzó el tope diario
                    Toast.makeText(RewardsActivity.this,
                            "Límite diario alcanzado (20 videos). Vuelve mañana 😉",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /* ---------- Canje ---------- */
    /* ---------- Canje ---------- */
    private void redeem(int cost, long millis) {
        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override public Transaction.Result doTransaction(@NonNull MutableData data) {

                // ➤ 1. si YA hay una recompensa activa → abortar
                Long until = data.child("adsFreeUntil").getValue(Long.class);
                if (until != null && until > System.currentTimeMillis()) {
                    return Transaction.abort();
                }

                // ➤ 2. comprobar puntos
                long pts = data.child("points").getValue(Long.class) == null ? 0
                        : data.child("points").getValue(Long.class);
                if (pts < cost) {                         // ←  ¡cambios aquí!
                    return Transaction.abort();           //  aborta, NO success
                }

                // ➤ 3. todo OK → canjear
                long nuevoUntil = System.currentTimeMillis() + millis;
                data.child("points").setValue(pts - cost);
                data.child("adsFreeUntil").setValue(nuevoUntil);

                return Transaction.success(data);
            }

            @Override public void onComplete(@Nullable DatabaseError e,
                                             boolean committed,
                                             @Nullable DataSnapshot snap) {

                if (!committed) {   // llegó aquí por falta de puntos o recompensa activa
                    long pts = (snap != null && snap.child("points").getValue(Long.class) != null)
                            ? snap.child("points").getValue(Long.class) : 0;

                    String msg = (pts < cost)
                            ? "Te faltan puntos para canjear esta recompensa."
                            : "Ya tienes un modo sin anuncios activo.";
                    Toast.makeText(RewardsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Canje exitoso → desactivar anuncios temporalmente
                SharedPreferences.Editor editor = getSharedPreferences("clave_uuid_app", MODE_PRIVATE).edit();
                editor.putBoolean("ADMOB", false);
                editor.apply();

                Toast.makeText(RewardsActivity.this,
                        "Recompensa canjeada 🎉", Toast.LENGTH_SHORT).show();
            }
        });
    }




    /* ---------- Helper para banners ---------- */
    public static boolean shouldShowAds(DataSnapshot userSnap) {
        Long until = userSnap.child("adsFreeUntil").getValue(Long.class);
        return until == null || until < System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RewardsActivity.this, MainActivity.class));
        finish();
    }
}
