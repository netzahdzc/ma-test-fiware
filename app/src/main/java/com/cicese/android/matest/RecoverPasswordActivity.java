package com.cicese.android.matest;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cicese.android.matest.util.DialogMessageUtils;
import com.cicese.android.matest.util.UserDBHandlerUtils;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class RecoverPasswordActivity extends AppCompatActivity {

    private DialogMessageUtils mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.recover_password);

        mMessage = new DialogMessageUtils(this);

        TextView button_request_reset_password = (TextView) findViewById(R.id.button_request_reset_password);

        button_request_reset_password.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText reset_user_email = (EditText) findViewById(R.id.reset_user_email);
                String mUserEmail = reset_user_email.getText().toString();

                UserDBHandlerUtils userDBObj = new UserDBHandlerUtils(getApplicationContext());
                userDBObj.openDB();
                Cursor mCursorUser = userDBObj.readData(mUserEmail);

                if (mCursorUser.getCount() > 0) {
                    String tempCode = userDBObj.updateData(reset_user_email.getText().toString());

                    userDBObj.closeDB();
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_recover_password), //Title
                            getResources().getString(R.string.recover_password_successfuly) + "\n\nCode: " + tempCode, //Body message
                            true //To close current Activity when confirm
                    );
                } else {
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.header_recover_password), //Title
                            getResources().getString(R.string.email_no_exist), //Body message
                            false //To close current Activity when confirm
                    );
                }
            }
        });

        final TextView button_cancel_request = (TextView) findViewById(R.id.button_cancel_request);
        button_cancel_request.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
