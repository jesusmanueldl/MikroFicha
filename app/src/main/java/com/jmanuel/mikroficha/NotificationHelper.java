package com.jmanuel.mikroficha;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public final class NotificationHelper {

    private NotificationHelper() {}

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = context.getString(R.string.nombre_canal);
            String canalId = context.getString(R.string.canal_id);
            String descripcion = context.getString(R.string.canal_id_descrip);
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel canal = new NotificationChannel(canalId, name, importancia);
            canal.setDescription(descripcion);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    public static void notificarNuevaFicha(Context context) {
        String canalId = context.getString(R.string.canal_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Nueva ficha activada")
                .setContentText("Nuevo usuario conectado")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Nuevo usuario conectado"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}
