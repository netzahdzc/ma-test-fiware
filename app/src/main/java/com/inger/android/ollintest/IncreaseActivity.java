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

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.inger.android.ollintest.fragment.DatePickerFragment;
import com.inger.android.ollintest.util.DialogMessageUtils;
import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.PatientUtils;
import com.inger.android.ollintest.util.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IncreaseActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    final int BIRTHDAY_FORMAT = 1;
    final int UPDATE_FORMAT = 2;

    private ImageView patientPhoto;
    private DialogMessageUtils mMessage;
    private Spinner mSpinnerGender;

    private byte[] inputPatientPhotoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_patient);

        mMessage = new DialogMessageUtils(this);
        createAndFillSpinner();

        final TextView button = (TextView) findViewById(R.id.button_add_patient);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView patientName = (TextView) findViewById(R.id.patient_name);
                String mPatientName = patientName.getText().toString();

                TextView patientSurname = (TextView) findViewById(R.id.patient_surname);
                String mPatientSurname = patientSurname.getText().toString();

                Spinner patientGender = (Spinner) findViewById(R.id.patient_gender);
                int mPatientGender = patientGender.getSelectedItemPosition();

//                TextView patientGender = (TextView) findViewById(R.id.patient_gender);
//                String mPatientGender = patientGender.getText().toString();

                TextView patientBirthday = (TextView) findViewById(R.id.date_field);
                String mPatientBirthday = patientBirthday.getText().toString();

                if (validate(mPatientName, mPatientSurname, mPatientGender, mPatientBirthday)) {
                    PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
                    patientDBObj.openDB();
                    patientDBObj.insertData(mPatientName, mPatientSurname, mPatientGender,
                            PatientUtils.convertToISO8601(mPatientBirthday, BIRTHDAY_FORMAT), getImage());

                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_new_patient), //Title
                            getResources().getString(R.string.patient_created_successfuly), //Body message
                            false //To close current Activity when confirm
                    );

                    cleanForm();
                    patientDBObj.closeDB();
                } else {
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_new_patient), //Title
                            getResources().getString(R.string.validation_with_photo), //Body message
                            false //To close current Activity when confirm
                    );
                }
            }
        });

        final TextView buttonLoadImage = (TextView) findViewById(R.id.button_load_patient_photo);
        patientPhoto = (ImageView) findViewById(R.id.patient_photo);

        buttonLoadImage.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getResources().getString(R.string.select_picture)), SELECT_PICTURE);
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
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

    public void createAndFillSpinner() {
        // Set gender dropdown
        mSpinnerGender = (Spinner) findViewById(R.id.patient_gender);

        List<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.spinner_select_option));
        list.add(getResources().getString(R.string.user_gender_male));
        list.add(getResources().getString(R.string.user_gender_female));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerGender.setAdapter(dataAdapter);
    }

    // To get image loaded when select
    byte[] getImage() {
        return inputPatientPhotoData;
    }

    // This method validate empty and integer values
    public boolean validate(String patientName, String patientSurname, int patientGender,
                            String patientBirthday) {
        boolean flag = false;

        try {
            if (patientName != "" && patientSurname != "" && patientGender != 0 &&
                    patientBirthday != "") {
                flag = true;
            }
        } catch (NumberFormatException e) {
            flag = false;
        }

        return flag;
    }

    // To clean field
    public void cleanForm() {
        TextView patientName = (TextView) findViewById(R.id.patient_name);
        patientName.setText("");

        TextView patientSurname = (TextView) findViewById(R.id.patient_surname);
        patientSurname.setText("");

        Spinner userGender = (Spinner) findViewById(R.id.patient_gender);
        userGender.setSelection(0);

//        TextView patientGender = (TextView) findViewById(R.id.patient_gender);
//        patientGender.setText("");

        TextView patientBirthday = (TextView) findViewById(R.id.date_field);
        patientBirthday.setText("");

        ImageView patientPhoto = (ImageView) findViewById(R.id.patient_photo);
        patientPhoto.setImageResource(R.drawable.profile);
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
