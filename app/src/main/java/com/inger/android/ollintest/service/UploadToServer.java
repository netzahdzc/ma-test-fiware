package com.inger.android.ollintest.service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inger.android.ollintest.util.Filter;

public class UploadToServer extends Service {
    private static final String APP_NAME = "three_ollin_test";
    private final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);


    private int serverResponseCode = 0;
    private String upLoadServerUri =
            "http://189.209.180.190/investigacion/sqlite/UploadToServer.php";

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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public int onStartCommand(Intent intent , int flags , int startId) {
        super.onStartCommand(intent, flags , startId);

        Bundle b = intent.getExtras();
        String sensorType = b.getString("sensorType");
        Log.i(APP_NAME, "I'm on IBinder and I'm carrying this value: " + sensorType);

        // We start checking into directories to retrieve and send file to server-side
        startScan(sensorType);


        return START_REDELIVER_INTENT;
    }

    private void startScan(String mSensorType){
        // Get list with acc files
        File[] files = new Filter().finder(APP_DIRECTORY_PATH + "/" + mSensorType, "db");
        Log.i(APP_NAME, "Elements on queue: " + files.length + "");
        Log.i(APP_NAME, "sensorType: " + mSensorType);

        // Upload previous loaded list
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
                if (uploadFile(files[i].getPath()) == 200) {
                    deleteSentFile(files[i].getPath());
                }
                Log.v(APP_NAME, "File was sent because already past 10 min");
            } else {
                Log.v(APP_NAME, "File NO sent because files still fresh");
            }
        }

        // Get list with sensor data
        File[] mainFile = new Filter().finder(APP_DIRECTORY_PATH, "db");
        Log.i(APP_NAME, files.length + "");

        // Upload previous loaded list
        for (int i = 0; i < mainFile.length; i++) {
            // Renaming file to keep a track of the new one
            String[] dbNamePath = mainFile[i].getPath().split("\\.");
            String newFileName = dbNamePath[0] + "_" + System.currentTimeMillis() + "." + dbNamePath[1];
            uploadFile(mainFile[i].getPath(), newFileName);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int uploadFile(String sourceFileUri, String newName) {
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

                if (serverResponseCode == 200) {
                    Log.i(APP_NAME, "File Upload Complete: " + serverResponseCode);
                }

                //close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();

                return serverResponseCode;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();

                Log.e(APP_NAME, "MalformedURLException");
            } catch (Exception e) {
                e.printStackTrace();

                Log.e(APP_NAME, "Got Exception : see logcat");
                Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }

    public int uploadFile(String sourceFileUri) {
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
                // String serverResponseMessage = conn.getResponseMessage();

                if (serverResponseCode == 200) {
                    Log.i(APP_NAME, "File Upload Complete: " + serverResponseCode);
                }

                //close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();

                return serverResponseCode;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();

                Log.e(APP_NAME, "MalformedURLException");
            } catch (Exception e) {
                e.printStackTrace();

                Log.e(APP_NAME, "Got Exception : see logcat");
                Log.e(APP_NAME, "Exception : " + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }
}