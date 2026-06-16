package com.example.mkarte1.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class MediaStoreHelper {
    private static final String TAG = "MediaStoreHelper";
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String GALLERY_FOLDER = "Okannokarte";

    private MediaStoreHelper() {
    }

    public static Uri copyToGallery(Context context, File sourceFile, String displayName) {
        if (context == null || sourceFile == null || !sourceFile.exists()) {
            return null;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            scanExistingFile(context, sourceFile);
            return Uri.fromFile(sourceFile);
        }

        String safeDisplayName = displayName == null || displayName.trim().isEmpty()
                ? sourceFile.getName()
                : displayName.trim();

        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, safeDisplayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, JPEG_MIME_TYPE);
        values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + GALLERY_FOLDER
        );
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri galleryUri = null;
        try {
            galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (galleryUri == null) {
                return null;
            }

            try (InputStream input = new FileInputStream(sourceFile);
                 OutputStream output = resolver.openOutputStream(galleryUri)) {
                if (output == null) {
                    resolver.delete(galleryUri, null, null);
                    return null;
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }

            ContentValues publishValues = new ContentValues();
            publishValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(galleryUri, publishValues, null, null);
            return galleryUri;
        } catch (Exception e) {
            Log.w(TAG, "Failed to copy photo to MediaStore", e);
            if (galleryUri != null) {
                resolver.delete(galleryUri, null, null);
            }
            return null;
        }
    }

    public static void scanPhoto(Context context, File file) {
        if (context == null || file == null || !file.exists()) {
            return;
        }
        Uri galleryUri = copyToGallery(context, file, file.getName());
        if (galleryUri == null) {
            scanExistingFile(context, file);
        }
    }

    private static void scanExistingFile(Context context, File file) {
        MediaScannerConnection.scanFile(
                context.getApplicationContext(),
                new String[]{file.getAbsolutePath()},
                new String[]{JPEG_MIME_TYPE},
                null
        );
    }
}
