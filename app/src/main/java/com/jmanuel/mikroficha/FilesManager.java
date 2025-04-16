package com.jmanuel.mikroficha;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class FilesManager {

    // Método para escribir los archivos de la plantilla y copiar los recursos.
    public static boolean writeTemplateFiles(Context context, Map<String, String> filesMap, String logoUri, String backgroundImageUri) {
        try {
            // Obtener la carpeta de Descargas
            File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            // Crear la subcarpeta "hostpot"
            File exportDir = new File(downloadsDir, "hostpot");
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                return false;
            }

            // Escribir cada archivo generado (HTML, CSS, etc.)
            for (Map.Entry<String, String> entry : filesMap.entrySet()) {
                File file = new File(exportDir, entry.getKey());
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(entry.getValue().getBytes("UTF-8"));
                fos.close();
            }

            // Copiar el logo (si existe)
            if (logoUri != null && !logoUri.isEmpty()) {
                Uri uri = Uri.parse(logoUri);
                InputStream is = context.getContentResolver().openInputStream(uri);
                if (is != null) {
                    File logoFile = new File(exportDir, "logo.png");
                    FileOutputStream logoFos = new FileOutputStream(logoFile);
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        logoFos.write(buffer, 0, len);
                    }
                    logoFos.close();
                    is.close();
                }
            }

            // Copiar la imagen de fondo (si está definida)
            if (backgroundImageUri != null && !backgroundImageUri.isEmpty()) {
                Uri bgUri = Uri.parse(backgroundImageUri);
                InputStream bgIs = context.getContentResolver().openInputStream(bgUri);
                if (bgIs != null) {
                    // Leer toda la imagen para obtener su tamaño
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = bgIs.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    bgIs.close();
                    byte[] bgBytes = baos.toByteArray();

                    // Escribir la imagen de fondo en el directorio con el nombre "background.png"
                    File bgFile = new File(exportDir, "background.png");
                    FileOutputStream bgFos = new FileOutputStream(bgFile);
                    bgFos.write(bgBytes);
                    bgFos.close();
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
