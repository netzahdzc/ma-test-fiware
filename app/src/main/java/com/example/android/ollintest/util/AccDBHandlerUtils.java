package com.example.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.ollintest.DatabaseContract;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class AccDBHandlerUtils extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    public AccDBHandlerUtils(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // This method allows to store info into database
    public void insertData(long uniquePatientId, long uniqueTestId, double accTimestamp, int accAccuracy,
                           double accX, double accY, double accZ, String accType) {

        db = getWritableDatabase();

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
        db.insert(
                DatabaseContract.SensorAcc.TABLE_NAME,
                null,
                values);

        db.close();
    }


//    // This method reads info from database
//    public Cursor readData(long uniquePatientId) {
//
//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] projection = {
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL1,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL2,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL3,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL4,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL5,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL6,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL7,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL8,
//                DatabaseContract.SensorAcc.COLUMN_NAME_COL9
//        };
//
//        // How you want the results sorted in the resulting Cursor
//        String sortOrder = DatabaseContract.SensorAcc._ID + " DESC ";
//
//        // Define 'where' part of query.
//        String selection = DatabaseContract.SensorAcc.COLUMN_NAME_COL1 + " LIKE ?";
//
//        // Specify arguments in placeholder order.
//        String[] selectionArgs = {String.valueOf(uniquePatientId)};
//
//        Cursor c = db.query(
//                DatabaseContract.SensorAcc.TABLE_NAME, // The table to query
//                projection,                         // The columns to return
//                selection,                          // The columns for the WHERE clause
//                selectionArgs,                      // The values for the WHERE clause
//                null,                               // don't group the rows
//                null,                               // don't filter by row groups
//                sortOrder                           // The sort order
//        );
//
//        return c;
//    }


}

