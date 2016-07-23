package com.example.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.util.AccDBHandlerUtils;
import com.example.android.ollintest.util.ControlDBHandlerUtils;
import com.example.android.ollintest.util.DateUtil;
import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.PatientUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.TechnicalDBHandlerUtils;
import com.example.android.ollintest.util.TestDBHandlerUtils;

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

    private Chronometer crono = null;

    private long uniquePatientId;
    private int testType;
    private int balanceTestOption;
    private long uniqueTestId;
    private long lastPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.count_down);

        testType = getIntent().getIntExtra("testType", 0);
        balanceTestOption = getIntent().getIntExtra("balanceTestOption", 0);

        loadActivityData(testType, balanceTestOption);

        crono = (Chronometer) findViewById(R.id.chronometer);
        crono.setVisibility(View.GONE);

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
                new CountDownTimer(5000, 1000) {
                    TextView counter_text = (TextView) findViewById(R.id.counter_text);

                    public void onTick(long millisUntilFinished) {
                        counter_text.setText("" + millisUntilFinished / 1000);
                        button_cancel.setEnabled(false);
                        button_cancel.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_start_counter.setVisibility(View.GONE);

                        button_stop_counter.setEnabled(false);
                        button_stop_counter.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_stop_counter.setVisibility(View.VISIBLE);
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

                        startChronometer();
                        createTestFile();
                        startCollectingAccData();
                    }
                }.start();
            }
        });

        button_stop_counter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopChronometer();

                button_cancel.setVisibility(View.GONE);
                button_finish.setVisibility(View.VISIBLE);
                button_stop_counter.setVisibility(View.GONE);
                button_restart_counter.setVisibility(View.VISIBLE);

                stopCollectingAccData();
            }
        });

        button_finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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

                String mPatientBirthday = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL4)
                );

                byte[] mPatientPhoto = cursor.getBlob(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL5)
                );

                ImageView patientPhoto = (ImageView) findViewById(R.id.header_patient_photo);
                patientPhoto.setImageBitmap(Utilities.getImage(mPatientPhoto));

                TextView patientNameText = (TextView) findViewById(R.id.header_patient_name);
                patientNameText.setText(PatientUtils.getFormatName(mPatientName + " " + mPatientSurname));

                TextView patientAgeText = (TextView) findViewById(R.id.header_patient_age);
                patientAgeText.setText(PatientUtils.getAgeName(mPatientBirthday));

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

    public void createTestFile() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        TechnicalDBHandlerUtils techDBObj = new TechnicalDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();
        techDBObj.openDB();

        uniqueTestId = testDBObj.insertData(uniquePatientId, testType, balanceTestOption,
                null, null, null, null, null, null, null, null, null, null, null, 0, null, "sensorStarted");
        techDBObj.insertData(uniquePatientId, uniqueTestId, "techMobileModel", "techMobileBrand",
                "techMobileAndroidApi", "techAppVersion", "techAccModel");

        testDBObj.closeDB();
        techDBObj.closeDB();
    }

    public void startCollectingAccData() {
        AccDBHandlerUtils accDBObj = new AccDBHandlerUtils(getApplicationContext());
        accDBObj.openDB();

        accDBObj.insertData(uniquePatientId, uniqueTestId, "accTimestamp", "accAccuracy",
                "accX", "accY", "accZ", "accType");

        accDBObj.closeDB();
    }

    public void stopCollectingAccData() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, 0, 0, "", "", "", "", "", "", "",
                "", "", "", "", 0, "", "sensorStopped");

        testDBObj.closeDB();
    }

    public void restartCollectingAccData() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        AccDBHandlerUtils accDBObj = new AccDBHandlerUtils(getApplicationContext());
        accDBObj.openDB();
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, 0, 0, "", "", "", "", "", "", "",
                "", "", "", "", 0, "", "sensorRestarted");

        accDBObj.insertData(uniquePatientId, uniqueTestId, "accTimestamp", "accAccuracy",
                "accX", "accY", "accZ", "accType");

        testDBObj.closeDB();
        accDBObj.closeDB();
    }

    public void finishCollectingData() {
        DateUtil dateObj = new DateUtil();
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, 0, 0, dateObj.getCurrentDate(), "", "", "",
                "", "", "", "", "", "", "", 0, "", "sensorFinished");

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