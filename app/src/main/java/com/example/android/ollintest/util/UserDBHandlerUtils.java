package com.example.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.ollintest.DatabaseContract;
import com.example.android.ollintest.DatabaseHelper;
import com.example.android.ollintest.R;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class UserDBHandlerUtils {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mUserDBHandlerUtils;

    public UserDBHandlerUtils(Context context) {
        mUserDBHandlerUtils = context;
        mDbHelper = new DatabaseHelper(mUserDBHandlerUtils);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method reads info from database
    public Cursor readAllData() {

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

        Cursor c = db.query(
                DatabaseContract.User.TABLE_NAME, // The table to query
                projection,                       // The columns to return
                null,                             // The columns for the WHERE clause
                null,                             // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                null                              // The sort order
        );

        return c;
    }

    // This method reads info from database
    public Cursor readData(long uniqueUserId) {

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
        String sortOrder = DatabaseContract.User._ID + " DESC";

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

    // This method reads info from database
    public Cursor readData(String userEmail) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.User._ID,
                DatabaseContract.User.COLUMN_NAME_COL4
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.User._ID + " DESC";

        // Define 'where' part of query.
        String selection = DatabaseContract.User.COLUMN_NAME_COL4 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(userEmail)};

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

    // This method allows to store info into database
    public long insertData(String userName, String userSurname, int userGender,
                           String userEmail, String userPassword) {
        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.User.COLUMN_NAME_COL1, userName);
        values.put(DatabaseContract.User.COLUMN_NAME_COL2, userSurname);
        values.put(DatabaseContract.User.COLUMN_NAME_COL3, userGender);
        values.put(DatabaseContract.User.COLUMN_NAME_COL4, userEmail);
        values.put(DatabaseContract.User.COLUMN_NAME_COL5, userPassword);
        values.put(DatabaseContract.User.COLUMN_NAME_COL6, "");
        values.put(DatabaseContract.User.COLUMN_NAME_COL7, 2);
        values.put(DatabaseContract.User.COLUMN_NAME_COL8, 2);
        values.put(DatabaseContract.User.COLUMN_NAME_COL9, dateObj.getCurrentDate());
        values.put(DatabaseContract.User.COLUMN_NAME_COL10, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.User.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method updates info from database
    public int updateData(long uniqueUserId, String userName, String userSurname, int userGender,
                          String userPassword) {

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.User.COLUMN_NAME_COL1, userName);
        values.put(DatabaseContract.User.COLUMN_NAME_COL2, userSurname);
        values.put(DatabaseContract.User.COLUMN_NAME_COL3, userGender);

        if (!userPassword.isEmpty())
            values.put(DatabaseContract.User.COLUMN_NAME_COL5, userPassword);

        values.put(DatabaseContract.User.COLUMN_NAME_COL9, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.User._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueUserId)};

        int count = db.update(
                DatabaseContract.User.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;
    }

    // This method updates info from database
    public String updateData(String userEmail) {
        String temporalPass = "";

        DateUtil dateObj = new DateUtil();
        SecurityUtil md5Obj = new SecurityUtil();

        temporalPass = md5Obj.convert(dateObj.getCurrentDate());
        temporalPass = temporalPass.substring(temporalPass.length() - 4);

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.User.COLUMN_NAME_COL5, temporalPass);
        values.put(DatabaseContract.User.COLUMN_NAME_COL10, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.User.COLUMN_NAME_COL4 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(userEmail)};

        int count = db.update(
                DatabaseContract.User.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return temporalPass;
    }

}
