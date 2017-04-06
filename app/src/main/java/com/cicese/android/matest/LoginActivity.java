package com.cicese.android.matest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cicese.android.matest.util.DialogMessageUtils;
import com.cicese.android.matest.util.SessionUtil;
import com.cicese.android.matest.util.UserDBHandlerUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String APP_NAME = "ma_test";
    private final String APP_ACC_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/acc");
    private DialogMessageUtils mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        // This function helps to ensure there is a container to store databases
        checkAppDirectory();

        //Before start anything, we check if there is any session activated to start directly on the MainActivity
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        sessionObj.openDB();

        if (sessionObj.getUserSession() != 0) {
            sessionObj.closeDB();
            Intent loginScreen = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(loginScreen);
            finish();
        }

        setContentView(R.layout.login);

        boolean finish = getIntent().getBooleanExtra("finish", false);
        if (finish) {
            finish();
        } else {

            mMessage = new DialogMessageUtils(this);

            final TextView login = (TextView) findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    UserDBHandlerUtils userDBObj = new UserDBHandlerUtils(getApplicationContext());
                    userDBObj.openDB();

                    EditText userEmail = (EditText) findViewById(R.id.user_email);
                    String mUserEmail = userEmail.getText().toString();

                    EditText userPassword = (EditText) findViewById(R.id.user_password);
                    String mUserPassword = userPassword.getText().toString();

                    Cursor cursorUser = userDBObj.readData(mUserEmail);

                    if (authentication(cursorUser, mUserEmail, mUserPassword)) {
                        Intent loginScreen = new Intent(getApplicationContext(), MainActivity.class);

                        // This session lines allow us to keep the user session activated until he logout voluntary
                        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
                        sessionObj.openDB();

                        sessionObj.setUserSession(getUniqueUserId(cursorUser, mUserEmail, mUserPassword));
                        sessionObj.closeDB();
                        startActivity(loginScreen);
                    } else {
                        mMessage.dialogWarningMessage(
                                getResources().getString(R.string.authentication), //Title
                                getResources().getString(R.string.authentication_message), //Body message
                                false //To close current Activity when confirm
                        );
                    }

                    cleanLoginForm();
                    userDBObj.closeDB();
                }
            });

            final TextView recover_password = (TextView) findViewById(R.id.recover_password);
            recover_password.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent recoverPassScreen = new Intent(getApplicationContext(), RecoverPasswordActivity.class);
                    startActivity(recoverPassScreen);
                }
            });

            final TextView register = (TextView) findViewById(R.id.register);
            register.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent registerScreen = new Intent(getApplicationContext(), SignUpActivity.class);
                    startActivity(registerScreen);
                }
            });
        }
    }

    public void checkAppDirectory(){
        File myFile = new File(APP_ACC_DIRECTORY_PATH);

        if (!myFile.exists()) {
            myFile.mkdirs();
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cleanLoginForm() {
        EditText userEmail = (EditText) findViewById(R.id.user_email);
        userEmail.setText("");

        EditText userPassword = (EditText) findViewById(R.id.user_password);
        userPassword.setText("");
    }

    public boolean authentication(Cursor cursor, String email, String password) {
        long mUniqueUserId = 0;
        boolean mFlag = false;

        try {
            if (cursor.moveToFirst()) {
                do {
                    String mUserEmail = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL4)
                    );

                    String mUserPassword = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL5)
                    );

                    if (email.equals(mUserEmail) && password.equals(mUserPassword)) {
                        mUniqueUserId = cursor.getLong(
                                cursor.getColumnIndexOrThrow(DatabaseContract.User._ID)
                        );
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

        if (mUniqueUserId != 0) mFlag = true;

        return mFlag;
    }

    public long getUniqueUserId(Cursor cursor, String email, String password) {
        long mUniqueUserId = 0;

        try {
            if (cursor.moveToFirst()) {
                do {
                    String mUserEmail = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL4)
                    );

                    String mUserPassword = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL5)
                    );

                    if (email.equals(mUserEmail) && password.equals(mUserPassword)) {
                        mUniqueUserId = cursor.getLong(
                                cursor.getColumnIndexOrThrow(DatabaseContract.User._ID)
                        );
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

        return mUniqueUserId;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
