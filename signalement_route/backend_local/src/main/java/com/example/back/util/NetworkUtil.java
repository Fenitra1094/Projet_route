package com.example.back.util;

import java.net.InetAddress;

public class NetworkUtil {

    public static boolean hasInternetConnection() {
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            return address.isReachable(2000);
        } catch (Exception e) {
            return false;
        }
    }
}
