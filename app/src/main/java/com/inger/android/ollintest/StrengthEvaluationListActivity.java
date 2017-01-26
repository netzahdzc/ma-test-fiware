package com.inger.android.ollintest;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.inger.android.ollintest.util.DialogMediaUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.Utilities;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class StrengthEvaluationListActivity extends AppCompatActivity {

    private long uniquePatientId;
    private long uniqueTestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.strength_test_evaluation_list);

        final DialogMediaUtils dialog = new DialogMediaUtils(this);

        uniqueTestId = getIntent().getLongExtra("uniqueTestId", 0);

        loadActivityData();

        final TextView button_continue_evaluation = (TextView) findViewById(R.id.button_continue_evaluation);
        button_continue_evaluation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox q1 = (CheckBox) findViewById(R.id.checkbox_one);
                CheckBox q2 = (CheckBox) findViewById(R.id.checkbox_two);
                CheckBox q3 = (CheckBox) findViewById(R.id.checkbox_three);

                boolean q1Checkbox = q1.isChecked();
                boolean q2Checkbox = q2.isChecked();
                boolean q3Checkbox = q3.isChecked();

                dialog.showDialog(uniqueTestId, q1Checkbox, q2Checkbox, q3Checkbox, false,
                        false, false, false, false, false, false);
            }
        });

        final TextView button_repeat_test = (TextView) findViewById(R.id.button_repeat_test);
        button_repeat_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog diaBox = AskOption();
                diaBox.show();
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

    private AlertDialog AskOption() {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.confirmation_title))
                .setMessage(getResources().getString(R.string.confirmation_dialog))
                .setIcon(R.mipmap.ic_warning)

                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        repeatTest();

                        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(getApplicationContext());
                        testDBObj.openDB();
                        Cursor cursorTest = testDBObj.readData(uniqueTestId);

                        Intent countDownScreen = new Intent(StrengthEvaluationListActivity.this, CountDownActivity.class);
                        countDownScreen.putExtra("testType", testDBObj.getTestType(cursorTest));
                        countDownScreen.putExtra("balanceTestOption", testDBObj.getBalanceTestOption(cursorTest));
                        countDownScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(countDownScreen, 1);

                        testDBObj.closeDB();
                    }

                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    @Override
    public void onBackPressed() {
    }

}