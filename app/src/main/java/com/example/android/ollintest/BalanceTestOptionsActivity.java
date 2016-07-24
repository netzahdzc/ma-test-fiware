package com.example.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.PatientUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.TestDBHandlerUtils;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class BalanceTestOptionsActivity extends AppCompatActivity {

    final int BALANCE_TEST = 3;

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    private long uniquePatientId;
    private byte[] inputPatientPhotoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_test_options);

        loadActivityData();

        final TextView button_feet_together_option = (TextView) findViewById(R.id.button_feet_together_option);

        button_feet_together_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent footTogetherScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                footTogetherScreen.putExtra("testType", BALANCE_TEST);
                footTogetherScreen.putExtra("balanceTestOption", FEET_TOGETHER_TEST_OPTION);
                startActivity(footTogetherScreen);
            }
        });

        final TextView button_semi_tandem_option = (TextView) findViewById(R.id.button_semi_tandem_option);

        button_semi_tandem_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent semiTandemScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                semiTandemScreen.putExtra("testType", BALANCE_TEST);
                semiTandemScreen.putExtra("balanceTestOption", SEMI_TANDEM_TEST_OPTION);
                startActivity(semiTandemScreen);
            }
        });

        final TextView button_tandem_option = (TextView) findViewById(R.id.button_tandem_option);

        button_tandem_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tandemScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                tandemScreen.putExtra("testType", BALANCE_TEST);
                tandemScreen.putExtra("balanceTestOption", TANDEM_TEST_OPTION);
                startActivity(tandemScreen);
            }
        });

        final TextView button_one_leg_option = (TextView) findViewById(R.id.button_one_leg_option);

        button_one_leg_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent oneLegScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                oneLegScreen.putExtra("testType", BALANCE_TEST);
                oneLegScreen.putExtra("balanceTestOption", ONE_LEG_TEST_OPTION);
                startActivity(oneLegScreen);
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
                setOriginalImage(mPatientPhoto);

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

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
    }

    // To get image loaded when select
    void setOriginalImage(byte[] imageBytes) {
        inputPatientPhotoData = imageBytes;
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

}
