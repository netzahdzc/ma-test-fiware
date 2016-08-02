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
public class BalanceTestOptionsActivity extends AppCompatActivity {

    final int BALANCE_TEST = 3;

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    private TextView button_feet_together_option;
    private TextView button_semi_tandem_option;
    private TextView button_tandem_option;
    private TextView button_one_leg_option;

    private long uniquePatientId;
    private byte[] inputPatientPhotoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_test_options);

        button_feet_together_option = (TextView) findViewById(R.id.button_feet_together_option);
        button_semi_tandem_option = (TextView) findViewById(R.id.button_semi_tandem_option);
        button_tandem_option = (TextView) findViewById(R.id.button_tandem_option);
        button_one_leg_option = (TextView) findViewById(R.id.button_one_leg_option);

        loadActivityData();
        disableTestButtons();

        button_feet_together_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent footTogetherScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                footTogetherScreen.putExtra("testType", BALANCE_TEST);
                footTogetherScreen.putExtra("balanceTestOption", FEET_TOGETHER_TEST_OPTION);
                startActivity(footTogetherScreen);
            }
        });

        button_semi_tandem_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent semiTandemScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                semiTandemScreen.putExtra("testType", BALANCE_TEST);
                semiTandemScreen.putExtra("balanceTestOption", SEMI_TANDEM_TEST_OPTION);
                startActivity(semiTandemScreen);
            }
        });

        button_tandem_option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tandemScreen = new Intent(getApplicationContext(), WizardBalanceTestActivity.class);
                tandemScreen.putExtra("testType", BALANCE_TEST);
                tandemScreen.putExtra("balanceTestOption", TANDEM_TEST_OPTION);
                startActivity(tandemScreen);
            }
        });

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
                    setOriginalImage(mPatientPhoto);
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

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
    }

    // To get image loaded when select
    void setOriginalImage(byte[] imageBytes) {
        inputPatientPhotoData = imageBytes;
    }

    public void disableTestButtons() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        button_feet_together_option.setEnabled(true);
        button_feet_together_option.setBackgroundColor(getResources().getColor(R.color.ok_button));
        button_semi_tandem_option.setEnabled(false);
        button_semi_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
        button_tandem_option.setEnabled(false);
        button_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
        button_one_leg_option.setEnabled(false);
        button_one_leg_option.setBackgroundColor(getResources().getColor(R.color.hint));

        Cursor cursor = testDBObj.getTodayTest(uniquePatientId);

        try {
            cursor.moveToFirst();

            // Reading all data and setting it up to be displayed
            if (cursor != null) {
                do {
                    int optionTestType = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL3)
                    );

                    if (optionTestType == FEET_TOGETHER_TEST_OPTION) {
                        button_feet_together_option.setEnabled(false);
                        button_feet_together_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_semi_tandem_option.setEnabled(true);
                        button_semi_tandem_option.setBackgroundColor(getResources().getColor(R.color.ok_button));
                        button_tandem_option.setEnabled(false);
                        button_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_one_leg_option.setEnabled(false);
                        button_one_leg_option.setBackgroundColor(getResources().getColor(R.color.hint));
                    }

                    if (optionTestType == SEMI_TANDEM_TEST_OPTION) {
                        button_feet_together_option.setEnabled(false);
                        button_feet_together_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_semi_tandem_option.setEnabled(false);
                        button_semi_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_tandem_option.setEnabled(true);
                        button_tandem_option.setBackgroundColor(getResources().getColor(R.color.ok_button));
                        button_one_leg_option.setEnabled(false);
                        button_one_leg_option.setBackgroundColor(getResources().getColor(R.color.hint));
                    }

                    if (optionTestType == TANDEM_TEST_OPTION) {
                        button_feet_together_option.setEnabled(false);
                        button_feet_together_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_semi_tandem_option.setEnabled(false);
                        button_semi_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_tandem_option.setEnabled(false);
                        button_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_one_leg_option.setEnabled(true);
                        button_one_leg_option.setBackgroundColor(getResources().getColor(R.color.ok_button));
                    }

                    if (optionTestType == ONE_LEG_TEST_OPTION) {
                        button_feet_together_option.setEnabled(false);
                        button_feet_together_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_semi_tandem_option.setEnabled(false);
                        button_semi_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_tandem_option.setEnabled(false);
                        button_tandem_option.setBackgroundColor(getResources().getColor(R.color.hint));
                        button_one_leg_option.setEnabled(false);
                        button_one_leg_option.setBackgroundColor(getResources().getColor(R.color.hint));
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

}
