package com.inger.android.ollintest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inger.android.ollintest.util.AccDBHandlerUtils;
import com.inger.android.ollintest.util.DialogMessageUtils;
import com.inger.android.ollintest.util.Filter;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.Utilities;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import static com.inger.android.ollintest.ManualUploadActivity.getDate;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class ConcentratedReportActivity extends AppCompatActivity {

    TextView button_finish_report = null;
    ProgressBar loading_bar = null;
    TextView loading_text = null;

    static final int TIME_FORMAT = 3;
    private static final String APP_NAME = "three_ollin_test";
    private DialogMessageUtils mMessage;
    private final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    private long uniquePatientId;
    private long uniqueTestId;
    private int testType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.concentrated_finish_activity);

        mMessage = new DialogMessageUtils(this);
        uniqueTestId = getIntent().getLongExtra("uniqueTestId", 0);
        loadActivityData(uniqueTestId);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        checkFileWasWrote();

        button_finish_report = (TextView) findViewById(R.id.button_finish_report);
        loading_bar = (ProgressBar) findViewById(R.id.loading);
        loading_text = (TextView) findViewById(R.id.loadingText);

        button_finish_report.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finishTest();

                TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
                testDBObj.openDB();

                Cursor mCursorTest = testDBObj.readData(uniqueTestId);
                testType = testDBObj.getTestType(mCursorTest);

                if(testType == WALKING_TEST || testType == STRENGTH_TEST) {
                    Intent testsScreen = new Intent(ConcentratedReportActivity.this, TestsActivity.class);
                    testsScreen.putExtra("uniquePatientId", uniquePatientId);
                    testsScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(testsScreen, 1);
                } else if(testType == BALANCE_TEST) {
                    Intent balanceTestsOptionsScreen = new Intent(ConcentratedReportActivity.this,
                            BalanceTestOptionsActivity.class);
                    balanceTestsOptionsScreen.putExtra("uniquePatientId", uniquePatientId);
                    balanceTestsOptionsScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(balanceTestsOptionsScreen, 1);
                }

                testDBObj.closeDB();
            }
        });

        final TextView button_cancel = (TextView) findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        uniqueTestId = getIntent().getLongExtra("uniqueTestId", 0);
        loadActivityData(uniqueTestId);
    }

    public void checkFileWasWrote(){
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        Cursor mCursorTest = testDBObj.readData(uniqueTestId);
        testType = testDBObj.getTestType(mCursorTest);

        String dataBaseDate = PatientUtils.convertFromISO8601(
                testDBObj.getLatestTestTypeControl(uniquePatientId, testType), TIME_FORMAT);

        String accFileDate = loadLastDateFiles("acc");
        String orientFileDate = loadLastDateFiles("orient");

        /*AlertDialog diaBox_temp = AskOption(
                "ACC \ndataBaseDate: " + dataBaseDate + ", fileDate: " + accFileDate +
                "\n\nORI \ndataBaseDate: " + dataBaseDate + ", fileDate: " + orientFileDate +
                "\n\nC1: " + dataBaseDate.equalsIgnoreCase(accFileDate) +
                "\nC2: " + dataBaseDate.equalsIgnoreCase(orientFileDate));
        diaBox_temp.show();*/

        if(!dataBaseDate.equalsIgnoreCase(accFileDate) || !dataBaseDate.equalsIgnoreCase(orientFileDate)) {
            AlertDialog diaBox = AskOption(getResources().getString(R.string.file_problem));
            diaBox.show();
        }

        testDBObj.closeDB();
    }

    private AlertDialog AskOption(String message) {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.important))
                .setMessage(message)
                .setIcon(R.mipmap.ic_warning)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }

                })
                .create();
        return myQuittingDialogBox;
    }

    public String loadLastDateFiles(String directory){
        // Create  directory if it doesn't exist
        File dir = new File (APP_DIRECTORY_PATH + "/" + directory);
        if (!dir.exists())  dir.mkdirs();

        String outcome = getResources().getString(R.string.remaining_data_date);
        File[] files = new Filter().finder(APP_DIRECTORY_PATH + "/" + directory , "db");
        Arrays.sort(files);

        // Upload previous loaded list
        for (int i = 0; i < files.length; i++) {
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

    // This method load data to be displayed on screen
    public void loadReportData(Cursor cursor) {
        try {
            // Reading all data and setting it up to be displayed
            if (cursor.moveToFirst()) {
                String mUniqueTestId = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Test._ID)
                );

                int mTestType = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL2)
                );

                int mBalanceTestOption = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL3)
                );

                String mTestScore = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL16)
                );

                TextView uniqueTestId = (TextView) findViewById(R.id.concentrated_record_id);
                uniqueTestId.setText(mUniqueTestId);

                TextView testTypeText = (TextView) findViewById(R.id.concentrated_test_type);
                String testTypeDummy = "";
                String balanceTestOptionDummy = "";

                if (mTestType == WALKING_TEST)
                    testTypeDummy = getResources().getString(R.string.button_walking_test);
                if (mTestType == STRENGTH_TEST)
                    testTypeDummy = getResources().getString(R.string.button_strength_test);
                if (mTestType == BALANCE_TEST) {
                    testTypeDummy = getResources().getString(R.string.button_balance_test);

                    if (mBalanceTestOption == TANDEM_TEST_OPTION)
                        balanceTestOptionDummy = getResources().getString(R.string.button_tandem_test);
                    if (mBalanceTestOption == SEMI_TANDEM_TEST_OPTION)
                        balanceTestOptionDummy = getResources().getString(R.string.button_semi_tandem_test);
                    if (mBalanceTestOption == FEET_TOGETHER_TEST_OPTION)
                        balanceTestOptionDummy = getResources().getString(R.string.button_feet_together);
                    if (mBalanceTestOption == ONE_LEG_TEST_OPTION)
                        balanceTestOptionDummy = getResources().getString(R.string.button_one_leg_test);

                    balanceTestOptionDummy = "(" + balanceTestOptionDummy + ")";
                }

                testTypeDummy += balanceTestOptionDummy;
                testTypeText.setText(testTypeDummy);

                TextView testScoreText = (TextView) findViewById(R.id.concentrated_test_score);
                testScoreText.setText(mTestScore + getResources().getString(R.string.suffix_score));
            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
//                cursor.close();
            }
        }
    }

    // This method load data to be displayed on screen
    public void loadHeaderData(Cursor cursor) {
        try {
            // Reading all data and setting it up to be displayed
            if (cursor.moveToFirst()) {
                String mPatientName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL1)
                );

                String mPatientSurname = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL2)
                );

                int mPatientGender = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL3)
                );

                String mPatientBirthday = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL4)
                );

                byte[] mPatientPhoto = cursor.getBlob(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL5)
                );

                ImageView patientPhoto = (ImageView) findViewById(R.id.header_patient_photo);
                if (mPatientPhoto == null) {
                    if (mPatientGender == 1)
                        patientPhoto.setImageResource(R.drawable.profile_m);
                    if (mPatientGender == 2)
                        patientPhoto.setImageResource(R.drawable.profile_w);
                } else{
                    patientPhoto.setImageBitmap(Utilities.getImage(mPatientPhoto));
                }

                TextView patientNameText = (TextView) findViewById(R.id.header_patient_name);
                patientNameText.setText(PatientUtils.getFormatName(mPatientName + " " + mPatientSurname));

                TextView patientAgeText = (TextView) findViewById(R.id.header_patient_age);
                patientAgeText.setText(PatientUtils.getAge(mPatientBirthday) +
                        getResources().getString(R.string.suffix_year));
            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
//                cursor.close();
            }
        }
    }

    public void loadActivityData(long mUniqueTestId) {
        PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        patientDBObj.openDB();
        sessionObj.openDB();

        uniquePatientId = sessionObj.getPatientSession();

        Cursor mCursorPatient = patientDBObj.readData(uniquePatientId);
        loadHeaderData(mCursorPatient);


        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();
        Cursor mCursorTest = testDBObj.readData(mUniqueTestId);
        testType = testDBObj.getTestType(mCursorTest);
        loadReportData(mCursorTest);
        testDBObj.closeDB();

        patientDBObj.closeDB();
        sessionObj.closeDB();
    }

    public void finishTest() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, 0, 0, "", "", "", "", "", "", "",
                "", "", "", "", 0, "", "testCompleted");

        testDBObj.closeDB();
    }

    @Override
    public void onBackPressed() {
    }

    Handler h = new Handler();
    int delay = 60000; //60 seconds
    Runnable runnable;
    private AccDBHandlerUtils accDBObj;

    // Disable buttons until there is no more data recording on the database
    @Override
    protected void onStart() {
        //accDBObj = new AccDBHandlerUtils(getApplicationContext());
        //accDBObj.openDB();
        //Log.v("Last input", "Last input 00 " );

        // Disabling buttons
        loading_bar.setMax(delay);
        button_finish_report.setEnabled(false);
        button_finish_report.setBackgroundColor(getResources().getColor(R.color.hint));

        new CountDownTimer(delay, 100) {
            int secondsLeft = 1;

            public void onTick(long millisUntilFinished) {
                if (Math.round((float) millisUntilFinished) != secondsLeft) {
                    secondsLeft = Math.round((float) millisUntilFinished);
                    loading_bar.setProgress(delay-secondsLeft);
                    //Log.v("Last input", "Seconds left " + (delay-secondsLeft));
                }
            }

            @Override
            public void onFinish() {
                // Do nothing
            }
        }.start();

        // Start handler as activity become visible
        h.postDelayed(new Runnable() {
            public void run() {
                //Cursor cursor = accDBObj.readLastData();
                //double lastInput = 0.0, previousLastInput = 0.0;
                try {
                    //Log.v("Last input", "Last input 22 ");
                    /*
                    cursor.moveToFirst();

                    // Reading all data and setting it up to be displayed
                    if (cursor != null) {
                        do {
                            lastInput = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7));
                            Log.v("Last input", "Last input AA: " + lastInput);
                        } while (cursor.moveToNext());
                    }

                    // Enable button untill we ensure all records have been stored
                    if(lastInput == previousLastInput){
                        button_finish_report.setEnabled(true);
                        button_finish_report.setBackgroundColor(getResources().getColor(R.color.ok_button));
                        Log.v("Last input", "Last input CC: " + lastInput);
                    }else{
                        previousLastInput = lastInput;
                        Log.v("Last input", "Last input BB: " + previousLastInput);
                    }*/
                    button_finish_report.setEnabled(true);
                    button_finish_report.setBackgroundColor(getResources().getColor(R.color.ok_button));

                    loading_bar.setVisibility(View.INVISIBLE);
                    loading_text.setVisibility(View.INVISIBLE);

                } catch (Exception e) {
                    // exception handling
                    Log.v("Last input", "Last input ZZ: " + e.toString());
                } finally {
                    /*if (cursor != null) {
                        //cursor.close();
                    }*/
                }

                runnable=this;

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.v("Last input", "PAUSE" );
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
}
