package com.example.whatsappstatusdownloder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Saves a WhatsApp status file to the public gallery reliably on all Android
 * versions. On Android 10+ (scoped storage) direct File writes to /Download are
 * blocked, so we use MediaStore — which also makes the file show up in Gallery.
 */
public class MediaSaver {

    private static final String FOLDER = "StatusSaver";

    public static boolean save(Context ctx, File src) {
        String name = src.getName();
        boolean isVideo = name.toLowerCase().endsWith(".mp4");

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return saveWithMediaStore(ctx, src, name, isVideo);
            } else {
                return saveLegacy(ctx, src, name, isVideo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Android 10+ : MediaStore (no All-Files-Access needed to write, shows in Gallery)
    private static boolean saveWithMediaStore(Context ctx, File src, String name, boolean isVideo) throws Exception {
        ContentResolver resolver = ctx.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeOf(name, isVideo));
        String relPath = (isVideo ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES) + "/" + FOLDER;
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relPath);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri collection = isVideo
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Uri uri = resolver.insert(collection, values);
        if (uri == null) return false;

        try (InputStream in = new FileInputStream(src);
             OutputStream out = resolver.openOutputStream(uri)) {
            copy(in, out);
        }

        values.clear();
        values.put(MediaStore.MediaColumns.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        return true;
    }

    // Android 9 and below : write to public dir + tell the gallery to scan it
    private static boolean saveLegacy(Context ctx, File src, String name, boolean isVideo) throws Exception {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(
                        isVideo ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES),
                FOLDER);
        if (!dir.exists() && !dir.mkdirs()) return false;

        File dest = new File(dir, name);
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            copy(in, out);
        }
        MediaScannerConnection.scanFile(ctx, new String[]{dest.getAbsolutePath()}, null, null);
        return true;
    }

    private static String mimeOf(String name, boolean isVideo) {
        String n = name.toLowerCase();
        if (isVideo) return "video/mp4";
        if (n.endsWith(".png")) return "image/png";
        return "image/jpeg";
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();
    }
}
