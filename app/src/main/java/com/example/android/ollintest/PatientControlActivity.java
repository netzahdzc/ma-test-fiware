package com.example.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.util.ControlDBHandlerUtils;
import com.example.android.ollintest.util.DialogMessageUtils;
import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.PatientUtils;
import com.example.android.ollintest.util.SessionUtil;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class PatientControlActivity extends AppCompatActivity {

    private DialogMessageUtils mMessage;

    private long uniquePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);

        mMessage = new DialogMessageUtils(this);

        loadActivityData();

        final TextView save_button = (TextView) findViewById(R.id.button_save_control_patient);
        save_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView patientWeightText = (TextView) findViewById(R.id.control_weight);
                String mPatientWeight = patientWeightText.getText().toString();

                TextView patientHeightText = (TextView) findViewById(R.id.control_height);
                String mPatientHeight = patientHeightText.getText().toString();

                TextView patientWaistText = (TextView) findViewById(R.id.control_waist);
                String mPatientWaist = patientWaistText.getText().toString();

                TextView patientHeartRateText = (TextView) findViewById(R.id.control_heart_rate);
                String mPatientHeartRate = patientHeartRateText.getText().toString();

                TextView patientBloodPressSisText = (TextView) findViewById(R.id.control_blood_pressure_sis);
                String mPatientBloodPressSis = patientBloodPressSisText.getText().toString();

                TextView patientBloodPressDiaText = (TextView) findViewById(R.id.control_blood_pressure_dia);
                String mPatientBloodPressDia = patientBloodPressDiaText.getText().toString();

                if (validate(uniquePatientId, mPatientWeight, mPatientHeight, mPatientWaist,
                        mPatientHeartRate, mPatientBloodPressSis, mPatientBloodPressDia)) {
                    ControlDBHandlerUtils controlDBObj = new ControlDBHandlerUtils(getApplicationContext());
                    controlDBObj.openDB();

                    controlDBObj.insertData(uniquePatientId, mPatientWeight, mPatientHeight, mPatientWaist,
                            mPatientHeartRate, mPatientBloodPressSis, mPatientBloodPressDia);

                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_control), //Title
                            getResources().getString(R.string.control_data_stored), //Body message
                            true //To close current Activity when confirm
                    );

                    controlDBObj.closeDB();

                } else {
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_control), //Title
                            getResources().getString(R.string.validation), //Body message
                            false //To close current Activity when confirm
                    );
                }
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
                patientAgeText.setText(PatientUtils.getAge(mPatientBirthday)+
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

    // This method validate empty and integer values
    public boolean validate(long uniquePatientId, String patientWeight, String patientHeight,
                            String patientWaist, String patientHeartRate, String patientBloodPressSis,
                            String patientBloodPressDia) {
        boolean flag = false;

        try {
            if (uniquePatientId != 0 && !patientWeight.isEmpty() && !patientHeight.isEmpty() &&
                    !patientWaist.isEmpty() && !patientHeartRate.isEmpty() &&
                    !patientBloodPressSis.isEmpty() && !patientBloodPressDia.isEmpty()) {
                flag = true;
            }
        } catch (NumberFormatException e) {
            flag = false;
        }

        return flag;
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
