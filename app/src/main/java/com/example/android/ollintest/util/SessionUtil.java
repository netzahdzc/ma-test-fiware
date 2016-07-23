package com.example.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.ollintest.DatabaseContract;
import com.example.android.ollintest.DatabaseHelper;
import com.example.android.ollintest.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class SessionUtil {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mSessionUtil;

    private long mUniqueUserId;
    private long mUniquePatientId;

    public SessionUtil(Context context) {
        mSessionUtil = context;
        mDbHelper = new DatabaseHelper(mSessionUtil);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    /**
     * This method keeps user session to keep alive until the user voluntary logout
     */
    public void setUserSession(long uniqueUserId) {
        mUniqueUserId = uniqueUserId;
        if (updateData(mUniqueUserId, 1) <= 0) {
            insertData(mUniqueUserId, 1);
        }
    }

    /**
     * This method keeps Patient session on record to be used in further activities along user session
     */
    public void setPatientSession(long uniquePatientId) {
        mUniquePatientId = uniquePatientId;
        if (updateData(mUniquePatientId, 2) <= 0) {
            insertData(mUniquePatientId, 2);
        }
    }

    /**
     * This method recover current user session even-though the app is turned off
     */
    public long getUserSession() {
        Cursor mCursor = readData(1);
        mUniqueUserId = loadData(mCursor);
        return mUniqueUserId;
    }

    /**
     * This method recover current patient session even-though the app is turned off
     *
     * @return
     */
    public long getPatientSession() {
        Cursor mCursor = readData(2);
        mUniqueUserId = loadData(mCursor);
        return mUniqueUserId;
    }

    public void resetUserSession() {
        updateData(0, 1);
    }

    // This method updates info from database
    public int updateData(long uniqueUserId, long userType) {

//        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Session.COLUMN_NAME_COL1, uniqueUserId);
        values.put(DatabaseContract.Session.COLUMN_NAME_COL2, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.Session._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(userType)};

        int count = db.update(
                DatabaseContract.Session.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;
    }

    // This method allows to store info into database
    public long insertData(long uniqueUserId, int userType) {
        // Gets the data repository in write mode
//        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Session.COLUMN_NAME_COL1, uniqueUserId);
        values.put(DatabaseContract.Session.COLUMN_NAME_COL2, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.Session.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method load data to be displayed on screen
    public int loadData(Cursor cursor) {
        int mUniqueId = 0;

        try {
            // Reading all data and setting it up to be displayed
            if (cursor.moveToFirst()) {
                mUniqueId = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseContract.Session.COLUMN_NAME_COL1)
                );
            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
//                cursor.close();
            }
        }

        return mUniqueId;
    }

    // This method reads info from database
    public Cursor readData(int userType) {

//        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Session._ID,
                DatabaseContract.Session.COLUMN_NAME_COL1
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.User._ID + " DESC";

        // Define 'where' part of query.
        String selection = DatabaseContract.User._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(userType)};

        Cursor c = db.query(
                DatabaseContract.Session.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

//        db.close();

        return c;
    }
}
