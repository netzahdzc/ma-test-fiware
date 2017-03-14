package com.inger.android.ollintest.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


import com.inger.android.ollintest.util.AccDBHandlerUtils;
import com.inger.android.ollintest.util.DateUtil;
import com.inger.android.ollintest.util.OrientDBHandlerUtils;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * Created by netzahdzc on 7/24/16.
 */
public class MotionSensorListener implements SensorEventListener {

    private static final String APP_NAME = "three_ollin_test";

    private AccDBHandlerUtils accDBObj;
    private OrientDBHandlerUtils orientDBObj;

    private long mUniquePatientId;
    private long mUniqueTestId;
    private boolean mAllowRecording = false;

    public void setSettings(Context context, long uniquePatientId, long uniqueTestId) {
        accDBObj = new AccDBHandlerUtils(context);
        orientDBObj = new OrientDBHandlerUtils(context);
        mUniquePatientId = uniquePatientId;
        mUniqueTestId = uniqueTestId;
        mAllowRecording = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                if (mAllowRecording) {
                    // -------------- ACCELERATION SECTION --------------
                    accDBObj.openDB();
                    final double accX, accY, accZ;//, accTimestamp = 0.0;
                    final int accAccuracy;

                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];
                    accAccuracy = event.accuracy;

                    //DateUtil dateObj = new DateUtil();

                    //Log.v(APP_NAME, "ACC XXX " + mUniquePatientId + "," + mUniqueTestId + "," +
                    //        accAccuracy + ", " + accX + ", " + accY + ", " + accZ + " ==> " + dateObj.getCurrentDate());

                    accDBObj.insertData(mUniquePatientId, mUniqueTestId, accAccuracy, accX, accY, accZ);
                    accDBObj.closeDB();

                    // -------------- ORIENTATION SECTION --------------
                    orientDBObj.openDB();
                    final double azimuth, pitch, roll;//, orientTimestamp;

                    // Convert the rotation-vector to a 4x4 matrix.
                    float[] mRotationMatrix = new float[16];
                    float[] mRotationMatrixFromVector = new float[16];

                    SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector,
                            event.values);
                    SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                            SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
                    float[] orientationValues = new float[3];
                    SensorManager.getOrientation(mRotationMatrix, orientationValues);

                    // Converting the result from radians to degrees
                    azimuth = (float) Math.toDegrees(orientationValues[0]);

                    //Azimuth, angle of rotation about the -z axis. This value represents the angle
                    //between the device's y axis and the magnetic north pole. When facing north,
                    //this angle is 0, when facing south, this angle is π. Likewise, when facing east,
                    //this angle is π/2, and when facing west, this angle is -π/2.
                    //The range of values is -π to π.

                    pitch = (float) Math.toDegrees(orientationValues[1]);

                    //Pitch, angle of rotation about the x axis. This value represents the angle
                    //between a plane parallel to the device's screen and a plane parallel to the
                    //ground. Assuming that the bottom edge of the device faces the user and that
                    //the screen is face-up, tilting the top edge of the device toward the ground
                    //creates a positive pitch angle. The range of values is -π to π.

                    roll = (float) Math.toDegrees(orientationValues[2]);

                    //Roll, angle of rotation about the y axis. This value represents the angle between
                    //a plane perpendicular to the device's screen and a plane perpendicular to the
                    //ground. Assuming that the bottom edge of the device faces the user and that the
                    //screen is face-up, tilting the left edge of the device toward the ground creates
                    //a positive roll angle. The range of values is -π/2 to π/2.


                    //Log.v(APP_NAME, "ORIENT XXX " + mUniquePatientId + "," + mUniqueTestId + ", " +
                    //        azimuth + ", " + pitch + ", " + roll);

                    orientDBObj.insertData(mUniquePatientId, mUniqueTestId, azimuth, pitch, roll);
                    orientDBObj.closeDB();
                }
            }
        }
    }

    public String getAccDataBaseName() {
        return accDBObj.getPath();
    }

    public void stopDataCollection(){
        mAllowRecording = false;
    }

    public String getOrientDataBaseName() {
        return orientDBObj.getPath();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
