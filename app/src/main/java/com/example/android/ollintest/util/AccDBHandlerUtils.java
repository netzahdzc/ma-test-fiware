package com.example.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.example.android.ollintest.DatabaseContractAcc;
import com.example.android.ollintest.DatabaseHelperAcc;

import java.io.File;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class AccDBHandlerUtils {

    private DatabaseHelperAcc mDbHelper;
    private SQLiteDatabase db;

    public AccDBHandlerUtils(Context context) {
        mDbHelper = new DatabaseHelperAcc(context);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method allows to store info into database
    public void insertData(long uniquePatientId, long uniqueTestId, double accTimestamp, int accAccuracy,
                           double accX, double accY, double accZ, String accType) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL1, uniquePatientId);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL2, uniqueTestId);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL3, accTimestamp);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL4, accAccuracy);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL5, accX);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL6, accY);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL7, accZ);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL8, accType);
        values.put(DatabaseContractAcc.SensorAcc.COLUMN_NAME_COL9, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        db.insert(
                DatabaseContractAcc.SensorAcc.TABLE_NAME,
                null,
                values);

    }
}

