package com.cicese.android.matest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cicese.android.matest.DatabaseContractOrient;
import com.cicese.android.matest.DatabaseHelperOrient;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class OrientDBHandlerUtils {

    private DatabaseHelperOrient mDbHelper;
    private SQLiteDatabase db;

    public OrientDBHandlerUtils(Context context) {
        mDbHelper = new DatabaseHelperOrient(context);
    }

    public OrientDBHandlerUtils(Context context, String sourceFileUri){
        mDbHelper = new DatabaseHelperOrient(context, sourceFileUri);
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
    public void insertData(long uniquePatientId, long uniqueTestId,
                           double azimuth, double pitch, double roll) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL2, uniqueTestId);
        values.put(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL3, azimuth);
        values.put(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL4, pitch);
        values.put(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL5, roll);
        values.put(DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL6, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        db.insert(
                DatabaseContractOrient.SensorOrient.TABLE_NAME,
                null,
                values);

    }

    // This method reads info from database
    public Cursor readData() {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL1,
                DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL2,
                DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL3,
                DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL4,
                DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL5,
                DatabaseContractOrient.SensorOrient.COLUMN_NAME_COL6
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        // Define 'where' part of query.
        String selection = null;

        // Specify arguments in placeholder order.
        String[] selectionArgs = null;

        Cursor c = db.query(
                DatabaseContractOrient.SensorOrient.TABLE_NAME, // The table to query
                projection,                                     // The columns to return
                selection,                                      // The columns for the WHERE clause
                selectionArgs,                                  // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                sortOrder                                       // The sort order
        );

        return c;
    }
}

