package com.example.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.ollintest.DatabaseContract;
import com.example.android.ollintest.DatabaseHelper;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class AccDBHandlerUtils {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mAccDBHandlerUtils;

    public AccDBHandlerUtils(Context context) {
        mAccDBHandlerUtils = context;
        mDbHelper = new DatabaseHelper(mAccDBHandlerUtils);
        db = mDbHelper.getWritableDatabase();
    }

    public void openDB(){
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method allows to store info into database
    public long insertData(long uniquePatientId, long uniqueTestId, String accTimestamp, String accAccuracy,
                           String accX, String accY, String accZ, String accType) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL2, uniqueTestId);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL3, accTimestamp);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL4, accAccuracy);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL5, accX);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL6, accY);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL7, accZ);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL8, accType);
        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL9, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.SensorAcc.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method updates info from database
    public int updateData(long uniqueAccId, long uniquePatientId, long uniqueTestId,
                          String accTimestamp, String accAccuracy,
                          String accX, String accY, String accZ, String accType) {

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        if (uniquePatientId != 0)
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL1, uniquePatientId);
        if (uniqueTestId != 0)
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL2, uniqueTestId);
        if (!accTimestamp.isEmpty())
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL3, accTimestamp);
        if (!accAccuracy.isEmpty())
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL4, accAccuracy);
        if (!accX.isEmpty())
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL5, accX);
        if (!accY.isEmpty())
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL6, accY);
        if (!accZ.isEmpty())
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL7, accZ);
        if (!accType.isEmpty())
            values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL8, accType);

        values.put(DatabaseContract.SensorAcc.COLUMN_NAME_COL9, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.SensorAcc._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueAccId)};

        int count = db.update(
                DatabaseContract.SensorAcc.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;
    }

    // This method reads info from database
    public Cursor readData(long uniquePatientId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.SensorAcc.COLUMN_NAME_COL1,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL2,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL3,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL4,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL5,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL6,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL7,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL8,
                DatabaseContract.SensorAcc.COLUMN_NAME_COL9
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.SensorAcc._ID + " DESC ";

        // Define 'where' part of query.
        String selection = DatabaseContract.SensorAcc.COLUMN_NAME_COL1 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniquePatientId)};

        Cursor c = db.query(
                DatabaseContract.SensorAcc.TABLE_NAME, // The table to query
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

