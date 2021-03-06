package com.cicese.android.matest.broadcast;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cicese.android.matest.service.UploadToServer;

public class NetworkMonitor extends BroadcastReceiver {

    private final String APP_NAME = "ma_test";
    // This is a class variable since the idea is that each time the user get wifi connection,
    // a new schedule settled automatically, substituting the previous one
    private Timer timerWiFi = new Timer();

    @Override
    public void onReceive(final Context context, Intent intent) {
        final int INTERVAL_TO_SEARCH_WIFI = 60; // Time in minutes before trying again

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // WiFi connection found
            timerWiFi.schedule(new TimerTask() {
                                   public void run() {
                                       new Thread() {
                                           public void run() {
                                               // This sections is dedicated to start sending data to server-side
                                               Log.v(APP_NAME, "sending data to server");

                                               // To fetch accelerometer data
                                               Intent i = new Intent(context, UploadToServer.class);
                                               i.putExtra("sensorType", "acc");
                                               context.startService(i);
                                               context.stopService(i);

                                               // To fetch orientation data
                                               Intent ii = new Intent(context, UploadToServer.class);
                                               ii.putExtra("sensorType", "orient");
                                               context.startService(ii);
                                               context.stopService(ii);
                                           }
                                       }.start();
                                   }
                               },
                    60 * 1000 * INTERVAL_TO_SEARCH_WIFI, // DELAY AFTER FIRST EXECUTION
                    60 * 1000 * INTERVAL_TO_SEARCH_WIFI); // TASK WILL BE TRIGGER EACH TIME
        }

        if (!mWifi.isConnected()) {
            Log.v(APP_NAME, "NO wifi connection");
        }
    }
}