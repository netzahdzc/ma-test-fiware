package com.example.android.ollintest;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ollintest.util.DialogMessageUtils;
import com.example.android.ollintest.util.SessionUtil;
import com.example.android.ollintest.util.UserDBHandlerUtils;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class LoginActivity extends AppCompatActivity {

    private DialogMessageUtils mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

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

                    Cursor cursorUser = userDBObj.readAllData();

                    EditText userEmail = (EditText) findViewById(R.id.user_email);
                    String mUserEmail = userEmail.getText().toString();

                    EditText userPassword = (EditText) findViewById(R.id.user_password);
                    String mUserPassword = userPassword.getText().toString();

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

    public void cleanLoginForm() {
        EditText userEmail = (EditText) findViewById(R.id.user_email);
        userEmail.setText("");

        EditText userPassword = (EditText) findViewById(R.id.user_password);
        userPassword.setText("");
    }

    public boolean authentication(Cursor cursor, String email, String password) {
        int mUniqueUserId = 0;
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
                        mUniqueUserId = cursor.getInt(
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

    public int getUniqueUserId(Cursor cursor, String email, String password) {
        int mUniqueUserId = 0;

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
                        mUniqueUserId = cursor.getInt(
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