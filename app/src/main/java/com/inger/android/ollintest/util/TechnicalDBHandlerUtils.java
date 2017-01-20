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
public class TechnicalDBHandlerUtils {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mTechnicalDBHandlerUtils;

    public TechnicalDBHandlerUtils(Context context) {
        mTechnicalDBHandlerUtils = context;
        mDbHelper = new DatabaseHelper(mTechnicalDBHandlerUtils);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method allows to store info into database
    public long insertData(long uniquePatientId, long uniqueTestId, String techMobileModel, String techMobileBrand,
                           String techMobileAndroidApi, String techAppVersion) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL2, uniqueTestId);
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL3, techMobileModel);
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL4, techMobileBrand);
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL5, techMobileAndroidApi);
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL6, techAppVersion);
        values.put(DatabaseContract.Technical.COLUMN_NAME_COL7, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.Technical.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method updates info from database
    public int updateData(long uniqueTechnicalId, long uniquePatientId, long uniqueTestId,
                          String techMobileModel, String techMobileBrand,
                          String techMobileAndroidApi, String techAppVersion, String techAccModel) {

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        if (uniquePatientId != 0)
            values.put(DatabaseContract.Technical.COLUMN_NAME_COL1, uniquePatientId);
        if (uniqueTestId != 0)
            values.put(DatabaseContract.Technical.COLUMN_NAME_COL2, uniqueTestId);
        if (!techMobileModel.isEmpty())
            values.put(DatabaseContract.Technical.COLUMN_NAME_COL3, techMobileModel);
        if (!techMobileBrand.isEmpty())
            values.put(DatabaseContract.Technical.COLUMN_NAME_COL4, techMobileBrand);
        if (!techMobileAndroidApi.isEmpty())
            values.put(DatabaseContract.Technical.COLUMN_NAME_COL5, techMobileAndroidApi);
        if (!techAppVersion.isEmpty())
            values.put(DatabaseContract.Technical.COLUMN_NAME_COL6, techAppVersion);

        values.put(DatabaseContract.Technical.COLUMN_NAME_COL7, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.Technical._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueTechnicalId)};

        int count = db.update(
                DatabaseContract.Technical.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;
    }

    // This method reads info from database
    public Cursor readData(long uniqueTechnicalId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Technical._ID,
                DatabaseContract.Technical.COLUMN_NAME_COL1,
                DatabaseContract.Technical.COLUMN_NAME_COL2,
                DatabaseContract.Technical.COLUMN_NAME_COL3,
                DatabaseContract.Technical.COLUMN_NAME_COL4,
                DatabaseContract.Technical.COLUMN_NAME_COL5,
                DatabaseContract.Technical.COLUMN_NAME_COL6,
                DatabaseContract.Technical.COLUMN_NAME_COL7
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.Technical._ID + " DESC ";

        // Define 'where' part of query.
        String selection = DatabaseContract.Technical._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueTechnicalId)};

        Cursor c = db.query(
                DatabaseContract.Technical.TABLE_NAME, // The table to query
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
    public Cursor readDataByTestId(long testId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Technical._ID,
                DatabaseContract.Technical.COLUMN_NAME_COL1,
                DatabaseContract.Technical.COLUMN_NAME_COL2,
                DatabaseContract.Technical.COLUMN_NAME_COL3,
                DatabaseContract.Technical.COLUMN_NAME_COL4,
                DatabaseContract.Technical.COLUMN_NAME_COL5,
                DatabaseContract.Technical.COLUMN_NAME_COL6,
                DatabaseContract.Technical.COLUMN_NAME_COL7
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.Technical._ID + " DESC ";

        // Define 'where' part of query.
        String selection = DatabaseContract.Technical.COLUMN_NAME_COL2 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(testId)};

        Cursor c = db.query(
                DatabaseContract.Technical.TABLE_NAME, // The table to query
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
