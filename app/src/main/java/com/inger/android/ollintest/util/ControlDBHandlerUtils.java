package com.inger.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inger.android.ollintest.DatabaseContract;
import com.inger.android.ollintest.DatabaseHelper;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class ControlDBHandlerUtils {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mControlDBHandlerUtils;

    public ControlDBHandlerUtils(Context context) {
        mControlDBHandlerUtils = context;
        mDbHelper = new DatabaseHelper(mControlDBHandlerUtils);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method allows to store info into database
    public long insertData(long uniquePatientId, String patientWeight, String patientHeight,
                           String patientWaist, String patientHeartRate, String patientBloodPressSis,
                           String patientBloodPressDia) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Control.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL2, patientWeight);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL3, patientHeight);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL4, patientWaist);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL5, patientHeartRate);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL6, patientBloodPressSis);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL7, patientBloodPressDia);
        values.put(DatabaseContract.Control.COLUMN_NAME_COL8, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.Control.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method reads info from database
    public Cursor readData(long uniquePatientId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Control._ID,
                DatabaseContract.Control.COLUMN_NAME_COL1,
                DatabaseContract.Control.COLUMN_NAME_COL2,
                DatabaseContract.Control.COLUMN_NAME_COL3,
                DatabaseContract.Control.COLUMN_NAME_COL4,
                DatabaseContract.Control.COLUMN_NAME_COL5,
                DatabaseContract.Control.COLUMN_NAME_COL6,
                DatabaseContract.Control.COLUMN_NAME_COL7,
                DatabaseContract.Control.COLUMN_NAME_COL8
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.Control._ID + " DESC LIMIT 1";

        // Define 'where' part of query.
        String selection = DatabaseContract.Control.COLUMN_NAME_COL1 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniquePatientId)};

        Cursor c = db.query(
                DatabaseContract.Control.TABLE_NAME, // The table to query
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
    public Cursor readAllData() {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Control._ID,
                DatabaseContract.Control.COLUMN_NAME_COL1,
                DatabaseContract.Control.COLUMN_NAME_COL2,
                DatabaseContract.Control.COLUMN_NAME_COL3,
                DatabaseContract.Control.COLUMN_NAME_COL4,
                DatabaseContract.Control.COLUMN_NAME_COL5,
                DatabaseContract.Control.COLUMN_NAME_COL6,
                DatabaseContract.Control.COLUMN_NAME_COL7,
                DatabaseContract.Control.COLUMN_NAME_COL8
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.Control._ID + " DESC LIMIT 1";

        // Define 'where' part of query.
        String selection = null;

        // Specify arguments in placeholder order.
        String[] selectionArgs = null;

        Cursor c = db.query(
                DatabaseContract.Control.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        return c;
    }
}
