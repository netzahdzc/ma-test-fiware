package com.example.android.ollintest;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ollintest.util.DateUtil;
import com.example.android.ollintest.util.DialogMessageUtils;
import com.example.android.ollintest.util.PatientDBHandlerUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.TestDBHandlerUtils;
import com.example.android.ollintest.util.UserDBHandlerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class EditAccountActivity extends AppCompatActivity {

    private DialogMessageUtils mMessage;
    private Spinner mSpinnerGender;

    private long mUniqueUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_account);

        mMessage = new DialogMessageUtils(this);
        loadActivityData();

        final TextView edit_button = (TextView) findViewById(R.id.button_update_profile);
        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView userName = (TextView) findViewById(R.id.edit_user_name);
                String mUserName = userName.getText().toString();

                TextView userSurname = (TextView) findViewById(R.id.edit_user_surname);
                String mUserSurname = userSurname.getText().toString();

                Spinner userGender = (Spinner) findViewById(R.id.edit_user_gender);
                int mUserGender = userGender.getSelectedItemPosition();

//                TextView userEmail = (TextView) findViewById(R.id.edit_user_email);
//                String mUserEmail = userEmail.getText().toString();

                TextView userPassword = (TextView) findViewById(R.id.edit_user_password);
                String mUserPassword = userPassword.getText().toString();

                TextView userPasswordRep = (TextView) findViewById(R.id.edit_user_password_confirm);
                String mUserPasswordRep = userPasswordRep.getText().toString();

                if (validatePass(mUserPassword, mUserPasswordRep)) {
                    if (validate(mUserName, mUserSurname, mUserGender)) {
                        UserDBHandlerUtils userDBObj = new UserDBHandlerUtils(getApplicationContext());
                        userDBObj.openDB();
                        userDBObj.updateData(mUniqueUserId, mUserName, mUserSurname, mUserGender, mUserPassword);
                        userDBObj.closeDB();
                        mMessage.dialogWarningMessage(
                                getResources().getString(R.string.header_sign_up), //Title
                                getResources().getString(R.string.user_updated_successfuly), //Body message
                                true //To close current Activity when confirm
                        );
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
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
    }

    public void createAndFillSpinner() {
        // Set gender dropdown
        mSpinnerGender = (Spinner) findViewById(R.id.edit_user_gender);

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

    // This method validate empty and integer values
    public boolean validate(String userName, String userSurname, int userGender) {
        boolean flag = false;

        try {
            if (!userName.isEmpty() && !userSurname.isEmpty() && userGender != 0) {
                flag = true;
            }
        } catch (NumberFormatException e) {
            flag = false;
        }

        return flag;
    }

    // This method validate both password confirmation
    public boolean validatePass(String password, String passwordRep) {
        return (password.equals(passwordRep));
    }

    // This method load data to be displayed on screen
    public void loadData(Cursor cursor) {
        try {
            // Reading all data and setting it up to be displayed
            if (cursor.moveToFirst()) {
                String mUserName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL1)
                );

                String mUserSurname = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL2)
                );

                int mUserGender = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL3)
                );

//            String mUserEmail = cursor.getString(
//                    cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL4)
//            );

                TextView userNameText = (TextView) findViewById(R.id.edit_user_name);
                userNameText.setText(mUserName);

                TextView userSurnameText = (TextView) findViewById(R.id.edit_user_surname);
                userSurnameText.setText(mUserSurname);

                Spinner userGender = (Spinner) findViewById(R.id.edit_user_gender);
                userGender.setSelection(mUserGender);

//            TextView userEmailText = (TextView) findViewById(R.id.edit_user_email);
//            userEmailText.setText(mUserEmail);

            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
//                cursor.close();
            }
        }
    }

    public void loadActivityData(){
        UserDBHandlerUtils userDBObj = new UserDBHandlerUtils(getApplicationContext());
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        userDBObj.openDB();
        sessionObj.openDB();

        mUniqueUserId = sessionObj.getUserSession();
        Cursor mCursorUser = userDBObj.readData(mUniqueUserId);

        createAndFillSpinner();
        loadData(mCursorUser);

        userDBObj.closeDB();
        sessionObj.closeDB();
    }

}