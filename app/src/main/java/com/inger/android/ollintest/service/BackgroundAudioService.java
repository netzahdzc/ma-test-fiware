package com.inger.android.ollintest.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import com.inger.android.ollintest.R;
import com.inger.android.ollintest.broadcast.AlarmReceiver;

import java.util.GregorianCalendar;

/**
 * Created by netzahdzc on 8/27/16.
 */

public class BackgroundAudioService extends Service {

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    private int duration_seconds = 0;
    private int testType = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStart(intent, startId);
        Bundle b = intent.getExtras();
        testType = b.getInt("testType");

        if (testType == STRENGTH_TEST) {
            duration_seconds = 30;
        }

        if (testType == BALANCE_TEST) {
            duration_seconds = 10;
        }

        scheduleAlarm(duration_seconds);
//        if (!mediaPlayer.isPlaying()) {
//            new CountDownTimer(duration_seconds * 1000, 100) {
//
//                public void onTick(long millisUntilFinished) {
//                    // Do nothing
//                }
//
//                public void onFinish() {
//                    mediaPlayer.start();
//                }
//            }.start();
//        }
        return START_STICKY;
    }

    public void scheduleAlarm(int duration) {
        Long time = new GregorianCalendar().getTimeInMillis() + duration * 1000;

        Intent intentAlarm = new Intent(this, AlarmReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(this, 1,
                intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
