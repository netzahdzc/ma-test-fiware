package com.inger.android.ollintest.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.inger.android.ollintest.DatabaseContract;
import com.inger.android.ollintest.DatabaseContractAcc;
import com.inger.android.ollintest.DatabaseContractOrient;
import com.inger.android.ollintest.DatabaseHelper;
import com.inger.android.ollintest.DatabaseHelperAcc;
import com.inger.android.ollintest.util.AccDBHandlerUtils;
import com.inger.android.ollintest.util.ControlDBHandlerUtils;
import com.inger.android.ollintest.util.Filter;
import com.inger.android.ollintest.util.OrientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.TechnicalDBHandlerUtils;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.UserDBHandlerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;


public class UploadToServer extends Service {

    private static final int OK = 200;
    private static final int ERROR = 422;

    private static final String APP_NAME = "three_ollin_test";
    private static final String FIWARE_PATH = "fiware";
    private static final String FIWARE_ORION_PATH = "http://207.249.127.162:1026/v2";
    private static final String INGER_PATH = "http://investigacion.inger.gob.mx:8000";

    private final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);

    private String upLoadServerUri = INGER_PATH + "/sqlite/UploadToServer.php";
    private int serverResponseCode = 0;
    static final int MAX_CONNECTIONS = 5;
    static final String ENCODING = "UTF-8";


    public void deleteSentFile(String filePath) {
        File file = new File(filePath);
        file.delete();

        // To get more info about this files: https://www.sqlite.org/tempfiles.html
        File file_journal = new File(filePath + "-journal");
        file_journal.delete();

        Log.v(APP_NAME, "File Deleted.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.setProperty("http.maxConnections", String.valueOf(MAX_CONNECTIONS));
        System.setProperty("sun.net.http.errorstream.enableBuffering", "true");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle b = intent.getExtras();
        String sensorType = b.getString("sensorType");
        Log.i(APP_NAME, "I'm on IBinder and I'm carrying this value: " + sensorType);

        // We start checking into directories to retrieve and send file to server-side
        startScan(sensorType);

        return START_REDELIVER_INTENT;
    }

    private void processFiles(File[] files, String mSensorType, String option) {
        if (option.equals("inger") || option.equals("fiware")) {
            // Upload previous loaded list of file to INGER server
            // Upload previous loaded list of file to INGER server
            for (int i = 0; i < files.length; i++) {
                Log.i(APP_NAME, "Current element to analise: " + files[i].getPath() + "");

                /** In order to ensure that we are not sending (uploading & deleting) any file
                 * currently been used to store acc data. This section, compare epoch time to filter
                 * new ones. Thus, we only send (upload & delete) files older than 10 min. Which is
                 * enough time to ensure that at least 1 patient has already finish his tests.
                 */
                long tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000);
                String[] fileNameChunks = files[i].getPath().split("\\.");
                String[] fileTime = fileNameChunks[0].split("/");
                String[] time = fileTime[fileTime.length - 1].split("_");

                long fileTimeMilliseconds = Long.parseLong(time[time.length - 1]);

                Log.v(APP_NAME, fileTimeMilliseconds + " < " + tenMinutesAgo);

                if (fileTimeMilliseconds < tenMinutesAgo) {
                    if (option.equals("inger")) {
                        if (uploadINGERFile(files[i].getPath()) == OK) {
                            // Delete file, since we have already sent them
                            deleteSentFile(files[i].getPath());
                        }
                    }
                    if (option.equals("fiware")) {
                        try {
                            // TODO Check how to refrieve FIWARE code number instead of this funky solution
                            if (uploadFIWAREFile(mSensorType, files[i].getPath()) != 0) {
                                // Delete file, since we have already sent them
                                Log.v("XXX FIWARE para DELETE", files[i].getPath());
                                deleteSentFile(files[i].getPath());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.v(APP_NAME, "File was sent because already past 10 min");
                } else {
                    Log.v(APP_NAME, "File NO sent because files still fresh");
                }
            }
        }

        if (option.equals("main")) {
            // Upload previous loaded list
            for (int i = 0; i < files.length; i++) {
                try {
                    // Renaming file to keep a track of the new one
                    String[] dbNamePath = files[i].getPath().split("\\.");
                    String newFileName = dbNamePath[0] + "_" + System.currentTimeMillis() + "." + dbNamePath[1];
                    uploadMainFile(files[i].getPath(), newFileName);

                    //Log.v("adadas", "qweqweqwe");
                    uploadMainFileFiware("ControlTests");
                    uploadMainFileFiware("Patients");
                    uploadMainFileFiware("Questionnaires");
                    uploadMainFileFiware("Users");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startScan(String mSensorType) {
        // Get list with sensor files from INGER
        File[] ingerFiles = new Filter().finder(APP_DIRECTORY_PATH + "/" + mSensorType, "db");
        Log.i(APP_NAME, "Elements on queue: " + ingerFiles.length + "");
        Log.i(APP_NAME, "sensorType: " + mSensorType);
        processFiles(ingerFiles, mSensorType, "inger");

        // Get list with sensor files from INGER
        File[] fiwareFiles = new Filter().finder(APP_DIRECTORY_PATH + "/" + FIWARE_PATH + "/" + mSensorType, "db");
        Log.i(APP_NAME, "Elements on queue: " + fiwareFiles.length + "");
        Log.i(APP_NAME, "sensorType: " + mSensorType);
        processFiles(fiwareFiles, mSensorType, "fiware");

        // Get list of main database. The one including user records.
        File[] mainFile = new Filter().finder(APP_DIRECTORY_PATH, "db");
        Log.i(APP_NAME, "Elements on queue: " + mainFile.length + "");
        processFiles(mainFile, mSensorType, "main");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // To upload main files
    // TODO There are three similar functions to upload files, there should be a single one
    // TODO Keep the app responsive: https://developer.android.com/training/articles/perf-anr.html
    public int uploadMainFile(String sourceFileUri, String newName) {
        final String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.i(APP_NAME, "Source File not exist ");
            return 0;
        } else {
            try {
                Log.i(APP_NAME, "Temp file found:" + sourceFile.getPath());

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", newName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + newName + '"' + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                // String serverResponseMessage = conn.getResponseMessage();

                if (serverResponseCode == OK) {
                    Log.i(APP_NAME, "File Upload Complete: " + serverResponseCode);
                }

                //close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();

                //return serverResponseCode;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                serverResponseCode = ERROR;
                Log.e(APP_NAME, "MalformedURLException");
            } catch (Exception e) {
                e.printStackTrace();
                serverResponseCode = ERROR;
                Log.e(APP_NAME, "Got Exception : see logcat");
                Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }

    // To upload main file to FIWARE in JSON documents
    public int uploadMainFileFiware(String option) throws JSONException {
        Log.v(APP_NAME, "Processing " + option + " data");
        HttpURLConnection urlConnection;
        JSONObject data = extract2Parse(option);

        Log.v(APP_NAME, "Preparing batch: " + data.toString());

        try {
            // Open a HTTP  connection to  the URL
            urlConnection = (HttpURLConnection) ((new URL(FIWARE_ORION_PATH + "/" + "op" + "/" + "update").openConnection()));
            urlConnection.setDoInput(true); // Allow Inputs
            urlConnection.setDoOutput(true); // Allow Outputs
            urlConnection.setUseCaches(false); // Don't use a Cached Copy
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                urlConnection.setRequestProperty("Connection", "close");
            }

            //Write
            Log.v(APP_NAME, "Writing document");
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, ENCODING));
            writer.write(String.valueOf(data));
            writer.close();
            outputStream.flush();
            outputStream.close();

            //Read
            Log.v(APP_NAME, "Reading document");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), ENCODING));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            Log.v("XXX line: ", "(uploadMainFileFiware) " + line);

            Log.v(APP_NAME, "Closing document");
            bufferedReader.close();

            //close the streams
            Log.v(APP_NAME, sb.toString() + "");

            serverResponseCode = OK;
        } catch (FileNotFoundException e) {
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "FileNotFoundException: " + e);
        } catch (EOFException e) {
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "EOFException");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "MalformedURLException: " + e);
        } catch (Exception e) {
            e.printStackTrace();
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "Got Exception : see logcat");
            Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
        }
        return serverResponseCode;
    }

    private JSONObject extract2Parse(String option) throws JSONException {

        JSONObject core = new JSONObject();
        JSONArray entitiesArray = new JSONArray();
        JSONObject main;
        JSONObject standardContent;
        JSONObject metadataContent;


        if (option.equals("ControlTests")) {
            String patient_id = "0";
            String weight = "0";
            String height = "0";
            String waist_size = "0";
            String heart_rate = "0";
            String systolic_blood = "0";
            String diastolic_blood = "0";
            String time_interval = "";

            // Setting the batch of control entities
            core.put("actionType", "APPEND");

            ControlDBHandlerUtils controlDBObj = new ControlDBHandlerUtils(getApplicationContext());
            controlDBObj.openDB();

            Cursor mControlTest = controlDBObj.readAllData();
            if (mControlTest != null) {
                while (mControlTest.moveToNext()) {
                    patient_id = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL1)
                    );
                    weight = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL2)
                    );
                    height = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL3)
                    );
                    waist_size = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL4)
                    );
                    heart_rate = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL5)
                    );
                    systolic_blood = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL6)
                    );
                    diastolic_blood = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL7)
                    );
                    time_interval = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL8)
                    );

                    // Building JSON document
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "-" + "control" + "-" +
                            patient_id + "" + System.currentTimeMillis();
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "ControlTest");

                    standardContent = new JSONObject();
                    standardContent.put("value", weight);
                    standardContent.put("type", "kg");
                    main.put("omh:body_weight", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", height);
                    standardContent.put("type", "cm");
                    main.put("omh:body_height", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", waist_size);
                    standardContent.put("type", "cm");
                    main.put("waistCircumference", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", heart_rate);
                    standardContent.put("type", "beats/min");
                    main.put("omh:heart_rate", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", systolic_blood);
                    standardContent.put("type", "mmHg");
                    main.put("omh:systolic_blood_pressure", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", diastolic_blood);
                    standardContent.put("type", "mmHg");
                    main.put("omh:diastolic_blood_pressure", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", time_interval);
                    standardContent.put("type", "omh:time-interval");
                    main.put("omh:effective_time_frame", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        if (option.equals("Patients")) {
            String patient_id = "0";
            String name = "";
            String surname = "";
            String gender = "";
            String birthday = "";
            String trash = "0";
            String created = "";
            String lastUpdate = "";

            // Setting the batch of control entities
            core.put("actionType", "APPEND");

            PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
            patientDBObj.openDB();

            Cursor mControlPatien = patientDBObj.readAllData();
            if (mControlPatien != null) {
                while (mControlPatien.moveToNext()) {
                    patient_id = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient._ID)
                    );
                    name = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL1)
                    );
                    surname = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL2)
                    );
                    gender = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL3)
                    );
                    birthday = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL4)
                    );
                    trash = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL6)
                    );
                    created = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL7)
                    );
                    lastUpdate = mControlPatien.getString(
                            mControlPatien.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL8)
                    );

                    // Building JSON document
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "-" + "patient" + "-" +
                            patient_id + "" + System.currentTimeMillis();
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "ControlTest");

                    standardContent = new JSONObject();
                    standardContent.put("value", name);
                    main.put("name", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", surname);
                    main.put("surname", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", gender);
                    main.put("gender", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", birthday);
                    main.put("birthday", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", trash);
                    main.put("trash", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", created);
                    main.put("created", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", lastUpdate);
                    main.put("lastUpdate", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        if (option.equals("Users")) {
            String user_id = "0";
            String name = "";
            String surname = "";
            String gender = "";
            String email = "";
            String activationCode = "0";
            String trash = "";
            String created = "";
            String lastUpdate = "";

            // Setting the batch of control entities
            core.put("actionType", "APPEND");

            UserDBHandlerUtils userDBObj = new UserDBHandlerUtils(getApplicationContext());
            userDBObj.openDB();

            Cursor mControlUser = userDBObj.readAllData();
            if (mControlUser != null) {
                while (mControlUser.moveToNext()) {
                    user_id = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User._ID)
                    );
                    name = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL1)
                    );
                    surname = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL2)
                    );
                    gender = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL3)
                    );
                    activationCode = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL6)
                    );
                    email = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL4)
                    );
                    trash = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL8)
                    );
                    created = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL9)
                    );
                    lastUpdate = mControlUser.getString(
                            mControlUser.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL10)
                    );

                    // Building JSON document
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "-" + "user" + "-" +
                            user_id + "" + System.currentTimeMillis();
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "ControlTest");

                    standardContent = new JSONObject();
                    standardContent.put("value", name);
                    main.put("name", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", surname);
                    main.put("surname", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", gender);
                    main.put("gender", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", activationCode);
                    main.put("activationCode", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", email);
                    main.put("email", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", trash);
                    main.put("trash", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", created);
                    main.put("created", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", lastUpdate);
                    main.put("lastUpdate", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        if (option.equals("Questionnaires")) {
            String test_id = "0";
            String patient_id = "0";
            String question1 = "";
            String question2 = "";
            String question3 = "";
            String question4 = "";
            String question5 = "";
            String question6 = "";
            String question7 = "";
            String question8 = "";
            String question9 = "";
            String question10 = "";
            String status = "";
            String lastUpdate = "";

            // Setting the batch of control entities
            core.put("actionType", "APPEND");

            TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
            testDBObj.openDB();

            Cursor mControlTest = testDBObj.readAllData();
            if (mControlTest != null) {
                while (mControlTest.moveToNext()) {
                    test_id = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test._ID)
                    );
                    patient_id = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test._ID)
                    );
                    question1 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL6)
                    );
                    question2 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL7)
                    );
                    question3 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL8)
                    );
                    question4 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL9)
                    );
                    question5 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL10)
                    );
                    question6 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL11)
                    );
                    question7 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL12)
                    );
                    question8 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL13)
                    );
                    question9 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL14)
                    );
                    question10 = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL15)
                    );
                    status = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL18)
                    );
                    lastUpdate = mControlTest.getString(
                            mControlTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL19)
                    );

                    // Building JSON document
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "-" + "questionnaire" + "-" +
                            test_id + "" + System.currentTimeMillis();
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "Questionnaire");

                    standardContent = new JSONObject();
                    standardContent.put("value", test_id);
                    standardContent.put("type", "id");
                    main.put("test", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", patient_id);
                    standardContent.put("type", "id");
                    main.put("patient", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question1);
                    main.put("question1", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question2);
                    main.put("question2", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question3);
                    main.put("question3", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question4);
                    main.put("question4", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question5);
                    main.put("question5", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question6);
                    main.put("question6", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question7);
                    main.put("question7", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question8);
                    main.put("question8", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question9);
                    main.put("question9", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", question10);
                    main.put("question10", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", status);
                    main.put("status", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", lastUpdate);
                    main.put("lastUpdate", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        core.put("entities", entitiesArray);

        return core;
    }

    // To upload files to INGER server
    public int uploadINGERFile(String sourceFileUri) {
        final String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.i(APP_NAME, "Source File not exist ");
            return 0;
        } else {
            try {
                Log.i(APP_NAME, "Temp file found:" + sourceFile.getPath());

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + '"' + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                //String serverResponseMessage = conn.getResponseMessage();
                //Log.v("XXX CODE: ", serverResponseCode + "");
                //Log.v("XXX MESSAGE", serverResponseMessage);

                if (serverResponseCode == OK) {
                    Log.i(APP_NAME, "File Upload Complete: " + serverResponseCode);
                }

                //close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();

                return serverResponseCode;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                serverResponseCode = ERROR;
                Log.e(APP_NAME, "MalformedURLException");
            } catch (Exception e) {
                e.printStackTrace();
                serverResponseCode = ERROR;
                Log.e(APP_NAME, "Got Exception : see logcat");
                Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }

    private String getUniqueID() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    public String getTestType(int option) {
        String outcome = "";

        switch (option) {
            case WALKING_TEST:
                outcome = "Timed Up and Go";
                break;
            case STRENGTH_TEST:
                outcome = "30 second sit to stand test";
                break;
            case BALANCE_TEST:
                outcome = "4-Stage Balance Test";
                break;
            default:
        }

        return outcome;
    }

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    // TODO This need to be integrated to both: the Android's schema (json) and documentation
    public String getTestTypeOption(int option) {
        String outcome = "";

        switch (option) {
            case TANDEM_TEST_OPTION:
                outcome = "Tandem (Full)";
                break;
            case SEMI_TANDEM_TEST_OPTION:
                outcome = "Semi-Tandem";
                break;
            case FEET_TOGETHER_TEST_OPTION:
                outcome = "Side by Side";
                break;
            case ONE_LEG_TEST_OPTION:
                outcome = "Single-Leg Stance";
                break;
            default:
                outcome = "NA";
        }

        return outcome;
    }

    final int RESTART_COUNTER = 1;
    final int BATCH_SIZE = 10;

    private List extract2Parse(String sourceFileUri, String mSensorType) throws JSONException {

        JSONObject main = new JSONObject();
        JSONObject standardContent;
        JSONObject metadataContent;
        List batch = new ArrayList();
        List jsonAndBatch = new ArrayList();

        int c = RESTART_COUNTER;
        String patient_id = "0";
        String test_id = "0";
        String test_option = "0";
        //String accuracy = "0;
        String x = "0.0";
        String y = "0.0";
        String z = "0.0";
        String azimuth = "0.0";
        String pitch = "0.0";
        String roll = "0.0";
        String created = "0.0";
        String start_date_time = "";
        String end_date_time = "";
        String chunk = "";
        String test_type = "0";
        String sensor_type = "0.0";
        String sensor_speed = "60"; // TODO Get speed of sensors

        if (mSensorType.equals("acc")) {
            chunk = "";
            sensor_type = "Accelerometer";
            AccDBHandlerUtils mAccDbHelper = new AccDBHandlerUtils(getApplicationContext(), sourceFileUri);
            mAccDbHelper.openDB();

            Cursor mAccCursor = mAccDbHelper.readData();
            if (mAccCursor != null) {
                while (mAccCursor.moveToNext()) {
                    patient_id = mAccCursor.getString(
                            mAccCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL1)
                    );
                    test_id = mAccCursor.getString(
                            mAccCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL2)
                    );
                    // TODO It won't be included to fiware, but maybe I should. Think about it.
                    /*accuracy= mCursor.getString(
                            mCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL3)
                    );*/
                    x = mAccCursor.getString(
                            mAccCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL4)
                    );
                    y = mAccCursor.getString(
                            mAccCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL5)
                    );
                    z = mAccCursor.getString(
                            mAccCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL6)
                    );
                    created = mAccCursor.getString(
                            mAccCursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7)
                    );
                    chunk += x + "," + y + "," + z + "," + created + " ";

                    // Building a batch
                    // TODO take care of the remaining less than the BATCH_SIZE. I'm leaving then to hell :P
                    if (c == BATCH_SIZE) {
                        batch.add(chunk);
                        c = RESTART_COUNTER;
                    }

                    c++;
                }

                mAccCursor.close();
            }
        }

        if (mSensorType.equals("orient")) {
            chunk = "";
            sensor_type = "Orientation";
            OrientDBHandlerUtils mOrientDbHelper = new OrientDBHandlerUtils(getApplicationContext(), sourceFileUri);
            mOrientDbHelper.openDB();

            Cursor mOrientCursor = mOrientDbHelper.readData();
            if (mOrientCursor != null) {
                while (mOrientCursor.moveToNext()) {
                    patient_id = mOrientCursor.getString(
                            mOrientCursor.getColumnIndexOrThrow(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL1)
                    );
                    test_id = mOrientCursor.getString(
                            mOrientCursor.getColumnIndexOrThrow(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL2)
                    );
                    azimuth = mOrientCursor.getString(
                            mOrientCursor.getColumnIndexOrThrow(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL3)
                    );
                    pitch = mOrientCursor.getString(
                            mOrientCursor.getColumnIndexOrThrow(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL4)
                    );
                    roll = mOrientCursor.getString(
                            mOrientCursor.getColumnIndexOrThrow(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL5)
                    );
                    created = mOrientCursor.getString(
                            mOrientCursor.getColumnIndexOrThrow(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL6)
                    );
                    chunk += azimuth + "," + pitch + "," + roll + "," + created + " ";

                    // Building a batch
                    // TODO take care of the remaining less than the BATCH_SIZE. I'm leaving then to hell :P
                    if (c == BATCH_SIZE) {
                        batch.add(chunk);
                        c = RESTART_COUNTER;
                    }

                    c++;
                }

                mOrientCursor.close();
            }
        }

        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        Cursor mCursorTest = testDBObj.readData(Integer.parseInt(test_id));
        if (mCursorTest.moveToFirst()) {
            test_type = mCursorTest.getString(
                    mCursorTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL2)
            );
            test_option = mCursorTest.getString(
                    mCursorTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL3)
            );
            start_date_time = mCursorTest.getString(
                    mCursorTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL4)
            );
            end_date_time = mCursorTest.getString(
                    mCursorTest.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL5)
            );
        }

        TechnicalDBHandlerUtils technicalDBObj = new TechnicalDBHandlerUtils(getApplicationContext());
        technicalDBObj.openDB();

        String mobile_model = "";
        String mobile_brand = "";
        String mobile_android_api = "";
        String app_version = "";

        Cursor mCursorTechnical = technicalDBObj.readDataByTestId(Integer.parseInt(test_id));
        if (mCursorTechnical.moveToFirst()) {
            mobile_model = mCursorTechnical.getString(
                    mCursorTechnical.getColumnIndexOrThrow(DatabaseContract.Technical.COLUMN_NAME_COL3)
            );
            mobile_brand = mCursorTechnical.getString(
                    mCursorTechnical.getColumnIndexOrThrow(DatabaseContract.Technical.COLUMN_NAME_COL4)
            );
            mobile_android_api = mCursorTechnical.getString(
                    mCursorTechnical.getColumnIndexOrThrow(DatabaseContract.Technical.COLUMN_NAME_COL5)
            );
            app_version = mCursorTechnical.getString(
                    mCursorTechnical.getColumnIndexOrThrow(DatabaseContract.Technical.COLUMN_NAME_COL6)
            );
        }

        // Building JSON document
        String sensor = "";
        if (mSensorType.equals("acc")) sensor = "accelerometer";
        if (mSensorType.equals("orient")) sensor = "orientation";

        String uniquePostId = getUniqueID().replaceAll("-", "") + "-" + sensor + "-" + test_id + "" + System.currentTimeMillis();
        main.put("id", uniquePostId);
        main.put("type", "PhysicalTest");

        standardContent = new JSONObject();
        standardContent.put("value", getTestTypeOption(Integer.parseInt(test_option)));
        standardContent.put("type", "test-option");
        metadataContent = new JSONObject();
        metadataContent.put("variant", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", getTestType(Integer.parseInt(test_type)));
        standardContent.put("type", "test-type");
        standardContent.put("metadata", metadataContent);
        main.put("test", standardContent);

        standardContent = new JSONObject();
        standardContent.put("value", sensor_speed);
        standardContent.put("type", "hz");
        metadataContent = new JSONObject();
        metadataContent.put("speed", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", sensor_type);
        standardContent.put("type", "sensor-type");
        standardContent.put("metadata", metadataContent);
        main.put("sensor", standardContent);

        standardContent = new JSONObject();
        standardContent.put("value", "csv");
        standardContent.put("type", "format");
        metadataContent = new JSONObject();
        metadataContent.put("format", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", "Smartphone");
        standardContent.put("type", "device-type");
        metadataContent.put("device", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", mobile_model);
        standardContent.put("type", "Smartphone");
        metadataContent.put("model", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", mobile_brand);
        standardContent.put("type", "Smartphone");
        metadataContent.put("brand", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", app_version);
        standardContent.put("type", "Smartphone");
        metadataContent.put("software", standardContent);
        standardContent = new JSONObject();
        standardContent.put("value", mobile_android_api);
        standardContent.put("type", "android-api");
        metadataContent.put("library", standardContent);
        standardContent = new JSONObject();
        // TODO this mechanism might help to append chunks to respective post 1/2
        // TODO but the ideal mechanism is to send a single huge JSON batch
        // http://stackoverflow.com/questions/17650509/post-huge-json-object-in-android
        //if (batch.size() >= 1)
        //    standardContent.put("value", "[SEGMENTED]");
        //else
        standardContent.put("value", batch.get(0));
        standardContent.put("type", "sensor-data");
        standardContent.put("metadata", metadataContent);
        main.put("data", standardContent);

        standardContent = new JSONObject();
        standardContent.put("value", start_date_time);
        standardContent.put("type", "omh:time-interval");
        //standardContent.put("metadata", "");
        main.put("omh:start_date_time", standardContent);

        standardContent = new JSONObject();
        standardContent.put("value", end_date_time);
        standardContent.put("type", "omh:time-interval");
        //standardContent.put("metadata", "");
        main.put("omh:end_date_time", standardContent);

        jsonAndBatch.add(main);
        jsonAndBatch.add(batch);
        jsonAndBatch.add(uniquePostId);

        return jsonAndBatch;
    }

    // To upload FIWARE JSON documents
    public int uploadFIWAREFile(String mSensorType, String sourceFileUri) throws JSONException {
        HttpURLConnection urlConnection;
        List jsonAndBatch = extract2Parse(sourceFileUri, mSensorType);
        JSONObject data = (JSONObject) jsonAndBatch.get(0);
        List batch = (List) jsonAndBatch.get(1);
        String uniquePostId = (String) jsonAndBatch.get(2);

        Log.v(APP_NAME, "Preparing batch: " + data.toString());
        Log.v(APP_NAME, "Number of elements in batch: " + batch.size());

        try {
            // Open a HTTP  connection to  the URL
            urlConnection = (HttpURLConnection) ((new URL(FIWARE_ORION_PATH + "/" + "entities").openConnection()));
            urlConnection.setDoInput(true); // Allow Inputs
            urlConnection.setDoOutput(true); // Allow Outputs
            urlConnection.setUseCaches(false); // Don't use a Cached Copy
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                urlConnection.setRequestProperty("Connection", "close");
            }

            //Write
            Log.v(APP_NAME, "Writing document");
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, ENCODING));
            writer.write(String.valueOf(data));
            writer.close();
            outputStream.flush();
            outputStream.close();

            //Read
            Log.v(APP_NAME, "Reading document");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), ENCODING));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            Log.v("XXX line", line + "");
            Log.v(APP_NAME, "Closing document");
            bufferedReader.close();

            //close the streams
            Log.v(APP_NAME, sb.toString() + "");

            serverResponseCode = OK;
        } catch (FileNotFoundException e) {
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "FileNotFoundException: " + e);
        } catch (EOFException e) {
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "EOFException: " + e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "MalformedURLException: " + e);
        } catch (Exception e) {
            e.printStackTrace();
            serverResponseCode = ERROR;
            Log.e(APP_NAME, "Got Exception : see logcat");
            Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
        }

        // TODO this mechanism might help to append chunks to respective post 2/2
        // the ideal scenario is that the metadata could be able to store an array of elements,
        // so I can append the number of chunks found
        // {
        //        "metadata": {
        //            [
        //                {
        //                   "name": "chunk1",
        //                   "value": "1231231.213123.123123.12313.123123.13",
        //                   "type": "csv"
        //               },
        //               {
        //                   "name": "chunk2",
        //                       "value": "1231231.213123.123123.12313.123123.13",
        //                       "type": "csv"
        //               }
        //           ]
        //       }
        //   }
        //if (serverResponseCode == OK) sendPuts(uniquePostId, batch);
        Log.v("XXX return from FIWARE", serverResponseCode + "");
        return serverResponseCode;
    }

    // Mechanism to send chunks of data instead of a huge JSON document; which is ideal
    public void sendPuts(String uniquePostId, List batch) {
        HttpURLConnection urlConnection;

        int j = 0;
        while (j < batch.size()) {
            try {
                Log.v(APP_NAME, "Sensor batch: " + batch.get(j) + "");
                // Open a HTTP  connection to  the URL
                urlConnection = (HttpURLConnection) ((new URL(FIWARE_ORION_PATH + "/" + "entities" + "/" + uniquePostId +
                        "/" + "metadata?options=append").openConnection()));
                urlConnection.setDoInput(true); // Allow Inputs
                urlConnection.setDoOutput(true); // Allow Outputs
                urlConnection.setUseCaches(false); // Don't use a Cached Copy
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                    urlConnection.setRequestProperty("Connection", "close");
                }

                //Write
                Log.v(APP_NAME, "Writing document");
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outputStream, ENCODING));
                writer.write(String.valueOf(batch.get(j)));
                writer.close();
                outputStream.flush();
                outputStream.close();

                //Read
                Log.v(APP_NAME, "Reading document");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), ENCODING));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                Log.v(APP_NAME, "Closing document");
                bufferedReader.close();

                //close the streams
                Log.v(APP_NAME, sb.toString() + "");

                serverResponseCode = OK;
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                serverResponseCode = ERROR;
                Log.e(APP_NAME, "MalformedURLException");
            } catch (Exception e) {
                e.printStackTrace();
                serverResponseCode = ERROR;
                Log.e(APP_NAME, "Got Exception : see logcat");
                Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
            }
            j++;
        }
    }
}