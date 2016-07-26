package com.inger.android.ollintest.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.inger.android.ollintest.listener.MotionSensorListener;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;

public class MotionSensors extends Service {

    private TestDBHandlerUtils testDBObj;
    private SessionUtil sessionObj;

    private Sensor mSensor;
    private MotionSensorListener mMotionSensor;
    private SensorManager mSensorManager;
    private Context mContext;

    private long uniquePatientId;
    private long uniqueTestId;


    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mMotionSensor = new MotionSensorListener();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        loadServiceDBs();
        registerDetector();
        loadSettings();
    }

    @SuppressLint("InlinedApi")
    public void registerDetector() {
        HandlerThread mThread = new HandlerThread("RecorderThread");
        mThread.start();

        // TODO what sensor will I use? should I include gyro?
        mSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
//                | Sensor.TYPE_MAGNETIC_FIELD
//                | Sensor.TYPE_ORIENTATION
        );

        // TODO What speed for acc sensor should I define and why?
        mSensorManager.registerListener(
                mMotionSensor,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST,
                new Handler(mThread.getLooper()));
    }

    public void loadSettings() {
        if (mMotionSensor != null) {
            mMotionSensor.setSettings(mContext, uniquePatientId, uniqueTestId);
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mMotionSensor);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void loadServiceDBs() {
        testDBObj = new TestDBHandlerUtils(mContext);
        sessionObj = new SessionUtil(mContext);
        testDBObj.openDB();
        sessionObj.openDB();

        uniquePatientId = sessionObj.getPatientSession();
        uniqueTestId = testDBObj.getUniqueIDOfLatestTestType(uniquePatientId);

        sessionObj.closeDB();
        testDBObj.closeDB();
    }

}