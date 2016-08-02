package com.inger.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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
public class TestsActivity extends AppCompatActivity {

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    private TextView button_walking_test;
    private TextView button_strength_test;
    private TextView button_balance_test;

    private long uniquePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_options);

        button_walking_test = (TextView) findViewById(R.id.button_walking_test);
        button_strength_test = (TextView) findViewById(R.id.button_strength_test);
        button_balance_test = (TextView) findViewById(R.id.button_balance_test);

        loadActivityData();
        disableTestButtons();

        button_walking_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tutorialScreen = new Intent(getApplicationContext(), WizardTestActivity.class);
                tutorialScreen.putExtra("testType", WALKING_TEST);
                startActivity(tutorialScreen);
            }
        });

        button_strength_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tutorialScreen = new Intent(getApplicationContext(), WizardTestActivity.class);
                tutorialScreen.putExtra("testType", STRENGTH_TEST);
                startActivity(tutorialScreen);
            }
        });

        button_balance_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tutorialScreen = new Intent(getApplicationContext(), BalanceTestOptionsActivity.class);
                tutorialScreen.putExtra("testType", BALANCE_TEST);
                startActivity(tutorialScreen);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
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
                } else {
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

    public void disableTestButtons() {
        int balanceOptionTest = 0;
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        Cursor cursor = testDBObj.getTodayTest(uniquePatientId);

        button_walking_test.setEnabled(true);
        button_walking_test.setBackgroundColor(getResources().getColor(R.color.ok_button));
        button_strength_test.setEnabled(false);
        button_strength_test.setBackgroundColor(getResources().getColor(R.color.hint));
        button_balance_test.setEnabled(false);
        button_balance_test.setBackgroundColor(getResources().getColor(R.color.hint));

        try {
            cursor.moveToFirst();

            // Reading all data and setting it up to be displayed
            if (cursor != null) {
                do {
                    int testType = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL2)
                    );

                    balanceOptionTest += cursor.getInt(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL3)
                    );

                    if (testType == WALKING_TEST) {
                        button_walking_test.setEnabled(false);
                        button_walking_test.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_strength_test.setEnabled(true);
                        button_strength_test.setBackgroundColor(getResources().getColor(R.color.ok_button));

                        button_balance_test.setEnabled(false);
                        button_balance_test.setBackgroundColor(getResources().getColor(R.color.hint));
                    }

                    if (testType == STRENGTH_TEST) {
                        button_walking_test.setEnabled(false);
                        button_walking_test.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_strength_test.setEnabled(false);
                        button_strength_test.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_balance_test.setEnabled(true);
                        button_balance_test.setBackgroundColor(getResources().getColor(R.color.ok_button));
                    }

                    if (testType == BALANCE_TEST && balanceOptionTest == (TANDEM_TEST_OPTION +
                            SEMI_TANDEM_TEST_OPTION + FEET_TOGETHER_TEST_OPTION + ONE_LEG_TEST_OPTION)) {
                        button_walking_test.setEnabled(false);
                        button_walking_test.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_strength_test.setEnabled(false);
                        button_strength_test.setBackgroundColor(getResources().getColor(R.color.hint));

                        button_balance_test.setEnabled(false);
                        button_balance_test.setBackgroundColor(getResources().getColor(R.color.hint));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
//                cursor.close();
            }
        }
        testDBObj.closeDB();
    }

    public void loadActivityData() {
        PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        patientDBObj.openDB();
        sessionObj.openDB();

        uniquePatientId = sessionObj.getPatientSession();
        Cursor mCursorPatient = patientDBObj.readData(uniquePatientId);

        loadHeaderData(mCursorPatient);

        patientDBObj.closeDB();
        sessionObj.closeDB();
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
