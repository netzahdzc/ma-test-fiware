package com.inger.android.ollintest;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inger.android.ollintest.util.AccDBHandlerUtils;
import com.inger.android.ollintest.util.DialogMediaUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.TestDBHandlerUtils;
import com.inger.android.ollintest.util.Utilities;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class WalkingEvaluationListActivity extends AppCompatActivity {

    private long uniquePatientId;
    private long uniqueTestId;

    LinearLayout loading_block = null;
    TextView button_repeat_test = null;
    TextView button_continue_test = null;
    ProgressBar loading_bar = null;

    // TODO Time should be removed, instead it could be better to monitor acc database to ensure there is no more data coming, thus, ensure we can continue with another test sample
    int delay = 60000; //60 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walking_test_evaluation_list);

        final DialogMediaUtils dialog = new DialogMediaUtils(this);

        uniqueTestId = getIntent().getLongExtra("uniqueTestId", 0);

        loadActivityData();

        final TextView button_continue_evaluation = (TextView) findViewById(R.id.button_continue_evaluation);
        button_continue_evaluation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox q1 = (CheckBox) findViewById(R.id.checkbox_one);
                CheckBox q2 = (CheckBox) findViewById(R.id.checkbox_two);
                CheckBox q3 = (CheckBox) findViewById(R.id.checkbox_three);
                CheckBox q4 = (CheckBox) findViewById(R.id.checkbox_four);
                CheckBox q5 = (CheckBox) findViewById(R.id.checkbox_five);
                CheckBox q6 = (CheckBox) findViewById(R.id.checkbox_six);
                CheckBox q7 = (CheckBox) findViewById(R.id.checkbox_seven);
                CheckBox q8 = (CheckBox) findViewById(R.id.checkbox_eight);
                CheckBox q9 = (CheckBox) findViewById(R.id.checkbox_nine);

                boolean q1Checkbox = q1.isChecked();
                boolean q2Checkbox = q2.isChecked();
                boolean q3Checkbox = q3.isChecked();
                boolean q4Checkbox = q4.isChecked();
                boolean q5Checkbox = q5.isChecked();
                boolean q6Checkbox = q6.isChecked();
                boolean q7Checkbox = q7.isChecked();
                boolean q8Checkbox = q8.isChecked();
                boolean q9Checkbox = q9.isChecked();

                dialog.showDialog(uniqueTestId, q1Checkbox, q2Checkbox, q3Checkbox, q4Checkbox,
                        q5Checkbox, q6Checkbox, q7Checkbox, q8Checkbox, q9Checkbox, false);

            }
        });

        button_repeat_test = (TextView) findViewById(R.id.button_repeat_test);
        button_continue_test = (TextView) findViewById(R.id.button_continue_evaluation);
        loading_bar = (ProgressBar) findViewById(R.id.loading);
        loading_block = (LinearLayout) findViewById(R.id.loadingLinearLayout);

        button_repeat_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog diaBox = AskOption();
                diaBox.show();
            }
        });

        handleUploadBar();
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

                        Intent countDownScreen = new Intent(WalkingEvaluationListActivity.this, CountDownActivity.class);
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

    // Disable buttons until there is no more data recording on the database
    public void handleUploadBar() {
        loading_block.setVisibility(View.VISIBLE);

        // Disabling buttons
        loading_bar.setMax(delay);
        button_repeat_test.setEnabled(false);
        button_repeat_test.setBackgroundColor(getResources().getColor(R.color.hint));

        button_continue_test.setEnabled(false);
        button_continue_test.setBackgroundColor(getResources().getColor(R.color.hint));

        new CountDownTimer(delay, 100) {
            int secondsLeft = 1;

            public void onTick(long millisUntilFinished) {
                if (Math.round((float) millisUntilFinished) != secondsLeft) {
                    secondsLeft = Math.round((float) millisUntilFinished);
                    loading_bar.setProgress(delay-secondsLeft);
                }
            }

            @Override
            public void onFinish() {
                button_repeat_test.setEnabled(true);
                button_repeat_test.setBackgroundColor(getResources().getColor(R.color.ok_button));

                button_continue_test.setEnabled(true);
                button_continue_test.setBackgroundColor(getResources().getColor(R.color.ok_button));

                loading_block.setVisibility(View.GONE);

                playAlarm();
            }
        }.start();
    }

    public void playAlarm() {
        MediaPlayer mp = MediaPlayer.create(WalkingEvaluationListActivity.this, R.raw.alert);
        mp.start();
    }
}
