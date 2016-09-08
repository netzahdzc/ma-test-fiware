package com.inger.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.inger.android.ollintest.DatabaseContractAcc;
import com.inger.android.ollintest.DatabaseContractOrient;
import com.inger.android.ollintest.DatabaseHelperAcc;
import com.inger.android.ollintest.DatabaseHelperOrient;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class OrientDBHandlerUtils {

    private DatabaseHelperOrient mDbHelper;
    private SQLiteDatabase db;

    public OrientDBHandlerUtils(Context context) {
        mDbHelper = new DatabaseHelperOrient(context);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
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
}

