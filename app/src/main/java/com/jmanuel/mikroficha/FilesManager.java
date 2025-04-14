package com.jmanuel.mikroficha;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class FilesManager {

    // Escribe los archivos de la plantilla en la carpeta de Descargas en una subcarpeta llamada "hostpot"
    // y copia la imagen del logo (si existe) en el mismo directorio con el nombre "logo.png".
    public static boolean writeTemplateFiles(Context context, Map<String, String> filesMap, String logoUri) {
        try {
            // Obtener la carpeta pública de descargas
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            // Crear la subcarpeta "hostpot"
            File exportDir = new File(downloadsDir, "hostpot");
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                return false;
            }

            // Escribir cada archivo en el directorio exportDir
            for (Map.Entry<String, String> entry : filesMap.entrySet()) {
                File file = new File(exportDir, entry.getKey());
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(entry.getValue().getBytes("UTF-8"));
                fos.close();
            }

            // Si existe una URI para el logo, copiar la imagen en el directorio exportDir como "logo.png"
            if (logoUri != null && !logoUri.isEmpty()) {
                Uri uri = Uri.parse(logoUri);
                InputStream is = context.getContentResolver().openInputStream(uri);
                if (is != null) {
                    File imageFile = new File(exportDir, "logo.png");
                    FileOutputStream imageFos = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        imageFos.write(buffer, 0, len);
                    }
                    imageFos.close();
                    is.close();
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

