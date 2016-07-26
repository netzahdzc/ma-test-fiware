package com.example.android.ollintest.broadcast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkMonitor extends BroadcastReceiver {

    private final String APP_NAME = "three_ollin_test";
    private Timer timerWiFi = new Timer();

    @Override
    public void onReceive(final Context context, Intent intent) {
        final int INTERVAL_TO_SEARCH_WIFI = 30; // Time in minutes before trying again
        final int INTERVAL_TO_DEFINE_CONSIDER_A_STABLE_CONN = 10;

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // WiFi connection found
            timerWiFi.schedule(new TimerTask() {
                                   public void run() {
//                                       ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                                       List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
//                                       for(int i = 0; i < procInfos.size(); i++)
//                                       {
//                                           if(procInfos.get(i).processName.equals("com.android.browser"))
//                                           {
//                                           }
//                                       }

//                                       ActivityManager am =(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//                                       List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
//                                       ActivityManager.RunningTaskInfo task = tasks.get(0); // current task
//                                       ComponentName rootActivity = task.baseActivity;

                                       new Thread( ){
                                           public void run(){
                                               // This sections is dedicated to start sending data to server-side
                                               Log.v(APP_NAME, "sending data to server");

                                               Intent i = new Intent();
                                               i.setClassName(
                                                       "com.example.android.ollintest",
                                                       "com.example.android.ollintest.util.UploadToServer");
                                               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                               context.startActivity(i);

                                               timerWiFi.cancel();
                                               timerWiFi.purge();
                                               Log.v(APP_NAME, "stopping loop");

                                           }
                                       }.start();

                                   }
                               },
                    60 * 1000 * INTERVAL_TO_DEFINE_CONSIDER_A_STABLE_CONN,
                    60 * 1000 * INTERVAL_TO_SEARCH_WIFI);
        }

        if (!mWifi.isConnected()) {
            Log.v(APP_NAME, "NO wifi connection");
        }
    }
}