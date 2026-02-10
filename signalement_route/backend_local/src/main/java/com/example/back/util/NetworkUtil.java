package com.example.back.util;

import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtil {

    public static boolean hasInternetConnection() {
        try {
            // Try a simple HTTP HEAD request to a reliable host
            URL url = new URL("https://www.google.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int responseCode = conn.getResponseCode();
            return (200 <= responseCode && responseCode < 400);
        } catch (Exception e) {
            return false;
        }
    }
}
