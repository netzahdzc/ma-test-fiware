package com.cicese.android.matest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cicese.android.matest.DatabaseContractAcc;
import com.cicese.android.matest.DatabaseHelperAcc;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class AccDBHandlerUtils {

    private DatabaseHelperAcc mDbHelper;
    private SQLiteDatabase db;

    public AccDBHandlerUtils(Context context) {
        mDbHelper = new DatabaseHelperAcc(context);
    }

    public AccDBHandlerUtils(Context context, String sourceFileUri){
        mDbHelper = new DatabaseHelperAcc(context, sourceFileUri);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    public String getPath(){
        String path = "";
        path = mDbHelper.getDatabaseName();
        return path;
    }

    // This method allows to store info into database
    public void insertData(long uniquePatientId, long uniqueTestId, int accAccuracy,
                           double accX, double accY, double accZ) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL2, uniqueTestId);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL3, accAccuracy);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL4, accX);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL5, accY);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL6, accZ);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        db.insert(
                DatabaseContractAcc.SensorAcc.TABLE_NAME,
                null,
                values);
    }

    // This method reads info from database
    public Cursor readData() {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL1,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL2,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL3,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL4,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL5,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL6,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        // Define 'where' part of query.
        String selection = null;

        // Specify arguments in placeholder order.
        String[] selectionArgs = null;

        Cursor c = db.query(
                DatabaseContractAcc.SensorAcc.TABLE_NAME,   // The table to query
                projection,                                 // The columns to return
                selection,                                  // The columns for the WHERE clause
                selectionArgs,                              // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                sortOrder                                   // The sort order
        );

        return c;
    }

    // This method reads last data point
    public Cursor readLastData() {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL1,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL2,
                DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7 + " DESC LIMIT 1 ";

        // Define 'where' part of query.
        String selection = null;

        // Specify arguments in placeholder order.
        String[] selectionArgs = null;

        Cursor c = db.query(
                DatabaseContractAcc.SensorAcc.TABLE_NAME,   // The table to query
                projection,                                 // The columns to return
                selection,                                  // The columns for the WHERE clause
                selectionArgs,                              // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                sortOrder                                   // The sort order
        );

        return c;
    }
}

