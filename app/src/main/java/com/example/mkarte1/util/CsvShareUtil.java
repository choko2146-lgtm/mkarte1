package com.example.mkarte1.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

public final class CsvShareUtil {
    private static final String MIME_TYPE_CSV = "text/csv";
    private static final String CHOOSER_TITLE = "CSV住所録を共有";

    private CsvShareUtil() {
    }

    public static void shareCsv(Context context, File csvFile) {
        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                csvFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MIME_TYPE_CSV);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, CHOOSER_TITLE));
    }
}
