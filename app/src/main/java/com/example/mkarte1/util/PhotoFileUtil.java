package com.example.mkarte1.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.example.mkarte1.data.Customer;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class PhotoFileUtil {
    private static final String APP_FOLDER = "おかんのカルテ";

    private PhotoFileUtil() {
    }

    public static File createTempPhotoFile(Context context) throws IOException {
        File dir = appPictureDir(context);
        return new File(dir, "TEMP_" + DateUtil.tempStamp() + ".jpg");
    }

    public static File appPictureDir(Context context) {
        File pictures = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File dir = new File(pictures, APP_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File customerDir(Context context, Customer customer) {
        File dir = new File(appPictureDir(context), FileNameUtil.sanitize(customer.name));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File moveTempToCustomer(Context context, String tempPath, Customer customer, String ymd) throws IOException {
        File source = new File(tempPath);
        File dir = customerDir(context, customer);
        String base = ymd + "_" + FileNameUtil.sanitize(customer.name);
        File target = uniqueFile(dir, base, ".jpg");
        try (FileInputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        source.delete();
        return target;
    }

    public static Uri uriFor(File file) {
        return Uri.fromFile(file);
    }

    public static void deleteQuietly(String uri) {
        if (uri == null || uri.isEmpty()) {
            return;
        }
        try {
            File file = new File(Uri.parse(uri).getPath());
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private static File uniqueFile(File dir, String base, String ext) {
        File candidate = new File(dir, base + ext);
        int index = 1;
        while (candidate.exists()) {
            candidate = new File(dir, base + "_" + String.format("%02d", index) + ext);
            index++;
        }
        return candidate;
    }
}
