package com.inger.android.ollintest;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inger.android.ollintest.service.UploadToServer;
import com.inger.android.ollintest.util.DateUtil;
import com.inger.android.ollintest.util.DialogMessageUtils;
import com.inger.android.ollintest.util.Filter;
import com.inger.android.ollintest.util.SessionUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class ManualUploadActivity extends AppCompatActivity {

    private static final String APP_NAME = "three_ollin_test";
    private final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);
    private DialogMessageUtils mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_upload);

        final Context context = this;
        final String APP_NAME = "three_ollin_test";
        final TextView upload_data = (TextView) findViewById(R.id.button_manual_upload);
        final TextView data_date = (TextView) findViewById(R.id.remaining_data_date);
        mMessage = new DialogMessageUtils(this);

        data_date.setText(loadLastDateFiles());

        upload_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ConnectivityManager connManager = (ConnectivityManager) getApplication().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                    // Message to show that data is been sent
                    data_date.setText(getResources().getString(R.string.loading));

                    // This sections is dedicated to start sending data to server-side
                    Log.v(APP_NAME, "sending data to server");

                    // To fetch accelerometer data
                    Intent i = new Intent(getApplicationContext(), UploadToServer.class);
                    i.putExtra("sensorType", "acc");
                    context.startService(i);
                    context.stopService(i);

                    // To fetch orientation data
                    Intent ii = new Intent(getApplicationContext(), UploadToServer.class);
                    ii.putExtra("sensorType", "orient");
                    context.startService(ii);
                    context.stopService(ii);

                    new CountDownTimer(5000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            data_date.setText(data_date.getText()+".");
                        }
                        public void onFinish() {
                            data_date.setText(loadLastDateFiles());
                        }
                    }.start();
                }else{
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.important), //Title
                            getResources().getString(R.string.wifi_problem), //Body message
                            false //To close current Activity when confirm
                    );
                }
            }
        });
    }

    public String loadLastDateFiles(){
        String outcome = getResources().getString(R.string.remaining_data_date);
        File[] files = new Filter().finder(APP_DIRECTORY_PATH + "/acc", "db");
        Arrays.sort(files, Collections.reverseOrder());

        // Upload previous loaded list
        for (int i = 0; i < files.length; i++) {
            Log.i(APP_NAME, "Current element to analise: " + files[i].getPath() + "");

            /** In order to ensure that we are not sending (uploading & deleting) any file
             * currently been used to store acc data. This section, compare epoch time to filter
             * new ones. Thus, we only send (upload & delete) files older than 10 min. Which is
             * enough time to ensure that at least 1 patient has already finish his tests.
             */
            String[] fileNameChunks = files[i].getPath().split("\\.");
            String[] fileTime = fileNameChunks[0].split("/");
            String[] time = fileTime[fileTime.length - 1].split("_");

            long fileTimeMilliseconds = Long.parseLong(time[time.length - 1]);

            outcome = getDate(fileTimeMilliseconds, "dd/MM/yyyy HH:mm");
        }

        return outcome;
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
            break;
        }
        return true;
    }
}
