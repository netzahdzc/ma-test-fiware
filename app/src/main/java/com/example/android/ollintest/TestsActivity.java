package com.example.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.PatientUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.Utilities;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class TestsActivity extends AppCompatActivity {

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    private long uniquePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_options);

        loadActivityData();

        final TextView button_walking_test = (TextView) findViewById(R.id.button_walking_test);

        button_walking_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tutorialScreen = new Intent(getApplicationContext(), WizardTestActivity.class);
                tutorialScreen.putExtra("testType", WALKING_TEST);
                startActivity(tutorialScreen);
            }
        });

        final TextView button_strength_test = (TextView) findViewById(R.id.button_strength_test);

        button_strength_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tutorialScreen = new Intent(getApplicationContext(), WizardTestActivity.class);
                tutorialScreen.putExtra("testType", STRENGTH_TEST);
                startActivity(tutorialScreen);
            }
        });

        final TextView button_balance_test = (TextView) findViewById(R.id.button_balance_test);

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
