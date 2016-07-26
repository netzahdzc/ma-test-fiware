/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inger.android.ollintest;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.inger.android.ollintest.fragment.DatePickerFragment;
import com.inger.android.ollintest.util.DialogMessageUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.SessionUtil;
import com.inger.android.ollintest.util.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    final int BIRTHDAY_FORMAT = 1;
    final int UPDATE_FORMAT = 2;

    private DialogMessageUtils mMessage;
    private ImageView patientPhoto;
    private Spinner mSpinnerGender;

    private byte[] inputPatientPhotoData;
    private long uniquePatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_patient);

        mMessage = new DialogMessageUtils(this);

        loadActivityData();

        final TextView edit_button = (TextView) findViewById(R.id.button_update_patient);
        final TextView remove_button = (TextView) findViewById(R.id.button_remove_patient);

        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText uniqueId = (EditText) findViewById(R.id.edit_unique_patient_id);
                String mUniqueId = uniqueId.getText().toString();

                EditText name = (EditText) findViewById(R.id.edit_patient_name);
                String mName = name.getText().toString();

                EditText lastName = (EditText) findViewById(R.id.edit_patient_surname);
                String mLastName = lastName.getText().toString();

                Spinner userGender = (Spinner) findViewById(R.id.edit_patient_gender);
                int mPatientSex = userGender.getSelectedItemPosition();

//                EditText patientSex = (EditText) findViewById(R.id.edit_patient_gender);
//                String mPatientSex = patientSex.getText().toString();

                TextView patientBirthDate = (TextView) findViewById(R.id.date_field);
                String mPatientBirthDate = patientBirthDate.getText().toString();

                if (validate(mName, mLastName, mPatientSex, mPatientBirthDate, getImage())) {
                    PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
                    patientDBObj.openDB();

                    patientDBObj.updateData(Integer.parseInt(mUniqueId), mName, mLastName, mPatientSex,
                            PatientUtils.convertToISO8601(mPatientBirthDate, BIRTHDAY_FORMAT), getImage());

                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_update_patient), //Title
                            getResources().getString(R.string.updated), //Body message
                            true //To close current Activity when confirm
                    );

                    patientDBObj.closeDB();
                } else {
                    // Something went wrong with the info provided.
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_update_patient), //Title
                            getResources().getString(R.string.update), //Body message
                            false //To close current Activity when confirm
                    );
                }
            }
        });


        /**
         * This section is dedicated to handle the remove request
         */
        remove_button.setOnClickListener(new View.OnClickListener() {
            EditText uniquePatientId = (EditText) findViewById(R.id.edit_unique_patient_id);
            String mUniquePatientId = uniquePatientId.getText().toString();

            public void onClick(View v) {
                confirmation(Integer.parseInt(mUniquePatientId));
            }
        });

        /**
         * This section is dedicated to handle the update picture
         */
        final TextView buttonLoadImage = (TextView) findViewById(R.id.edit_load_patient_photo);
        patientPhoto = (ImageView) findViewById(R.id.edit_patient_photo);

        buttonLoadImage.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getResources().getString(R.string.select_a_picture_header)), SELECT_PICTURE);
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    if (setImage(selectedImageUri)) {
                        Utilities obj = new Utilities();
                        try {
                            patientPhoto.setImageBitmap(Utilities.scaleImage(getApplicationContext(), selectedImageUri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void createAndFillSpinner() {
        // Set gender dropdown
        mSpinnerGender = (Spinner) findViewById(R.id.edit_patient_gender);

        List<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.spinner_select_option));
        list.add(getResources().getString(R.string.user_gender_male));
        list.add(getResources().getString(R.string.user_gender_female));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerGender.setAdapter(dataAdapter);
    }

    // To save image in a temporal variable
    Boolean setImage(Uri selectedImageUri) {
        try {
            Utilities obj = new Utilities();
            Bitmap bmp = Utilities.scaleImage(getApplicationContext(), selectedImageUri);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            inputPatientPhotoData = byteArray;
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    // To get image loaded when select
    void setOriginalImage(byte[] imageBytes) {
        inputPatientPhotoData = imageBytes;
    }

    // To get image loaded when select
    byte[] getImage() {
        return inputPatientPhotoData;
    }

    // Method to show a notification to confirm the action of delete an patient
    public void confirmation(final int mUniqueParticipantId) {
        new AlertDialog.Builder(EditActivity.this).setTitle(getResources().getString(R.string.deleting_header)).
                setMessage(getResources().getString(R.string.confirmation)).
                setNegativeButton(getResources().getString(R.string.no), null).setPositiveButton(
                getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
                        patientDBObj.openDB();
                        patientDBObj.removeData(mUniqueParticipantId);
                        patientDBObj.closeDB();

                        mMessage.dialogWarningDeleteMessage(
                                getResources().getString(R.string.header_update_patient), //Title
                                getResources().getString(R.string.removed), //Body message
                                true //To close current Activity when confirm
                        );

                    }
                }).show();
    }

    // This method load data to be fisplayed on screen
    public void loadData(Cursor cursor) {
        try {
            // Reading all data and setting it up to be displayed
            if (cursor.moveToFirst()) {
                int mUniquePatientId = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient._ID)
                );

                String mPatientName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL1)
                );

                String mPatientSurname = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL2)
                );

                int mPatientGender = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL3)
                );

//                String mPatientGender = cursor.getString(
//                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL3)
//                );

                String mPatientBirthday = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL4)
                );

                byte[] mPatientPhoto = cursor.getBlob(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL5)
                );

                ImageView patientPhoto = (ImageView) findViewById(R.id.edit_patient_photo);
                patientPhoto.setImageBitmap(Utilities.getImage(mPatientPhoto));
                setOriginalImage(mPatientPhoto);

                TextView uniquePatientIdText = (TextView) findViewById(R.id.edit_unique_patient_id);
                uniquePatientIdText.setText(mUniquePatientId + "");

                TextView patientNameText = (TextView) findViewById(R.id.edit_patient_name);
                patientNameText.setText(mPatientName);

                TextView patientSurnameNameText = (TextView) findViewById(R.id.edit_patient_surname);
                patientSurnameNameText.setText(mPatientSurname);

                Spinner patientGenderText = (Spinner) findViewById(R.id.edit_patient_gender);
                patientGenderText.setSelection(mPatientGender);

//                TextView patientGenderText = (TextView) findViewById(R.id.edit_patient_gender);
//                patientGenderText.setText(mPatientGender);

                TextView patientBirthdayText = (TextView) findViewById(R.id.date_field);
                patientBirthdayText.setText(PatientUtils.convertFromISO8601(mPatientBirthday, BIRTHDAY_FORMAT));
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

    public boolean validate(String patientName, String patientSurname, int patientGender,
                            String patientBirthday, byte patientPhoto[]) {
        boolean flag = false;

        try {
            if (patientName != "" && patientSurname != "" && patientGender != 0 &&
                    patientBirthday != "" && patientPhoto != null) {
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

        createAndFillSpinner();
        loadData(mCursorPatient);

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
