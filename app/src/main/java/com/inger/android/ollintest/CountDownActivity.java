package com.inger.android.ollintest;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.inger.android.ollintest.service.BackgroundAudioService;
import com.inger.android.ollintest.service.MotionSensors;
import com.inger.android.ollintest.util.DateUtil;
import com.inger.android.ollintest.util.DialogMessageUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TechnicalDBHandlerUtils;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.Utilities;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class CountDownActivity extends AppCompatActivity {

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    private DialogMessageUtils mMessage;
    private Chronometer crono = null;

    private Intent accService;
    private Intent playbackServiceIntent;
    private long uniquePatientId;
    private int testType;
    private int balanceTestOption;
    private long uniqueTestId;
    private long lastPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.count_down);

        accService = new Intent(getApplicationContext(), MotionSensors.class);
        mMessage = new DialogMessageUtils(this);

        testType = getIntent().getIntExtra("testType", 0);
        balanceTestOption = getIntent().getIntExtra("balanceTestOption", 0);

        loadActivityData(testType, balanceTestOption);
        playbackServiceIntent = new Intent(this, BackgroundAudioService.class);

        final TextView button_cancel = (TextView) findViewById(R.id.button_cancel);
        button_cancel.setEnabled(true);

        button_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        final TextView button_start_counter = (TextView) findViewById(R.id.button_start_counter);

        final TextView button_stop_counter = (TextView) findViewById(R.id.button_stop_counter);
        button_stop_counter.setVisibility(View.GONE);

        final TextView button_restart_counter = (TextView) findViewById(R.id.button_restart_counter);
        button_restart_counter.setVisibility(View.GONE);

        final TextView button_finish = (TextView) findViewById(R.id.button_finish);
        button_finish.setVisibility(View.GONE);

        button_start_counter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Identificar si después de haberse activado la escucha de los sensores, el dispositivo esta almacenando la info
//                Log.v("ACC XXX", "xxxxxxx _ " + "button_start_counter");
                crono = (Chronometer) findViewById(R.id.chronometer);
                crono.setVisibility(View.GONE);

                new CountDownTimer(5000, 100) {
                    TextView counter_text = (TextView) findViewById(R.id.counter_text);
                    int secondsLeft = 1;

                    public void onTick(long millisUntilFinished) {
                        if (Math.round((float) millisUntilFinished / 1000.0f) != secondsLeft) {
                            secondsLeft = Math.round((float) millisUntilFinished / 1000.0f);
                            if (secondsLeft >= 1) {
                                counter_text.setText("" + secondsLeft);
                                button_cancel.setEnabled(false);
                                button_cancel.setBackgroundColor(getResources().getColor(R.color.hint));

                                button_start_counter.setVisibility(View.GONE);

                                button_stop_counter.setEnabled(false);
                                button_stop_counter.setBackgroundColor(getResources().getColor(R.color.hint));
                                button_stop_counter.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    public void onFinish() {
                        button_cancel.setEnabled(false);
                        button_cancel.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_start_counter.setVisibility(View.GONE);

                        button_stop_counter.setEnabled(true);
                        button_stop_counter.setBackgroundColor(getResources().getColor(R.color.ok_button));
                        button_stop_counter.setVisibility(View.VISIBLE);

                        crono.setVisibility(View.VISIBLE);
                        counter_text.setVisibility(View.GONE);

                        playAlarm();
                        startChronometer();

                        playbackServiceIntent.putExtra("testType", testType);
                        startService(playbackServiceIntent);

                        try {
                            createTestFile();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        startCollectingAccData();
                    }
                }.start();
            }
        });

        button_stop_counter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Log.v("ACC XXX", "xxxxxxx _ " + "button_stop_counter");
                stopChronometer();

                button_cancel.setVisibility(View.GONE);
                button_finish.setVisibility(View.VISIBLE);
                button_stop_counter.setVisibility(View.GONE);
                button_restart_counter.setVisibility(View.VISIBLE);

                stopCollectingAccData();
                stopService(playbackServiceIntent);
            }
        });

        button_finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Log.v("ACC XXX", "xxxxxxx _ " + "button_finish");
                PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
                patientDBObj.openDB();

                if (testType == WALKING_TEST) {
                    Intent walkingEvaluationListScreen = new Intent(getApplicationContext(), WalkingEvaluationListActivity.class);
                    walkingEvaluationListScreen.putExtra("uniqueTestId", uniqueTestId);
                    startActivity(walkingEvaluationListScreen);
                }

                if (testType == STRENGTH_TEST) {
                    Intent strengthEvaluationListScreen = new Intent(getApplicationContext(), StrengthEvaluationListActivity.class);
                    strengthEvaluationListScreen.putExtra("uniqueTestId", uniqueTestId);
                    startActivity(strengthEvaluationListScreen);
                }

                if (testType == BALANCE_TEST) {
                    Intent balanceEvaluationListScreen = new Intent(getApplicationContext(), BalanceEvaluationListActivity.class);
                    balanceEvaluationListScreen.putExtra("uniqueTestId", uniqueTestId);
                    startActivity(balanceEvaluationListScreen);
                }

                patientDBObj.closeDB();
                finishCollectingData();
                stopService(playbackServiceIntent);
            }
        });

        button_restart_counter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                restartChronometer();

                button_cancel.setVisibility(View.VISIBLE);
                button_finish.setVisibility(View.GONE);
                button_stop_counter.setVisibility(View.VISIBLE);
                button_restart_counter.setVisibility(View.GONE);

                restartCollectingAccData();
            }
        });
    }

    public void playAlarm() {
        MediaPlayer mp = MediaPlayer.create(CountDownActivity.this, R.raw.long_beep);
        mp.start();
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
                } else
                    patientPhoto.setImageBitmap(Utilities.getImage(mPatientPhoto));

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

    public void setTitle(int testType, int testTypeOption) {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        String title = "";
        String titleOption = "";

        switch (testType) {
            case WALKING_TEST: {
                title = getResources().getString(R.string.label_tug_test_name);
            }
            break;
            case STRENGTH_TEST: {
                title = getResources().getString(R.string.label_strength_test_name);
            }
            break;
            case BALANCE_TEST: {
                title = getResources().getString(R.string.label_balance_test_name);
                switch (testTypeOption) {
                    case TANDEM_TEST_OPTION: {
                        titleOption = " - (" +
                                getResources().getString(R.string.label_balance_tandem_option)
                                + ")";
                    }
                    break;
                    case SEMI_TANDEM_TEST_OPTION: {
                        titleOption = " - (" +
                                getResources().getString(R.string.label_balance_semi_tandem_option)
                                + ")";
                    }
                    break;
                    case FEET_TOGETHER_TEST_OPTION: {
                        titleOption = " - (" +
                                getResources().getString(R.string.label_balance_semi_feet_together_option)
                                + ")";
                    }
                    break;
                    case ONE_LEG_TEST_OPTION: {
                        titleOption = " - (" +
                                getResources().getString(R.string.label_balance_semi_feet_one_leg_option)
                                + ")";
                    }
                    break;
                    default:
                }
            }
            break;
            default:
        }
        ab.setTitle(title + titleOption);
    }

    public void createTestFile() throws PackageManager.NameNotFoundException {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        TechnicalDBHandlerUtils techDBObj = new TechnicalDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();
        techDBObj.openDB();

        uniqueTestId = testDBObj.insertData(uniquePatientId, testType, balanceTestOption,
                null, null, null, null, null, null, null, null, null, null, null, 0, null, "sensorStarted");

        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        String version = pInfo.versionName;
        int verCode = pInfo.versionCode;

        techDBObj.insertData(uniquePatientId, uniqueTestId, android.os.Build.MODEL, android.os.Build.MANUFACTURER,
                String.valueOf(android.os.Build.VERSION.SDK_INT), "vN" + version + "_vC" + verCode);

        testDBObj.closeDB();
        techDBObj.closeDB();
    }

    public void startCollectingAccData() {
//        Log.v("ACC XXX", "xxxxxxx _ " + "startCollectingAccData");
        if (uniqueTestId != 0)
            startService(accService);
        else
            mMessage.dialogWarningMessage(
                    getResources().getString(R.string.important), //Title
                    getResources().getString(R.string.something_wrong_with_sensors), //Body message
                    false //To close current Activity when confirm
            );

    }

    public void stopCollectingAccData() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, testType, balanceTestOption, "", "", "", "", "", "", "",
                "", "", "", "", 0, "", "sensorStopped");

        stopService(accService);

        testDBObj.closeDB();
    }

    public void restartCollectingAccData() {
        if (uniqueTestId != 0) {
            TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
            testDBObj.openDB();

            testDBObj.updateData(uniqueTestId, 0, testType, balanceTestOption, "", "", "", "", "", "", "",
                    "", "", "", "", 0, "", "sensorRestarted");

            testDBObj.closeDB();

            startService(new Intent(CountDownActivity.this, MotionSensors.class));
        } else
            mMessage.dialogWarningMessage(
                    getResources().getString(R.string.important), //Title
                    getResources().getString(R.string.something_wrong_with_sensors), //Body message
                    false //To close current Activity when confirm
            );
    }

    public void finishCollectingData() {
        DateUtil dateObj = new DateUtil();
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, testType, balanceTestOption, dateObj.getCurrentDate(), "", "", "",
                "", "", "", "", "", "", "", 0, "", "sensorFinished");

        stopService(accService);

        testDBObj.closeDB();
    }

    public void loadActivityData(int testType, int balanceTestOption) {
        setTitle(testType, balanceTestOption);

        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
        sessionObj.openDB();
        patientDBObj.openDB();

        uniquePatientId = sessionObj.getPatientSession();
        Cursor mCursorPatient = patientDBObj.readData(uniquePatientId);

        loadHeaderData(mCursorPatient);

        patientDBObj.closeDB();
        sessionObj.closeDB();
    }

    public void startChronometer() {
        crono.setBase(SystemClock.elapsedRealtime());
        crono.start();
    }

    public void restartChronometer() {
        crono.setBase(crono.getBase() + SystemClock.elapsedRealtime() - lastPause);
        crono.start();
    }

    public void stopChronometer() {
        lastPause = SystemClock.elapsedRealtime();
        crono.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }
}
