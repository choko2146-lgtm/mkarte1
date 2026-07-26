package com.example.mkarte1.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZipCloudAddressClient {
    private static final String ENDPOINT = "https://zipcloud.ibsnet.co.jp/api/search";
    private static final int CONNECT_TIMEOUT_MS = 7000;
    private static final int READ_TIMEOUT_MS = 7000;

    public SearchResult search(String postalCode) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(ENDPOINT + "?zipcode=" + postalCode);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");

            int httpStatus = connection.getResponseCode();
            InputStream responseStream = httpStatus == HttpURLConnection.HTTP_OK
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = readBody(responseStream);
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                throw new IOException("ZipCloud HTTP status: " + httpStatus);
            }

            JSONObject root = new JSONObject(responseBody);
            int apiStatus = root.optInt("status", -1);
            if (apiStatus != 200) {
                throw new IOException("ZipCloud API status: " + apiStatus);
            }

            JSONArray results = root.optJSONArray("results");
            if (results == null || results.length() == 0) {
                return SearchResult.notFound();
            }

            List<AddressCandidate> candidates = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                String address = item.optString("address1", "")
                        + item.optString("address2", "")
                        + item.optString("address3", "");
                if (!address.trim().isEmpty()) {
                    candidates.add(new AddressCandidate(
                            item.optString("zipcode", postalCode),
                            address
                    ));
                }
            }
            return candidates.isEmpty() ? SearchResult.notFound() : SearchResult.found(candidates);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readBody(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public static class SearchResult {
        private final List<AddressCandidate> candidates;

        private SearchResult(List<AddressCandidate> candidates) {
            this.candidates = candidates;
        }

        public static SearchResult found(List<AddressCandidate> candidates) {
            return new SearchResult(Collections.unmodifiableList(new ArrayList<>(candidates)));
        }

        public static SearchResult notFound() {
            return new SearchResult(Collections.emptyList());
        }

        public boolean hasCandidates() {
            return !candidates.isEmpty();
        }

        public List<AddressCandidate> getCandidates() {
            return candidates;
        }
    }

    public static class AddressCandidate {
        private final String postalCode;
        private final String address;

        public AddressCandidate(String postalCode, String address) {
            this.postalCode = postalCode;
            this.address = address;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getAddress() {
            return address;
        }
    }
}
