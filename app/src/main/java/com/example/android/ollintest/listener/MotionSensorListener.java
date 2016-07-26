package com.example.android.ollintest.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;


import com.example.android.ollintest.DatabaseHelper;
import com.example.android.ollintest.DatabaseHelperAcc;
import com.example.android.ollintest.util.AccDBHandlerUtils;

/**
 * Created by netzahdzc on 7/24/16.
 */
public class MotionSensorListener implements SensorEventListener {

    private AccDBHandlerUtils accDBObj;

    private long mUniquePatientId;
    private long mUniqueTestId;

    public void setSettings(Context context, long uniquePatientId, long uniqueTestId) {
        accDBObj = new AccDBHandlerUtils(context);
        mUniquePatientId = uniquePatientId;
        mUniqueTestId = uniqueTestId;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
//        double[] gravity = null;
//        double[] linear_acceleration = null;

        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // TODO Pending to define if I will use gyro
            } else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    accDBObj.openDB();
//                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // TODO define how am I going to get correct acc values. Search about sensor fusion (the idea will be to implement for devices capable to collect such sensors).?
                    final double accX, accY, accZ, accTimestamp;
                    final int accAccuracy;

                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];
                    accTimestamp = event.timestamp;
                    accAccuracy = event.accuracy;
                    // Code taken from:
                    // https://developer.android.com/reference/android/hardware/SensorEvent.html#values
//                    final float alpha = 0.8f;
//
//                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
//                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
//                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
//
//                    linear_acceleration[0] = event.values[0] - gravity[0];
//                    linear_acceleration[1] = event.values[1] - gravity[1];
//                    linear_acceleration[2] = event.values[2] - gravity[2];

//                    storeAccData(accX, accY, accZ, accTimestamp, accAccuracy);

//                    Log.v("XXX", mUniquePatientId + "," + mUniqueTestId + "," + accTimestamp + "," +
//                            accAccuracy + "," + accX + "," + accY + "," + accZ + "," + "ga");

                    accDBObj.insertData(mUniquePatientId, mUniqueTestId, accTimestamp, accAccuracy,
                            accX, accY, accZ, "ga");
                    accDBObj.closeDB();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
