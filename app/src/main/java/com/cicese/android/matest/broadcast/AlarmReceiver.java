package com.cicese.android.matest.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.PowerManager;

import com.cicese.android.matest.R;

/**
 * Created by netzahdzc on 9/8/16.
 */
public class AlarmReceiver extends BroadcastReceiver implements MediaPlayer.OnCompletionListener  {

    private MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKING-UP");
        wl.acquire();

        mediaPlayer = MediaPlayer.create(context, R.raw.long_beep);
        mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer _mediaPlayer) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }
}
