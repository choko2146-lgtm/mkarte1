package com.example.mkarte1.util;

import android.content.Context;

import com.example.mkarte1.data.CustomerAddressExport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CustomerAddressCsvUtil {
    private static final String FILE_PREFIX = "customer_address_";
    private static final String FILE_EXTENSION = ".csv";
    private static final String LINE_SEPARATOR = "\r\n";

    private CustomerAddressCsvUtil() {
    }

    public static File createCsvFile(Context context, List<CustomerAddressExport> customers) throws IOException {
        File file = new File(context.getCacheDir(), buildFileName());
        String csv = buildCsv(customers);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file, false),
                StandardCharsets.UTF_8)) {
            writer.write(csv);
        }

        return file;
    }

    private static String buildFileName() {
        return FILE_PREFIX + DateUtil.todayYmd() + FILE_EXTENSION;
    }

    private static String buildCsv(List<CustomerAddressExport> customers) {
        StringBuilder builder = new StringBuilder();
        builder.append("顧客名,郵便番号,住所").append(LINE_SEPARATOR);

        if (customers == null) {
            return builder.toString();
        }

        for (CustomerAddressExport customer : customers) {
            builder.append(escapeCsv(customer.name))
                    .append(',')
                    .append(escapeCsv(customer.postalCode))
                    .append(',')
                    .append(escapeCsv(customer.address))
                    .append(LINE_SEPARATOR);
        }

        return builder.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuote = value.contains(",")
                || value.contains("\n")
                || value.contains("\r")
                || value.contains("\"");
        String escaped = value.replace("\"", "\"\"");

        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }
}
