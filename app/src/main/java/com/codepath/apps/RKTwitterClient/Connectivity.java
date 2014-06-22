package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Connectivity {

    /**
     * Check if DNS is functioning and packets can reach google.com.
     */
    public static boolean pingGoogleSynchronous() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();

            BufferedReader r = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            Log.d("DEBUG", total.toString());

            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static boolean isEmulator() {
        boolean isEmulator = Build.FINGERPRINT.startsWith("generic");
        Log.d("DBG", String.format("Is it an emulator? %b", isEmulator));
        return isEmulator;
    }

    public static boolean isOnline(Activity activity) {
        if (isEmulator()) {
            return isNetworkAvailable(activity);
        }
        return pingGoogleSynchronous();
    }

}
