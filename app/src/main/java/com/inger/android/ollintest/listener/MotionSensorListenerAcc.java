package com.inger.android.ollintest.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


import com.inger.android.ollintest.util.AccDBHandlerUtils;
import com.inger.android.ollintest.util.OrientDBHandlerUtils;

/**
 * Created by netzahdzc on 7/24/16.
 */
public class MotionSensorListenerAcc implements SensorEventListener {

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

        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                int j = (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) ? 1 : 0;
                if (j == 1) {
                    accDBObj.openDB();
                    final double accX, accY, accZ, accTimestamp;
                    final int accAccuracy;

                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];
                    accTimestamp = event.timestamp;
                    accAccuracy = event.accuracy;

//                    Log.v("ACC XXX", mUniquePatientId + "," + mUniqueTestId + "," + accTimestamp + "," +
//                            accAccuracy + "," + accX + "," + accY + "," + accZ);

                    accDBObj.insertData(mUniquePatientId, mUniqueTestId, accTimestamp, accAccuracy,
                            accX, accY, accZ);
                    accDBObj.closeDB();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
