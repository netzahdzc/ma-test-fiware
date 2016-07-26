package com.inger.android.ollintest;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.inger.android.ollintest.util.DialogMessageUtils;
import com.inger.android.ollintest.util.UserDBHandlerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class SignUpActivity extends AppCompatActivity {

    private DialogMessageUtils mMessage;
    private Spinner mSpinnerGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.signup);

        mMessage = new DialogMessageUtils(this);

        final TextView button_cancel_sign_up = (TextView) findViewById(R.id.button_cancel_signup);
        button_cancel_sign_up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        final TextView button_register_user = (TextView) findViewById(R.id.button_register_user);
        button_register_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView userName = (TextView) findViewById(R.id.user_name);
                String mUserName = userName.getText().toString();

                TextView userSurname = (TextView) findViewById(R.id.user_surname);
                String mUserSurname = userSurname.getText().toString();

                Spinner userGender = (Spinner) findViewById(R.id.user_gender);
                int mUserGender = userGender.getSelectedItemPosition();

                TextView userEmail = (TextView) findViewById(R.id.user_email);
                String mUserEmail = userEmail.getText().toString();

                TextView userPassword = (TextView) findViewById(R.id.user_password);
                String mUserPassword = userPassword.getText().toString();

                TextView userPasswordRep = (TextView) findViewById(R.id.user_password_confirm);
                String mUserPasswordRep = userPasswordRep.getText().toString();

                UserDBHandlerUtils userDBObj = new UserDBHandlerUtils(getApplicationContext());
                userDBObj.openDB();
                Cursor mCursorUser = userDBObj.readAllData();

                if (validatePass(mUserPassword, mUserPasswordRep)) {
                    if (validate(mUserName, mUserSurname, mUserGender, mUserEmail, mUserPassword)) {
                        if (!confirmRedundancy(mCursorUser, mUserEmail)) {
                            userDBObj.insertData(mUserName, mUserSurname, mUserGender, mUserEmail, mUserPassword);

                            mMessage.dialogWarningMessage(
                                    getResources().getString(R.string.header_sign_up), //Title
                                    getResources().getString(R.string.user_created_successfuly), //Body message
                                    true //To close current Activity when confirm
                            );
                        } else {
                            mMessage.dialogWarningMessage(
                                    getResources().getString(R.string.header_sign_up), //Title
                                    getResources().getString(R.string.user_already_exist), //Body message
                                    false //To close current Activity when confirm
                            );
                        }
                    } else {
                        mMessage.dialogWarningMessage(
                                getResources().getString(R.string.header_sign_up),  //Title
                                getResources().getString(R.string.validation), //Body message
                                false //To close current Activity when confirm
                        );
                    }
                } else {
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_sign_up), //Title
                            getResources().getString(R.string.authentication_password_no), //Body message
                            false //To close current Activity when confirm
                    );
                }
                userDBObj.closeDB();
            }
        });

        createAndFillSpinner();

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void createAndFillSpinner() {
        // Set gender dropdown
        mSpinnerGender = (Spinner) findViewById(R.id.user_gender);

        List<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.spinner_select_option));
        list.add(getResources().getString(R.string.user_gender_male));
        list.add(getResources().getString(R.string.user_gender_female));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        mSpinnerGender.setAdapter(dataAdapter);
    }

    // This method validate both password confirmation
    public boolean validatePass(String password, String passwordRep) {
        return (password.equals(passwordRep));
    }

    // This method validate empty and integer values
    public boolean validate(String userName, String userSurname, int userGender,
                            String userEmail, String userPassword) {

        boolean flag = false;

        try {
            if (!userName.isEmpty() && !userSurname.isEmpty() && userGender != 0 &&
                    !userEmail.isEmpty() && !userPassword.isEmpty()) {
                flag = true;
            }
        } catch (NumberFormatException e) {
            flag = false;
        }

        return flag;
    }

    //This method helps to reduce redundancy on users at local storage
    public boolean confirmRedundancy(Cursor cursor, String email) {
        boolean mFlag = false;
        try {
            if (cursor.moveToFirst()) {
                do {
                    String mUserEmail = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL4)
                    );

                    if (email.equals(mUserEmail)) mFlag = true;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mFlag;
    }

}
