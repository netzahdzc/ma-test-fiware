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
package com.example.android.ollintest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ollintest.util.DialogMessageUtils;
import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.UserDBHandlerUtils;

import java.io.IOException;
import java.io.InputStream;

public class IncreaseActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;

    private ImageView patientPhoto;
    private DialogMessageUtils mMessage;

    private byte[] inputPatientPhotoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_patient);

        mMessage = new DialogMessageUtils(this);

        final TextView button = (TextView) findViewById(R.id.button_add_patient);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView patientName = (TextView) findViewById(R.id.patient_name);
                String mPatientName = patientName.getText().toString();

                TextView patientSurname = (TextView) findViewById(R.id.patient_surname);
                String mPatientSurname = patientSurname.getText().toString();

                TextView patientGender = (TextView) findViewById(R.id.patient_gender);
                String mPatientGender = patientGender.getText().toString();

                TextView patientBirthday = (TextView) findViewById(R.id.patient_birthday);
                String mPatientBirthday = patientBirthday.getText().toString();

                if (validate(mPatientName, mPatientSurname, mPatientGender, mPatientBirthday, getImage())) {
                    PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
                    patientDBObj.openDB();
                    patientDBObj.insertData(mPatientName, mPatientSurname, mPatientGender, mPatientBirthday, getImage());

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
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), SELECT_PICTURE);
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    // To save image in a temporal variable
    Boolean setImage(Uri selectedImageUri) {
        try {
            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
            inputPatientPhotoData = Utilities.getBytes(iStream);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    // To get image loaded when select
    byte[] getImage() {
        return inputPatientPhotoData;
    }

    // This method validate empty and integer values
    public boolean validate(String patientName, String patientSurname, String patientGender,
                            String patientBirthday, byte patientPhoto[]) {
        boolean flag = false;

        try {
            if (patientName != "" && patientSurname != "" && patientGender != "" &&
                    patientBirthday != "" && patientPhoto != null) {
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

        TextView patientGender = (TextView) findViewById(R.id.patient_gender);
        patientGender.setText("");

        TextView patientBirthday = (TextView) findViewById(R.id.patient_birthday);
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
                        patientPhoto.setImageURI(selectedImageUri);
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
