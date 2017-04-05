package com.cicese.android.matest;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cicese.android.matest.util.DateUtil;
import com.cicese.android.matest.util.DialogMessageUtils;
import com.cicese.android.matest.util.SessionUtil;

import java.util.regex.Pattern;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class ActivationAccountActivity extends AppCompatActivity {

    private DialogMessageUtils mMessage;
    private DatabaseHelper mDbHelper;
    private SessionUtil sessionObj;

    private Cursor mCursorUser;

    private long mUniqueUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activation_code);

        mMessage = new DialogMessageUtils(this);
        mDbHelper = new DatabaseHelper(getApplicationContext());
        sessionObj = new SessionUtil(getApplicationContext());
        sessionObj.openDB();

        mUniqueUserId = sessionObj.getUserSession();
        mCursorUser = readData(mUniqueUserId);
        sessionObj.closeDB();

        LinearLayout account_already_activated_layout = (LinearLayout) findViewById(R.id.account_already_activated_layout);
        LinearLayout activation_code_layout = (LinearLayout) findViewById(R.id.activation_code_layout);

        if(checkIfAccountActivated(mCursorUser)) {
            account_already_activated_layout.setVisibility(View.VISIBLE);
            activation_code_layout.setVisibility(View.GONE);
        }else{
            account_already_activated_layout.setVisibility(View.GONE);
            activation_code_layout.setVisibility(View.VISIBLE);
        }

        final TextView edit_button = (TextView) findViewById(R.id.button_activation_code);
        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView userActivationCodeText = (TextView) findViewById(R.id.user_activation_code);
                String mUserActivationCode = userActivationCodeText.getText().toString();

                if (validateCode(mUserActivationCode)) {
                    updateData(mUniqueUserId, mUserActivationCode);

                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.activation_code), //Title
                            getResources().getString(R.string.activation_code_updated), //Body message
                            true //To close current Activity when confirm
                    );
                } else {
                    mMessage.dialogWarningMessage(
                            getResources().getString(R.string.activation_code), //Title
                            getResources().getString(R.string.wrong_activation_code), //Body message
                            false //To close current Activity when confirm
                    );
                }
            }
        });

        // To avoid starting keyword automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    // This method validate the code :P
    public boolean validateCode(String userActivationCode) {
        boolean flag = false;

        try {
            if (!userActivationCode.isEmpty() && confirmCodeOnline(userActivationCode)) {
                flag = true;
            }
        } catch (NumberFormatException e) {
            flag = false;
        }

        return flag;
    }

    // This method updates info from database
    public int updateData(long uniqueUserId, String activationCode) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.User.COLUMN_NAME_COL6, activationCode);
        values.put(DatabaseContract.User.COLUMN_NAME_COL7, 1);
        values.put(DatabaseContract.User.COLUMN_NAME_COL10, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.User._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueUserId)};

        int count = db.update(
                DatabaseContract.User.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();

        return count;
    }

    // This method reads info from database
    public Cursor readData(long uniqueUserId) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.User._ID,
                DatabaseContract.User.COLUMN_NAME_COL1,
                DatabaseContract.User.COLUMN_NAME_COL2,
                DatabaseContract.User.COLUMN_NAME_COL3,
                DatabaseContract.User.COLUMN_NAME_COL4,
                DatabaseContract.User.COLUMN_NAME_COL5,
                DatabaseContract.User.COLUMN_NAME_COL6,
                DatabaseContract.User.COLUMN_NAME_COL7,
                DatabaseContract.User.COLUMN_NAME_COL8,
                DatabaseContract.User.COLUMN_NAME_COL9,
                DatabaseContract.User.COLUMN_NAME_COL10
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.User._ID + " DESC";

        // Define 'where' part of query.
        String selection = DatabaseContract.User._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueUserId)};

        Cursor c = db.query(
                DatabaseContract.User.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        return c;
    }

    // This method load data to be displayed on screen
    public boolean checkIfAccountActivated(Cursor cursor) {
        boolean mActivationFlag = false;

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {

            int mActivationStatus = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseContract.User.COLUMN_NAME_COL7)
            );

            if (mActivationStatus == 1) mActivationFlag = true;

        }

        cursor.close();

        return mActivationFlag;
    }

    // TODO A smarter mechanism could better. Consider an online confirmation.
    public boolean confirmCodeOnline(String activationCode) {
        return Pattern.matches("^INGER[0-9]{3}$", activationCode);
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
