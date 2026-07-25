package com.jmanuel.mikroficha;

import static androidx.webkit.internal.AssetHelper.guessMimeType;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class FilesManager {

    /* TAG único para toda la clase */
    private static final String TAG = "FilesManager";

    public static boolean writeTemplateFiles(Context context,
                                             Map<String,String> filesMap,
                                             String logoUri,
                                             String backgroundImageUri) {

        ContentResolver resolver = context.getContentResolver();
        String relativePath = Environment.DIRECTORY_DOWNLOADS + "/hotspot/";

        Log.d(TAG, "→ writeTemplateFiles() — total=" + filesMap.size());

        try {
            /* 1️⃣  Exportar HTML / CSS / JS … */
            for (Map.Entry<String,String> entry : filesMap.entrySet()) {
                String filename = entry.getKey();                    // ej. login.html
                String mime     = guessMime(filename);               // ej. text/html
                byte[] data     = entry.getValue()
                        .getBytes(StandardCharsets.UTF_8);

                Log.d(TAG, " · Grabando " + filename + " (" + mime + ")");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // ───── Android 10+ (MediaStore) ─────
                    String sel = MediaStore.Downloads.RELATIVE_PATH + "=? AND "
                            + MediaStore.Downloads.DISPLAY_NAME + "=?";
                    resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            sel, new String[]{relativePath, filename});

                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                    cv.put(MediaStore.Downloads.MIME_TYPE,    mime);
                    cv.put(MediaStore.Downloads.RELATIVE_PATH,relativePath);

                    Uri fileUri = resolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);

                    if (fileUri == null) {
                        Log.e(TAG, "‼️  No se pudo crear URI para " + filename);
                        return false;
                    }
                    try (OutputStream os = resolver.openOutputStream(fileUri)) {
                        os.write(data);
                    }

                } else {
                    // ───── API <29: carpeta privada de la app ─────
                    File exportDir = new File(
                            context.getExternalFilesDir(
                                    Environment.DIRECTORY_DOWNLOADS),
                            "hotspot");
                    if (!exportDir.exists() && !exportDir.mkdirs()) {
                        Log.e(TAG, "No se pudo crear " + exportDir.getAbsolutePath());
                        return false;
                    }
                    File dest = new File(exportDir, filename);
                    if (dest.exists()) dest.delete();
                    try (OutputStream os = new FileOutputStream(dest)) {
                        os.write(data);
                    }
                }
            }

            /* 2️⃣ Copiar logo y fondo */
            if (!TextUtils.isEmpty(logoUri)) {
                Log.d(TAG, " · Copiando logo (" + logoUri + ")");
                copyDocumentToDownloads(context, logoUri, "logo", relativePath);
            }
            if (!TextUtils.isEmpty(backgroundImageUri)) {
                Log.d(TAG, " · Copiando fondo (" + backgroundImageUri + ")");
                copyDocumentToDownloads(context, backgroundImageUri,
                        "background", relativePath);
            }

            Log.d(TAG, "✔️  Exportación completada OK");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "‼️  Error en writeTemplateFiles", e);
            return false;
        }
    }

    private static String guessMime(String filename){
        String ext = MimeTypeMap.getFileExtensionFromUrl(filename);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime != null ? mime : "application/octet-stream";
    }


    private static void copyDocumentToDownloads(Context ctx,
                                                String srcUriString,
                                                String destBaseName,
                                                String relativePath) throws IOException {
        ContentResolver resolver = ctx.getContentResolver();
        Uri srcUri = Uri.parse(srcUriString);

        // Primero intentamos obtener el nombre real del archivo
        String originalFilename = getFileNameFromUri(resolver, srcUri);
        String originalExt = null;

        // Extraer extensión del archivo original si existe
        if (originalFilename != null && originalFilename.contains(".")) {
            originalExt = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }

        // Si no pudimos obtener el nombre, usamos la detección por MIME
        String mime = resolver.getType(srcUri);
        String ext = originalExt;

        // Si no tenemos extensión del nombre original, intentamos obtenerla del MIME
        if (ext == null && mime != null) {
            ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        }

        // Si aún no tenemos extensión, intentamos extraerla del URI como último recurso
        if (ext == null) {
            String p = srcUri.getLastPathSegment();
            ext = (p != null && p.contains("."))
                    ? p.substring(p.lastIndexOf('.') + 1)
                    : null;
        }

        // Si todo falló, asignamos extensión según el MIME o usamos un valor predeterminado
        if (ext == null) {
            if (mime != null) {
                if (mime.startsWith("image/jpeg")) ext = "jpg";
                else if (mime.startsWith("image/png")) ext = "png";
                else if (mime.startsWith("image/gif")) ext = "gif";
                else if (mime.startsWith("image/")) ext = "img";
                else ext = "bin";
            } else {
                ext = "bin";
            }
        }

        String finalName = destBaseName + "." + ext;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // --- Eliminar existente ---
            String sel = MediaStore.Downloads.RELATIVE_PATH + "=? AND "
                    + MediaStore.Downloads.DISPLAY_NAME + "=?";
            String[] args = new String[]{ relativePath, finalName };
            resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    sel, args);

            // --- Insertar copia nueva ---
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Downloads.DISPLAY_NAME, finalName);
            cv.put(MediaStore.Downloads.MIME_TYPE,       mime != null ? mime : "application/octet-stream");
            cv.put(MediaStore.Downloads.RELATIVE_PATH,   relativePath);

            Uri destUri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv
            );
            if (destUri == null) {
                throw new IOException("No se pudo crear destino en MediaStore");
            }
            try (InputStream  is = resolver.openInputStream(srcUri);
                 OutputStream os = resolver.openOutputStream(destUri)) {
                if (is == null || os == null) throw new IOException("Streams inválidos");
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
            }
        } else {
            // Fallback API <29
            File exportDir = new File(
                    ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "hotspot"
            );
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                throw new IOException("No se pudo crear directorio");
            }
            File destFile = new File(exportDir, finalName);
            if (destFile.exists()) destFile.delete();    // <-- eliminar si existe
            try (InputStream  is = resolver.openInputStream(srcUri);
                 OutputStream os = new FileOutputStream(destFile)) {
                if (is == null) throw new IOException("Stream de entrada inválido");
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
            }
        }
    }

    /**
     * Obtiene el nombre original del archivo desde un Uri de contenido
     * @param resolver ContentResolver para acceder al contenido
     * @param uri Uri del documento
     * @return Nombre del archivo o null si no se pudo obtener
     */
    private static String getFileNameFromUri(ContentResolver resolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    /** Devuelve la extensión (sin punto) a partir de una content‑URI */
    public static String guessExtension(Context ctx, String uriString) {
        if (uriString == null || uriString.isEmpty()) return "png";   // fallback
        Uri uri = Uri.parse(uriString);
        ContentResolver cr = ctx.getContentResolver();

        // 1. nombre real → …/IMG_20240402.jpg
        String name = getFileNameFromUri(cr, uri);
        if (name != null && name.contains("."))
            return name.substring(name.lastIndexOf('.') + 1).toLowerCase();

        // 2. por MIME type
        String mime = cr.getType(uri);
        if (mime != null) {
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
            if (ext != null) return ext.toLowerCase();
        }

        // 3. último recurso
        return "png";
    }

    /**
     * Sube vía SFTP todos los archivos de la carpeta hotspot local al router MikroTik.
     * @param host Dirección IP o hostname del router
     * @param port Puerto SFTP (normalmente 22)
     * @param username Usuario (ej. "admin")
     * @param password Contraseña
     * @param remoteDir Directorio remoto dentro de RouterOS (ej. "hotspot/temply-template")
     * @param context Contexto de la app para localizar la carpeta Downloads/hotspot
     * @return true si la transferencia fue exitosa
     */
    public static boolean uploadTemplateFTP(
            String host,
            int port,
            String username,
            String password,
            String remoteDir,
            Context context
    ) {
        // 1️⃣ Carpeta privada (API <29)
        File localDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "hotspot");

        // 2️⃣ Si no existe, prueba con la carpeta pública (API 29+)
        if (!localDir.exists()) {
            localDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS),
                    "hotspot");
        }
        if (!localDir.exists() || !localDir.isDirectory()) {
            Log.e("FilesManager","Directorio local no existe: "+localDir.getAbsolutePath());
            return false;
        }

        if (!localDir.exists() || !localDir.isDirectory()) {
            Log.e("FilesManager", "Directorio local no existe: " + localDir);
            return false;
        }

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            if (!ftp.login(username, password)) {
                ftp.logout();
                return false;
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            // Asegúrate de estar en el directorio remoto
            if (!ftp.changeWorkingDirectory(remoteDir)) {
                // si no existe, créalo y luego posiciónate
                ftp.makeDirectory(remoteDir);
                ftp.changeWorkingDirectory(remoteDir);
            }

            // Sube todos los archivos de la carpeta hotspot
            for (File f : localDir.listFiles()) {
                if (f.isFile()) {
                    try (InputStream is = new FileInputStream(f)) {
                        if (!ftp.storeFile(f.getName(), is)) {
                            Log.e("FilesManager", "Error subiendo " + f.getName());
                            // continúas o return false según tu criterio
                        }
                    }
                }
            }

            ftp.logout();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ftp.isConnected()) ftp.disconnect();
            } catch (IOException ignored) {}
        }
    }



}
