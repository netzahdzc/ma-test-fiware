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
    private DatabaseHelper mDbHelper;
    private SessionUtil sessionObj;

    private Cursor mCursorUser;

    private long mUniqueUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_upload);

        final Context context = this;
        final String APP_NAME = "three_ollin_test";
        final TextView upload_data = (TextView) findViewById(R.id.button_manual_upload);
        final TextView data_date = (TextView) findViewById(R.id.remaining_data_date);
        mMessage = new DialogMessageUtils(this);

        mDbHelper = new DatabaseHelper(getApplicationContext());
        sessionObj = new SessionUtil(getApplicationContext());
        sessionObj.openDB();

        mUniqueUserId = sessionObj.getUserSession();
        mCursorUser = readData(mUniqueUserId);
        sessionObj.closeDB();

        LinearLayout account_still_inactive_layout = (LinearLayout) findViewById(R.id.account_still_inactive_layout);
        TextView button_manual_upload = (TextView) findViewById(R.id.button_manual_upload);

        if(checkIfAccountActivated(mCursorUser)) {
            account_still_inactive_layout.setVisibility(View.GONE);
            button_manual_upload.setVisibility(View.VISIBLE);
        }else{
            account_still_inactive_layout.setVisibility(View.VISIBLE);
            button_manual_upload.setVisibility(View.GONE);
        }

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

    // This method reads info from database
    public Cursor readData(long uniqueUserId) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.User._ID,
                DatabaseContract.User.COLUMN_NAME_COL1,
                DatabaseContract.User.COLUMN_NAME_COL2,
                DatabaseContract.User.COLUMN_NAME_COL3,
                DatabaseContract.User.COLUMN_NAME_COL4,
                DatabaseContract.User.COLUMN_NAME_COL5,
                DatabaseContract.User.COLUMN_NAME_COL6,
                DatabaseContract.User.COLUMN_NAME_COL7,
                DatabaseContract.User.COLUMN_NAME_COL8,
                DatabaseContract.User.COLUMN_NAME_COL9,
                DatabaseContract.User.COLUMN_NAME_COL10
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.User._ID + " DESC";

        // Define 'where' part of query.
        String selection = DatabaseContract.User._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueUserId)};

        Cursor c = db.query(
                DatabaseContract.User.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        return c;
    }

    // This method load data to be displayed on screen
    public boolean checkIfAccountActivated(Cursor cursor) {
        boolean mActivationFlag = false;

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {

            int mActivationStatus = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL7)
            );

            if (mActivationStatus == 1) mActivationFlag = true;

        }

        cursor.close();

        return mActivationFlag;
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
