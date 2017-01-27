package com.inger.android.ollintest.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.inger.android.ollintest.listener.MotionSensorListener;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MotionSensors extends Service {

    private static final String FIWARE_PATH = "fiware";
    private static final String APP_NAME = "three_ollin_test";
    private final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);

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

        mSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(
                mMotionSensor,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST,
                new Handler(mThread.getLooper()));
    }

    public void loadSettings() {
        if (mMotionSensor != null) {
//            Log.v("ACC XXX", "xxxxxxx _ " + "loadSettings - " + mMotionSensorAcc +" " + mContext +" "+ uniquePatientId+" "+ uniqueTestId);
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
        copy2FIWAREDir(mMotionSensor.getOrientDataBaseName(), "orient");
        copy2FIWAREDir(mMotionSensor.getAccDataBaseName(), "acc");
        mSensorManager.unregisterListener(mMotionSensor);
    }

    public void copy2FIWAREDir(String databasePath, String mSensorType){
        String[] path = databasePath.split("/");
        String dbName = path[path.length - 1];

        String originPath = APP_DIRECTORY_PATH + "/" + mSensorType + "/";
        String destinyPath = APP_DIRECTORY_PATH + "/" + FIWARE_PATH + "/" + mSensorType + "/";

        copyFile(originPath, dbName, destinyPath);
        //Log.v("XXX", dbName);
    }

    private void copyFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
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