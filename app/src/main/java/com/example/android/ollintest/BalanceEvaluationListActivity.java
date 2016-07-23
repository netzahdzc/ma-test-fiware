package com.example.android.ollintest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.util.ControlDBHandlerUtils;
import com.example.android.ollintest.util.DialogMediaUtils;
import com.example.android.ollintest.util.DialogMessageUtils;
import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.PatientUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.TestDBHandlerUtils;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class BalanceEvaluationListActivity extends AppCompatActivity {

    private long uniquePatientId;
    private long uniqueTestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_test_evaluation_list);

        final DialogMediaUtils dialog = new DialogMediaUtils(this);

        uniqueTestId = getIntent().getLongExtra("uniqueTestId", 0);

        loadActivityData();

        final TextView button_continue_evaluation = (TextView) findViewById(R.id.button_continue_evaluation);
        button_continue_evaluation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.showDialog(uniqueTestId);
            }
        });

        final TextView button_repeat_test = (TextView) findViewById(R.id.button_repeat_test);
        button_repeat_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                repeatTest();

                TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
                testDBObj.openDB();
                Cursor cursorTest = testDBObj.readData(uniqueTestId);

                Intent countDownScreen = new Intent(BalanceEvaluationListActivity.this, CountDownActivity.class);
                countDownScreen.putExtra("testType", testDBObj.getTestType(cursorTest));
                countDownScreen.putExtra("balanceTestOption", testDBObj.getBalanceTestOption(cursorTest));
                countDownScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(countDownScreen, 1);

                testDBObj.closeDB();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
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

    public void repeatTest() {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, 0, 0, "", "", "", "", "", "", "",
                "", "", "", "", 0, "", "testCorrupted");

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
    }

}
