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

import com.inger.android.ollintest.listener.MotionSensorListenerAcc;
import com.inger.android.ollintest.listener.MotionSensorListenerOrient;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;

public class MotionSensors extends Service {

    private TestDBHandlerUtils testDBObj;
    private SessionUtil sessionObj;

    private Sensor mSensorAcc, mSensorOrient;
    private MotionSensorListenerAcc mMotionSensorAcc;
    private MotionSensorListenerOrient mMotionSensorOrient;
    private SensorManager mSensorManagerAcc, mSensorManagerOrient;
    private Context mContext;

    private long uniquePatientId;
    private long uniqueTestId;


    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mMotionSensorAcc = new MotionSensorListenerAcc();
        mMotionSensorOrient = new MotionSensorListenerOrient();
        mSensorManagerAcc = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManagerOrient = (SensorManager) getSystemService(SENSOR_SERVICE);

        loadServiceDBs();
        registerDetector();
        loadSettings();
    }

    @SuppressLint("InlinedApi")
    public void registerDetector() {
        HandlerThread mThread = new HandlerThread("RecorderThread");
        mThread.start();

        mSensorAcc = mSensorManagerAcc.getDefaultSensor(
                Sensor.TYPE_LINEAR_ACCELERATION
        );

        mSensorManagerAcc.registerListener(
                mMotionSensorAcc,
                mSensorAcc,
                SensorManager.SENSOR_DELAY_FASTEST,
                new Handler(mThread.getLooper()));

        mSensorOrient = mSensorManagerOrient.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR
        );

        mSensorManagerOrient.registerListener(
                mMotionSensorOrient,
                mSensorOrient,
                SensorManager.SENSOR_DELAY_FASTEST,
                new Handler(mThread.getLooper()));
    }

    public void loadSettings() {
        if (mMotionSensorAcc != null) {
            mMotionSensorAcc.setSettings(mContext, uniquePatientId, uniqueTestId);
        }
        if (mMotionSensorOrient != null) {
            mMotionSensorOrient.setSettings(mContext, uniquePatientId, uniqueTestId);
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
        mSensorManagerAcc.unregisterListener(mMotionSensorAcc);
        mSensorManagerOrient.unregisterListener(mMotionSensorOrient);
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