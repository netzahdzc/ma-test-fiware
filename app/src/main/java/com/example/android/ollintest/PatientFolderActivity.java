package com.example.android.ollintest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.util.ControlDBHandlerUtils;
import com.example.android.ollintest.util.DialogMessageUtils;
import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.PatientUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.TestDBHandlerUtils;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class PatientFolderActivity extends AppCompatActivity {

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    private long uniquePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_folder);

        loadActivityData();

        final TextView button_control = (TextView) findViewById(R.id.button_control);
        button_control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent patientControlScreen = new Intent(getApplicationContext(), PatientControlActivity.class);
                startActivity(patientControlScreen);
            }
        });

        final TextView button_test = (TextView) findViewById(R.id.button_test);
        button_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent testsScreen = new Intent(getApplicationContext(), TestsActivity.class);
                startActivity(testsScreen);
            }
        });

        final TextView button_update_patient_profile = (TextView) findViewById(R.id.button_update_patient_profile);
        button_update_patient_profile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent updateScreen = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(updateScreen);
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
                cursor.close();
            }
        }
    }

    // This method load data to be displayed on screen
    public void loadControlData(Cursor cursor) {
        try {
            // Reading all data and setting it up to be displayed
            if (cursor.moveToFirst()) {
                String mPatientWeight = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL2)
                );

                String mPatientHeight = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL3)
                );

                String mPatientWaist = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL4)
                );

                String mPatientHeartRate = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL5)
                );

                String mPatientBloodPressSis = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL6)
                );

                String mPatientBloodPressDia = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL7)
                );

                String mPatientControlDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Control.COLUMN_NAME_COL8)
                );

                TextView patientWeightText = (TextView) findViewById(R.id.control_weight);
                patientWeightText.setText(mPatientWeight);

                TextView patientHeightText = (TextView) findViewById(R.id.control_height);
                patientHeightText.setText(mPatientHeight);

                TextView patientWaistText = (TextView) findViewById(R.id.control_waist);
                patientWaistText.setText(mPatientWaist);

                TextView patientHeartRateText = (TextView) findViewById(R.id.control_heart_rate);
                patientHeartRateText.setText(mPatientHeartRate);

                TextView patientBloodPressSisText = (TextView) findViewById(R.id.control_blood_pressure_sis);
                patientBloodPressSisText.setText(mPatientBloodPressSis);

                TextView patientBloodPressDiaText = (TextView) findViewById(R.id.control_blood_pressure_dia);
                patientBloodPressDiaText.setText(mPatientBloodPressDia);

                TextView patientControlDateText = (TextView) findViewById(R.id.last_updated_control);
                patientControlDateText.setText(mPatientControlDate);

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
    public void loadRecordData(long uniquePatientId) {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        TextView tugTestText = (TextView) findViewById(R.id.last_tug_test);
        tugTestText.setText(testDBObj.getLatestTestType(uniquePatientId, WALKING_TEST));

        TextView balanceTestText = (TextView) findViewById(R.id.last_strength_test);
        balanceTestText.setText(testDBObj.getLatestTestType(uniquePatientId, STRENGTH_TEST));

        TextView strengthTestText = (TextView) findViewById(R.id.last_balance_test);
        strengthTestText.setText(testDBObj.getLatestTestType(uniquePatientId, BALANCE_TEST));

        testDBObj.closeDB();
    }

    public void loadActivityData() {
        PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
        ControlDBHandlerUtils controlDBObj = new ControlDBHandlerUtils(getApplicationContext());
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());

        patientDBObj.openDB();
        controlDBObj.openDB();
        sessionObj.openDB();

        uniquePatientId = sessionObj.getPatientSession();
        Cursor mCursorPatient = patientDBObj.readData(uniquePatientId);
        Cursor mCursorControl = controlDBObj.readData(uniquePatientId);

        loadHeaderData(mCursorPatient);
        loadControlData(mCursorControl);
        loadRecordData(uniquePatientId);

        patientDBObj.closeDB();
        controlDBObj.closeDB();
        sessionObj.closeDB();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
