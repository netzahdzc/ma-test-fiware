package com.cicese.android.matest.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.cicese.android.matest.DatabaseContract;
import com.cicese.android.matest.DatabaseContractAcc;
import com.cicese.android.matest.DatabaseContractOrient;
import com.cicese.android.matest.R;
import com.cicese.android.matest.util.AccDBHandlerUtils;
import com.cicese.android.matest.util.ControlDBHandlerUtils;
import com.cicese.android.matest.util.Filter;
import com.cicese.android.matest.util.OrientDBHandlerUtils;
import com.cicese.android.matest.util.PatientDBHandlerUtils;
import com.cicese.android.matest.util.TechnicalDBHandlerUtils;
import com.cicese.android.matest.util.TestDBHandlerUtils;
import com.cicese.android.matest.util.UserDBHandlerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UploadToServer extends Service {

    private static final int BACKUP = 1;
    private static final int FIWARE = 2;
    private static final int BACKUP_FIWARE = 3;
    private static final int OK = 200;
    private static final int ERROR = 422;

    /*
    TEST_MODE options:
        INGER; sends data only to BACKUP server keeping FIWARE files on the device.
        FIWARE; sends data only to FIWARE Cloud keeping BACKUP files on the device.
        BACKUP_FIWARE; sends data to both servers.
     */
    private static final int TEST_MODE = BACKUP_FIWARE;
    private static final String APP_NAME = "ma_test";
    private static final String FIWARE_PATH = "fiware";
    private static final String FIWARE_ORION_PATH = "http://207.249.127.152:1026/v2";
    private static final String BACKUP_PATH = "http://207.249.127.152:8000";

    private final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);

    private String upLoadServerUri = BACKUP_PATH + "/sqlite/UploadToServer.php";
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
        if (option.equals("backup") || option.equals("fiware")) {
            // Upload previous loaded list of file to BACKUP server
            // Upload previous loaded list of file to BACKUP server
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
                    if (option.equals("backup") && (TEST_MODE == BACKUP || TEST_MODE == BACKUP_FIWARE)) {
                        if (uploadBACKUPFile(files[i].getPath()) == OK) {
                            // Delete file, since we have already sent them
                            deleteSentFile(files[i].getPath());
                        }
                    }
                    if (option.equals("fiware") && (TEST_MODE == FIWARE || TEST_MODE == BACKUP_FIWARE) ) {
                        try {
                            // TODO Check how to retrieve FIWARE code number instead of this funky solution
                            int structureCounter = 0;
                            structureCounter += uploadFIWAREFile("deviceModel", files[i].getPath());
                            structureCounter += uploadFIWAREFile(mSensorType, files[i].getPath());
                            structureCounter += uploadFIWAREFile("deviceSmartphone", files[i].getPath());
                            structureCounter += uploadFIWAREFile("motorPhysicalTest", files[i].getPath());
                            // TODO Check the error: FileNotFoundException: java.io.FileNotFoundException: http://207.249.127.152:1026/v2/entities
                            // TODO I commented this because, the app does not have control of existent entities
                            //if ( structureCounter == (OK*4) ) {
                                // Delete file, since we have already sent them
                                Log.v("XXX FIWARE para DELETE", files[i].getPath());
                                deleteSentFile(files[i].getPath());
                            //}
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

                    if(TEST_MODE == BACKUP || TEST_MODE == BACKUP_FIWARE){
                        uploadMainFile(files[i].getPath(), newFileName);
                    }

                    if(TEST_MODE == FIWARE || TEST_MODE == BACKUP_FIWARE){
                        uploadMainFileFiware("ControlTests", null, null);
                        uploadMainFileFiware("Patients", null, null);
                        uploadMainFileFiware("Questionnaires", "TUG", null);
                        uploadMainFileFiware("Questionnaires", "Strength", null);
                        uploadMainFileFiware("Questionnaires", "Balance", null);
                        uploadMainFileFiware("Question", "1", null);
                        uploadMainFileFiware("Question", "2", null);
                        uploadMainFileFiware("Question", "3", null);
                        uploadMainFileFiware("Question", "4", null);
                        uploadMainFileFiware("Question", "5", null);
                        uploadMainFileFiware("Question", "6", null);
                        uploadMainFileFiware("Question", "7", null);
                        uploadMainFileFiware("Question", "8", null);
                        uploadMainFileFiware("Question", "9", null);
                        uploadMainFileFiware("Question", "10", null);
                        uploadMainFileFiware("Answer", "1", null);
                        uploadMainFileFiware("Answer", "2", null);
                        uploadMainFileFiware("Answer", "3", null);
                        uploadMainFileFiware("Answer", "4", null);
                        uploadMainFileFiware("Answer", "5", null);
                        uploadMainFileFiware("Answer", "6", null);
                        uploadMainFileFiware("Answer", "7", null);
                        uploadMainFileFiware("Answer", "8", null);
                        uploadMainFileFiware("Answer", "9", null);
                        uploadMainFileFiware("Answer", "10", null);
                        uploadMainFileFiware("Users", null, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startScan(String mSensorType) {
        // TEST_MODE allow skip the sharind data to BACKUP server to avoid
        // data miss-interpretation.
        // Moreover, this section get list with sensor files for BACKUP.
        if (TEST_MODE == BACKUP || TEST_MODE == BACKUP_FIWARE) {
            File[] backupFiles = new Filter().finder(APP_DIRECTORY_PATH + "/" + mSensorType, "db");
            Log.i(APP_NAME, "Elements on queue: " + backupFiles.length + "");
            Log.i(APP_NAME, "sensorType: " + mSensorType);
            processFiles(backupFiles, mSensorType, "backup");
        }

        // This section handle data specifically for the FIWARE cloud.
        if (TEST_MODE == FIWARE || TEST_MODE == BACKUP_FIWARE) {
            File[] fiwareFiles = new Filter().finder(APP_DIRECTORY_PATH + "/" + FIWARE_PATH + "/" + mSensorType, "db");
            Log.i(APP_NAME, "Elements on queue: " + fiwareFiles.length + "");
            Log.i(APP_NAME, "sensorType: " + mSensorType);
            processFiles(fiwareFiles, mSensorType, "fiware");
        }

        // TEST_MODE allow skip the sharind data to BACKUP server to avoid
        // data miss-interpretation
        // This section, allows to get list of main database. The one including user records.
        if (TEST_MODE == BACKUP || TEST_MODE == BACKUP_FIWARE) {
            File[] mainFile = new Filter().finder(APP_DIRECTORY_PATH, "db");
            Log.i(APP_NAME, "Elements on queue: " + mainFile.length + "");
            processFiles(mainFile, mSensorType, "main");
        }
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
                String serverResponseMessage = conn.getResponseMessage();
                Log.v(APP_NAME, "XXX CODE: " + serverResponseCode + "");
                Log.v(APP_NAME, "XXX MESSAGE" + serverResponseMessage);

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
    public int uploadMainFileFiware(String option, String exp1, String exp2) throws JSONException {
        Log.v(APP_NAME, "Processing " + option + " data");
        HttpURLConnection urlConnection;
        JSONObject data = extract2Parse(option, exp1, exp2);

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
            urlConnection.setRequestProperty("Fiware-Service", "matest");
            urlConnection.setRequestProperty("Fiware-ServicePath", "/smartphone");
            // TODO A token must be created right before send the batch of data
            // TODO header must include X-Auth-Token

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

            Log.v(APP_NAME, "XXX line: (uploadMainFileFiware) " + line);

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

    private JSONObject extract2Parse(String option, String exp1, String exp2) throws JSONException {

        ArrayList<String> list;
        JSONObject core = new JSONObject();
        JSONArray entitiesArray = new JSONArray();
        JSONObject main;
        JSONObject standardContent;
        JSONObject metadataContent;


        if (option.equals("ControlTests")) {
            Log.v(APP_NAME, "Processing ControlTests data");
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
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "CTS" + patient_id;
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "ControlTest");

                    standardContent = new JSONObject();
                    standardContent.put("value", getUniqueID().replaceAll("-", "") + "P" + patient_id);
                    main.put("refUser", standardContent);

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
                    standardContent.put("type", "beats-min");
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
                    main.put("dateModified", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        if (option.equals("Patients")) {
            Log.v(APP_NAME, "Processing Patients data");
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

            Cursor mControlPatient = patientDBObj.readAllData();
            if (mControlPatient != null) {
                while (mControlPatient.moveToNext()) {
                    patient_id = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient._ID)
                    );
                    name = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL1)
                    );
                    surname = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL2)
                    );
                    gender = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL3)
                    );
                    birthday = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL4)
                    );
                    trash = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL6)
                    );
                    created = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL7)
                    );
                    lastUpdate = mControlPatient.getString(
                            mControlPatient.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL8)
                    );

                    // Building JSON document
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "P" +  patient_id;
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "Participant");

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
                    standardContent.put("value", lastUpdate);
                    main.put("dateModified", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        if (option.equals("Users")) {
            Log.v(APP_NAME, "Processing Users data");
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
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "U" + user_id;
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "User");

                    standardContent = new JSONObject();
                    standardContent.put("value", name);
                    main.put("name", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", surname);
                    main.put("surname", standardContent);

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
                    standardContent.put("value", lastUpdate);
                    main.put("dateModified", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        // TODO Questionnaires and questions should be created on the server side, then pushed to...
        // the mobile devices, so only answers are managed on the client side

        if (option.equals("Questionnaires")) {
            Log.v(APP_NAME, "Processing Questionnaires data");
            // Setting the batch of control entities
            core.put("actionType", "APPEND");

            String questionnaireType;
            list = new ArrayList<String>();

            switch(exp1){
                case "TUG":{
                    questionnaireType = "Timed Up and Go";
                    list.add(getQuestion("id", exp1, "1"));
                    list.add(getQuestion("id", exp1, "2"));
                    list.add(getQuestion("id", exp1, "3"));
                    list.add(getQuestion("id", exp1, "4"));
                    list.add(getQuestion("id", exp1, "5"));
                    list.add(getQuestion("id", exp1, "6"));
                    list.add(getQuestion("id", exp1, "7"));
                    list.add(getQuestion("id", exp1, "8"));
                    list.add(getQuestion("id", exp1, "9"));
                } break;
                case "Strength":{
                    questionnaireType = "Strength";
                    list.add(getQuestion("id", exp1, "1"));
                    list.add(getQuestion("id", exp1, "2"));
                    list.add(getQuestion("id", exp1, "3"));
                } break;
                case "Balance":{
                    questionnaireType = "Balance";
                    list.add(getQuestion("id", exp1, "1"));
                    list.add(getQuestion("id", exp1, "2"));
                    list.add(getQuestion("id", exp1, "3"));
                } break;
                default: {
                    questionnaireType = "";
                }
            }

            main = new JSONObject();
            main.put("id", "QNNRE1");
            main.put("type", "Questionnaire");

            standardContent = new JSONObject();
            standardContent.put("value", questionnaireType);
            main.put("questionnaireType", standardContent);

            standardContent = new JSONObject();
            standardContent.put("type", "Array");
            standardContent.put("value", new JSONArray(list));
            main.put("refQuestion", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", "Simple test used to assess a persons mobility.");
            main.put("description", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", "2017-01-01T10:10:10.1000Z");
            main.put("dateModified", standardContent);

            entitiesArray.put(main);
        }

        // TODO As previously mentioned, questions should be created on the client side (i.e., a dashboard)
        if (option.equals("Question")) {
            Log.v(APP_NAME, "Processing Question data");
            String uniqueId = "QON"+exp1;

            // Setting the batch of control entities
            core.put("actionType", "APPEND");

            main = new JSONObject();
            main.put("id", uniqueId);
            main.put("type", "Question");

            standardContent = new JSONObject();
            standardContent.put("value", "health");
            main.put("category", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", getQuestion("description", exp1, exp2));
            main.put("value", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", "en");
            main.put("language", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", "2017-01-01T10:10:10.1000Z");
            main.put("dateModified", standardContent);

            entitiesArray.put(main);
        }

        if (option.equals("Answer")) {
            Log.v(APP_NAME, "Processing Answer data");
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

                    String refQuestion, answer;

                    switch(exp1){
                        case "1":{
                            refQuestion = "QON1";
                            answer = question1;
                        } break;
                        case "2":{
                            refQuestion = "QON2";
                            answer = question2;
                        } break;
                        case "3":{
                            refQuestion = "QON3";
                            answer = question3;
                        } break;
                        case "4":{
                            refQuestion = "QON4";
                            answer = question4;
                        } break;
                        case "5":{
                            refQuestion = "QON5";
                            answer = question5;
                        } break;
                        case "6":{
                            refQuestion = "QON6";
                            answer = question6;
                        } break;
                        case "7":{
                            refQuestion = "QON7";
                            answer = question7;
                        } break;
                        case "8":{
                            refQuestion = "QON8";
                            answer = question8;
                        } break;
                        case "9":{
                            refQuestion = "QON9";
                            answer = question9;
                        } break;
                        case "10":{
                            refQuestion = "QON10";
                            answer = question10;
                        } break;
                        default:
                            refQuestion = "";
                            answer = "";
                    }

                    // Building JSON document
                    String uniquePostId = getUniqueID().replaceAll("-", "") + "QON" + exp1 + "T"  + test_id;
                    main = new JSONObject();
                    main.put("id", uniquePostId);
                    main.put("type", "Answer");

                    standardContent = new JSONObject();
                    standardContent.put("value", refQuestion);
                    main.put("refQuestion", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", getUniqueID().replaceAll("-", "") + "P"  + patient_id);
                    main.put("refUser", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", answer);
                    main.put("answer", standardContent);

                    standardContent = new JSONObject();
                    standardContent.put("value", lastUpdate);
                    main.put("dateModified", standardContent);

                    entitiesArray.put(main);
                }
            }
        }

        core.put("entities", entitiesArray);

        return core;
    }

    public String getQuestion(String option, String type, String question){
        String valueQuestion;
        String questionId = "QON" + question;

        Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale("en");
        Resources resources = new Resources(getAssets(), null, conf);

        switch(type){
            case "TUG":{
                switch(question){
                    case "1": {
                        valueQuestion = resources.getString(R.string.walking_question_one).replace("'","");
                    }break;
                    case "2": {
                        valueQuestion = resources.getString(R.string.walking_question_two).replace("'","");
                    }break;
                    case "3": {
                        valueQuestion = resources.getString(R.string.walking_question_three).replace("'","");
                    }break;
                    case "4": {
                        valueQuestion = resources.getString(R.string.walking_question_four).replace("'","");
                    }break;
                    case "5": {
                        valueQuestion = resources.getString(R.string.walking_question_five).replace("'","");
                    }break;
                    case "6": {
                        valueQuestion = resources.getString(R.string.walking_question_six).replace("'","");
                    }break;
                    case "7": {
                        valueQuestion = resources.getString(R.string.walking_question_seven).replace("'","");
                    }break;
                    case "8": {
                        valueQuestion = resources.getString(R.string.walking_question_eight).replace("'","");
                    }break;
                    case "9": {
                        valueQuestion = resources.getString(R.string.walking_question_nine).replace("'","");
                    }break;
                    default:
                        valueQuestion = "";
                }
            } break;
            case "Strength":{
                switch(question){
                    case "1": {
                        valueQuestion = resources.getString(R.string.strength_question_one).replace("'","");
                    }break;
                    case "2": {
                        valueQuestion = resources.getString(R.string.strength_question_two).replace("'","");
                    }break;
                    case "3": {
                        valueQuestion = resources.getString(R.string.strength_question_three).replace("'","");
                    }break;
                    default:
                        valueQuestion = "";
                }
            } break;
            case "Balance":{
                switch(question){
                    case "1": {
                        valueQuestion = resources.getString(R.string.balance_question_one).replace("'","");
                    }break;
                    case "2": {
                        valueQuestion = resources.getString(R.string.balance_question_two).replace("'","");
                    }break;
                    case "3": {
                        valueQuestion = resources.getString(R.string.balance_question_three).replace("'","");
                    }break;
                    default:
                        valueQuestion = "";
                }
            } break;
            default: {
                valueQuestion = "";
            }
        }

        if(option.equals("id"))
            return questionId;
        else
            return valueQuestion;

    }

    // To upload files to BACKUP server
    public int uploadBACKUPFile(String sourceFileUri) {
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
                String serverResponseMessage = conn.getResponseMessage();
                Log.v(APP_NAME, "XXX CODE: " + serverResponseCode + "");
                Log.v(APP_NAME, "XXX MESSAGE" + serverResponseMessage);

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
    //final int BATCH_SIZE = 10;

    private List extract2Parse(String sourceFileUri, String mSensorType) throws JSONException {

        JSONObject main = new JSONObject();
        JSONObject standardContent;
        JSONObject metadataContent;
        List batch = new ArrayList();
        List jsonAndBatch = new ArrayList();
        ArrayList<String> list;

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
                    // TODO It won't be included to fiware, but maybe I should think about it.
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
                    // TODO take care of the remaining less than the BATCH_SIZE. I'm leaving them to hell :P
                    /*if (c == BATCH_SIZE) {
                        batch.add(chunk);
                        c = RESTART_COUNTER;
                    }

                    c++;*/
                }

                // To collect the whole pack of data instead of chunks as previously coded
                batch.add(chunk);

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
                    /*if (c == BATCH_SIZE) {
                        batch.add(chunk);
                        c = RESTART_COUNTER;
                    }

                    c++;*/
                }

                // To collect the whole pack of data instead of chunks as previously coded
                batch.add(chunk);

                mOrientCursor.close();
            }
        }

        // This will get the test_id, so I can retrieve metadata
        if (!mSensorType.equals("orient") && !mSensorType.equals("acc")) {
            try{
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
                    }
                    mAccCursor.close();
                }
            }catch (Exception e) {
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
                    }

                    mOrientCursor.close();
                }
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
        // TODO This data should come from the db instead
        // TODO It is also necesary to check wither hw and fw values are appropiately been address
        String mobile_manufacturer = Build.MANUFACTURER;
        String hardwareVersion = Build.HARDWARE;
        String firmwareVersion = Build.VERSION.RELEASE;


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

        ///////// Building JSON document //////////

        // DEVICE-MODEL
        // This entity will be used as part of the Device-Smartphone entity
        if (mSensorType.equals("deviceModel")) {
            Log.v(APP_NAME, "Working on: deviceModel");
            String uniquePostId = getUniqueID().replaceAll("-", "") + "DM";
            main.put("id", uniquePostId);
            main.put("type", "DeviceModel");

            standardContent = new JSONObject();
            standardContent.put("value", "smartphone");
            main.put("category", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", mobile_brand);
            main.put("brandName", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", mobile_model);
            main.put("modelName", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", mobile_manufacturer);
            main.put("manufacturerName", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", end_date_time);
            main.put("dateCreated", standardContent);

            jsonAndBatch.add(main);
            jsonAndBatch.add(batch);
            jsonAndBatch.add(uniquePostId);
        }

        // DEVICE-SENSOR
        // This entity will be used as part of the Device-Smartphone structure
        if (mSensorType.equals("acc") || mSensorType.equals("orient")) {
            Log.v(APP_NAME, "Working on: sensors");
            String sensor = "";
            if (mSensorType.equals("acc")) sensor = "accelerometer";
            if (mSensorType.equals("orient")) sensor = "orientation";

            String uniquePostId = getUniqueID().replaceAll("-", "") + "" + mSensorType.toUpperCase() + test_id;
            main.put("id", uniquePostId);
            main.put("type", "Device");

            standardContent = new JSONObject();
            standardContent.put("value", "sensor");
            main.put("category", standardContent);

            standardContent = new JSONObject();
            list = new ArrayList<String>();
            list.add("sensing");
            standardContent.put("type", "Array");
            standardContent.put("value", new JSONArray(list));
            main.put("function", standardContent);

            standardContent = new JSONObject();
            list = new ArrayList<String>();
            list.add(sensor);
            standardContent.put("type", "Array");
            standardContent.put("value", new JSONArray(list));
            main.put("controlledProperty", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", hardwareVersion);
            main.put("hardwareVersion", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", firmwareVersion);
            main.put("firmwareVersion", standardContent);

            standardContent = new JSONObject();
            // TODO this mechanism might help to append chunks to respective post 1/2
            // TODO but the ideal mechanism is to send a single huge JSON batch
            // http://stackoverflow.com/questions/17650509/post-huge-json-object-in-android
            //if (batch.size() >= 1)
            //    standardContent.put("value", "[SEGMENTED]");
            //else
            standardContent.put("value", batch.get(0));
            main.put("data", standardContent);

            metadataContent = new JSONObject();
            standardContent = new JSONObject();
            standardContent.put("format", "cvs");
            metadataContent.put("data", standardContent);
            standardContent = new JSONObject();
            standardContent.put("value", sensor_speed);
            standardContent.put("type", "hz");
            metadataContent.put("sampleRate", standardContent);
            standardContent = new JSONObject();
            standardContent.put("value", metadataContent);
            main.put("configuration", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", end_date_time);
            main.put("dateCreated", standardContent);

            jsonAndBatch.add(main);
            jsonAndBatch.add(batch);
            jsonAndBatch.add(uniquePostId);
        }

        // DEVICE-SMARTPHONE
        // This entity can host a number of Device-Sensor entities,
        // it also, makes reference to the device model.
        if (mSensorType.equals("deviceSmartphone")) {
            Log.v(APP_NAME, "Working on: deviceSmartphone");
            String uniquePostId = getUniqueID().replaceAll("-", "") + "DSM" + test_id;
            main.put("id", uniquePostId);
            main.put("type", "Device");

            standardContent = new JSONObject();
            standardContent.put("value", "smartphone");
            main.put("category", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", mobile_android_api);
            main.put("osVersion", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", app_version);
            main.put("softwareVersion", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", hardwareVersion);
            main.put("hardwareVersion", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", firmwareVersion);
            main.put("firmwareVersion", standardContent);

            standardContent = new JSONObject();
            list = new ArrayList<String>();
            list.add(getUniqueID().replaceAll("-", "") + "ACC" + test_id);
            list.add(getUniqueID().replaceAll("-", "") + "ORIENT" + test_id);
            standardContent.put("type", "Array");
            standardContent.put("value", new JSONArray(list));
            main.put("consistOf", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", getUniqueID().replaceAll("-", "") + "DM");
            main.put("refDeviceModel", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", end_date_time);
            main.put("dateCreated", standardContent);

            jsonAndBatch.add(main);
            jsonAndBatch.add(batch);
            jsonAndBatch.add(uniquePostId);
        }

        // MOTOR-PHYSICAL-TEST
        // This entity, holds previous Device-Smartphone strutures; which as overall involve all aforementioned entities (i.e., DeviceModel, Device-Sensor, and Device-Smaartphone)
        if (mSensorType.equals("motorPhysicalTest")) {
            Log.v(APP_NAME, "Working on: motorPhysicalTest");
            String uniquePostId = getUniqueID().replaceAll("-", "") + "MPT" + test_id;
            main.put("id", uniquePostId);
            main.put("type", "MotorPhysicalTest");

            standardContent = new JSONObject();
            standardContent.put("value", getTestTypeOption(Integer.parseInt(test_option)));
            main.put("testType", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", getUniqueID().replaceAll("-", "") + "P"  + patient_id);
            main.put("refUser", standardContent);

            standardContent = new JSONObject();
            list = new ArrayList<String>();
            list.add(getUniqueID().replaceAll("-", "") + "DSM" + test_id);
            standardContent.put("type", "Array");
            standardContent.put("value", new JSONArray(list));
            main.put("refDevice", standardContent);

            metadataContent = new JSONObject();
            standardContent = new JSONObject();
            standardContent.put("device", getUniqueID().replaceAll("-", "") + "DSM" + test_id);
            standardContent.put("position", "lower-back");
            metadataContent.put("data", standardContent);
            metadataContent.put("relationship", "device-limbs");
            standardContent = new JSONObject();
            standardContent.put("value", metadataContent);
            main.put("configuration", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", start_date_time);
            main.put("dateTestStarted", standardContent);

            standardContent = new JSONObject();
            standardContent.put("value", end_date_time);
            main.put("dateTestEnded", standardContent);

            jsonAndBatch.add(main);
            jsonAndBatch.add(batch);
            jsonAndBatch.add(uniquePostId);
        }

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
            urlConnection.setRequestProperty("Fiware-Service", "matest");
            urlConnection.setRequestProperty("Fiware-ServicePath", "/smartphone");

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

            Log.v(APP_NAME, line + "");
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
        Log.v(APP_NAME, "XXX return from FIWARE " + serverResponseCode + "");
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
                urlConnection.setRequestProperty("Fiware-Service", "matest");
                urlConnection.setRequestProperty("Fiware-ServicePath", "/smartphone");

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