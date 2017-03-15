package com.inger.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inger.android.ollintest.util.ControlDBHandlerUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.Utilities;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class PatientFolderActivity extends AppCompatActivity {

    static final int BIRTHDAY_FORMAT = 1;
    static final int UPDATE_FORMAT = 2;

    final int MALE = 1;
    final int FEMALE = 2;
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
                    if (mPatientGender == MALE)
                        patientPhoto.setImageResource(R.drawable.profile_m);
                    if (mPatientGender == FEMALE)
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
                patientWeightText.setText(mPatientWeight + " " + getResources().getString(R.string.units_kg));

                TextView patientHeightText = (TextView) findViewById(R.id.control_height);
                patientHeightText.setText(mPatientHeight + " " + getResources().getString(R.string.units_cm));

                TextView patientWaistText = (TextView) findViewById(R.id.control_waist);
                patientWaistText.setText(mPatientWaist + " " + getResources().getString(R.string.units_cm));

                TextView patientHeartRateText = (TextView) findViewById(R.id.control_heart_rate);
                patientHeartRateText.setText(mPatientHeartRate + " " + getResources().getString(R.string.units_bpm));

                TextView patientBloodPressSisText = (TextView) findViewById(R.id.control_blood_pressure_sis);
                patientBloodPressSisText.setText(mPatientBloodPressSis + " " + getResources().getString(R.string.units_mmHH));

                TextView patientBloodPressDiaText = (TextView) findViewById(R.id.control_blood_pressure_dia);
                patientBloodPressDiaText.setText(mPatientBloodPressDia + " " + getResources().getString(R.string.units_mmHH));

                TextView patientControlDateText = (TextView) findViewById(R.id.last_updated_control);
                patientControlDateText.setText(PatientUtils.convertFromISO8601(mPatientControlDate, UPDATE_FORMAT));

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
        tugTestText.setText(PatientUtils.convertFromISO8601(testDBObj.
                getLatestTestType(uniquePatientId, WALKING_TEST), UPDATE_FORMAT));

        TextView strengthTestText = (TextView) findViewById(R.id.last_strength_test);
        strengthTestText.setText(PatientUtils.convertFromISO8601(testDBObj.
                getLatestTestType(uniquePatientId, STRENGTH_TEST), UPDATE_FORMAT));

        TextView balanceTestText = (TextView) findViewById(R.id.last_balance_test);
        balanceTestText.setText(PatientUtils.convertFromISO8601(testDBObj.
                getLatestTestType(uniquePatientId, BALANCE_TEST), UPDATE_FORMAT));

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
