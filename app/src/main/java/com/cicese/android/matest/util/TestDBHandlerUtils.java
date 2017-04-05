package com.cicese.android.matest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cicese.android.matest.DatabaseContract;
import com.cicese.android.matest.DatabaseHelper;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class TestDBHandlerUtils {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mTestDBHandlerUtils;

    public TestDBHandlerUtils(Context context) {
        mTestDBHandlerUtils = context;
        mDbHelper = new DatabaseHelper(mTestDBHandlerUtils);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method allows to store info into database
    public long insertData(long uniquePatientId, long testType, long testOption,
                           String testFinishingTimestamp, String testQ1,
                           String testQ2, String testQ3, String testQ4, String testQ5, String testQ6,
                           String testQ7, String testQ8, String testQ9, String testQ10,
                           float testDataEvaluationScore, String testDataEvaluationDescription,
                           String testStatus) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Test.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL2, testType);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL3, testOption);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL4, dateObj.getCurrentDate());
        values.put(DatabaseContract.Test.COLUMN_NAME_COL5, testFinishingTimestamp);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL6, testQ1);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL7, testQ2);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL8, testQ3);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL9, testQ4);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL10, testQ5);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL11, testQ6);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL12, testQ7);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL13, testQ8);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL14, testQ9);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL15, testQ10);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL16, testDataEvaluationScore);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL17, testDataEvaluationDescription);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL18, testStatus);
        values.put(DatabaseContract.Test.COLUMN_NAME_COL19, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.Test.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method updates info from database
    public int updateData(long uniqueTestId, long uniquePatientId, long testType, long testOption,
                          String testFinishingTimestamp, String testQ1,
                          String testQ2, String testQ3, String testQ4, String testQ5, String testQ6,
                          String testQ7, String testQ8, String testQ9, String testQ10, float testDataEvaluationScore,
                          String testDataEvaluationDescription, String testStatus) {

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        if (uniquePatientId != 0)
            values.put(DatabaseContract.Test.COLUMN_NAME_COL1, uniquePatientId);
        if (testType != 0) values.put(DatabaseContract.Test.COLUMN_NAME_COL2, testType);
        if (testOption != 0) values.put(DatabaseContract.Test.COLUMN_NAME_COL3, testOption);
        if (!testFinishingTimestamp.isEmpty())
            values.put(DatabaseContract.Test.COLUMN_NAME_COL5, testFinishingTimestamp);
        if (!testQ1.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL6, testQ1);
        if (!testQ2.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL7, testQ2);
        if (!testQ3.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL8, testQ3);
        if (!testQ4.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL9, testQ4);
        if (!testQ5.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL10, testQ5);
        if (!testQ6.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL11, testQ6);
        if (!testQ7.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL12, testQ7);
        if (!testQ8.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL13, testQ8);
        if (!testQ9.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL14, testQ9);
        if (!testQ10.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL15, testQ10);
        if (testDataEvaluationScore != 0.0)
            values.put(DatabaseContract.Test.COLUMN_NAME_COL16, testDataEvaluationScore);
        if (!testDataEvaluationDescription.isEmpty())
            values.put(DatabaseContract.Test.COLUMN_NAME_COL17, testDataEvaluationDescription);
        if (!testStatus.isEmpty()) values.put(DatabaseContract.Test.COLUMN_NAME_COL18, testStatus);

        values.put(DatabaseContract.Test.COLUMN_NAME_COL19, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.Test._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueTestId)};

        int count = db.update(
                DatabaseContract.Test.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;
    }

    // This method reads info from database
    public Cursor readParticipantData(long uniquePatientId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL2,
                DatabaseContract.Test.COLUMN_NAME_COL3,
                DatabaseContract.Test.COLUMN_NAME_COL4,
                DatabaseContract.Test.COLUMN_NAME_COL5,
                DatabaseContract.Test.COLUMN_NAME_COL6,
                DatabaseContract.Test.COLUMN_NAME_COL7,
                DatabaseContract.Test.COLUMN_NAME_COL8,
                DatabaseContract.Test.COLUMN_NAME_COL9,
                DatabaseContract.Test.COLUMN_NAME_COL10,
                DatabaseContract.Test.COLUMN_NAME_COL11,
                DatabaseContract.Test.COLUMN_NAME_COL12,
                DatabaseContract.Test.COLUMN_NAME_COL13,
                DatabaseContract.Test.COLUMN_NAME_COL14,
                DatabaseContract.Test.COLUMN_NAME_COL15,
                DatabaseContract.Test.COLUMN_NAME_COL16,
                DatabaseContract.Test.COLUMN_NAME_COL17,
                DatabaseContract.Test.COLUMN_NAME_COL18,
                DatabaseContract.Test.COLUMN_NAME_COL19
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " DESC ";

        // Define 'where' part of query.
        String selection = DatabaseContract.Test.COLUMN_NAME_COL1 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniquePatientId)};

        Cursor c = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
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
    public Cursor readData(long uniqueTestId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL2,
                DatabaseContract.Test.COLUMN_NAME_COL3,
                DatabaseContract.Test.COLUMN_NAME_COL4,
                DatabaseContract.Test.COLUMN_NAME_COL5,
                DatabaseContract.Test.COLUMN_NAME_COL6,
                DatabaseContract.Test.COLUMN_NAME_COL7,
                DatabaseContract.Test.COLUMN_NAME_COL8,
                DatabaseContract.Test.COLUMN_NAME_COL9,
                DatabaseContract.Test.COLUMN_NAME_COL10,
                DatabaseContract.Test.COLUMN_NAME_COL11,
                DatabaseContract.Test.COLUMN_NAME_COL12,
                DatabaseContract.Test.COLUMN_NAME_COL13,
                DatabaseContract.Test.COLUMN_NAME_COL14,
                DatabaseContract.Test.COLUMN_NAME_COL15,
                DatabaseContract.Test.COLUMN_NAME_COL16,
                DatabaseContract.Test.COLUMN_NAME_COL17,
                DatabaseContract.Test.COLUMN_NAME_COL18,
                DatabaseContract.Test.COLUMN_NAME_COL19
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " DESC ";

        // Define 'where' part of query.
        String selection = DatabaseContract.Test._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueTestId)};

        Cursor c = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
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
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL2,
                DatabaseContract.Test.COLUMN_NAME_COL3,
                DatabaseContract.Test.COLUMN_NAME_COL4,
                DatabaseContract.Test.COLUMN_NAME_COL5,
                DatabaseContract.Test.COLUMN_NAME_COL6,
                DatabaseContract.Test.COLUMN_NAME_COL7,
                DatabaseContract.Test.COLUMN_NAME_COL8,
                DatabaseContract.Test.COLUMN_NAME_COL9,
                DatabaseContract.Test.COLUMN_NAME_COL10,
                DatabaseContract.Test.COLUMN_NAME_COL11,
                DatabaseContract.Test.COLUMN_NAME_COL12,
                DatabaseContract.Test.COLUMN_NAME_COL13,
                DatabaseContract.Test.COLUMN_NAME_COL14,
                DatabaseContract.Test.COLUMN_NAME_COL15,
                DatabaseContract.Test.COLUMN_NAME_COL16,
                DatabaseContract.Test.COLUMN_NAME_COL17,
                DatabaseContract.Test.COLUMN_NAME_COL18,
                DatabaseContract.Test.COLUMN_NAME_COL19
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " DESC ";

        // Define 'where' part of query.
        String selection = null;

        // Specify arguments in placeholder order.
        String[] selectionArgs = null;

        Cursor c = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        return c;
    }

    public long getUniqueIDOfLatestTestType(long uniqueParticipantId) {

        long latestTestID = 0;

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL19
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " DESC LIMIT 1";

        // Define 'where' part of query.
        String selection = DatabaseContract.Test.COLUMN_NAME_COL1 + " LIKE ? ";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueParticipantId)};

        Cursor cursor = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {
            latestTestID = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DatabaseContract.Test._ID)
            );
        }

//        cursor.close();

//        db.close();

        return latestTestID;
    }

    public String getLatestTestType(long uniqueParticipantId, int testTypeId) {

        String latestTest = "";

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL2,
                DatabaseContract.Test.COLUMN_NAME_COL19
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " DESC LIMIT 1";

        // Define 'where' part of query.
        String selection = DatabaseContract.Test.COLUMN_NAME_COL1 + " LIKE ? AND " +
                DatabaseContract.Test.COLUMN_NAME_COL2 + " LIKE ? AND " +
                DatabaseContract.Test.COLUMN_NAME_COL18 + " LIKE ? ";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueParticipantId), String.valueOf(testTypeId), "testCompleted"};

        Cursor cursor = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {
            latestTest = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL19)
            );
        }

//        cursor.close();

//        db.close();

        return latestTest;
    }

    public String getLatestTestTypeControl(long uniqueParticipantId, int testTypeId) {

        String latestTest = "";

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL2,
                DatabaseContract.Test.COLUMN_NAME_COL4
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " DESC LIMIT 1";

        // Define 'where' part of query.
        String selection = DatabaseContract.Test.COLUMN_NAME_COL1 + " LIKE ? AND " +
                DatabaseContract.Test.COLUMN_NAME_COL2 + " LIKE ? ";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueParticipantId), String.valueOf(testTypeId)};

        Cursor cursor = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {
            latestTest = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL4)
            );
        }

//        cursor.close();

//        db.close();

        return latestTest;
    }

    public Cursor getTodayTest(long uniqueParticipantId) {

        DateUtil dateObj = new DateUtil();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Test._ID,
                DatabaseContract.Test.COLUMN_NAME_COL1,
                DatabaseContract.Test.COLUMN_NAME_COL2,
                DatabaseContract.Test.COLUMN_NAME_COL3,
                DatabaseContract.Test.COLUMN_NAME_COL19
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.Test._ID + " ASC ";

        // Define 'where' part of query.
        String selection = DatabaseContract.Test.COLUMN_NAME_COL1 + " LIKE ? AND " +
                DatabaseContract.Test.COLUMN_NAME_COL19 + " LIKE ? AND " +
                DatabaseContract.Test.COLUMN_NAME_COL18 + " LIKE ? ";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniqueParticipantId),
                String.valueOf(dateObj.getCurrentDateSQL()+"%"),
        "testCompleted"};

        Cursor c = db.query(
                DatabaseContract.Test.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        return c;
    }

    public int getTestType(Cursor cursor) {
        int testType = 0;

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {
            testType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL2));
        }

        return testType;
    }

    public int getBalanceTestOption(Cursor cursor) {
        int testType = 0;

        // Reading all data and setting it up to be displayed
        if (cursor.moveToFirst()) {
            testType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Test.COLUMN_NAME_COL3));
        }

        return testType;
    }

}
