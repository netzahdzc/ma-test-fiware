package com.inger.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.inger.android.ollintest.DatabaseContractAcc;
import com.inger.android.ollintest.DatabaseHelperAcc;

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
}

