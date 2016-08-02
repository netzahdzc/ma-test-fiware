package com.inger.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.Utilities;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class ConcentratedReportActivity extends AppCompatActivity {

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

        uniqueTestId = getIntent().getLongExtra("uniqueTestId", 0);
        loadActivityData(uniqueTestId);

        final TextView button_finish_report = (TextView) findViewById(R.id.button_finish_report);
        button_finish_report.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finishTest();

                TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
                testDBObj.openDB();

                Cursor mCursorTest = testDBObj.readData(uniqueTestId);
                testType = testDBObj.getTestType(mCursorTest);

                if (testType == WALKING_TEST || testType == STRENGTH_TEST) {
                    Intent testsScreen = new Intent(ConcentratedReportActivity.this, TestsActivity.class);
                    testsScreen.putExtra("uniquePatientId", uniquePatientId);
                    testsScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(testsScreen, 1);
                } else if (testType == BALANCE_TEST) {
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

}
