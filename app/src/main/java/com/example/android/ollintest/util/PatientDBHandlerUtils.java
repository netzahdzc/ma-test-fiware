package com.example.android.ollintest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ollintest.DatabaseContract;
import com.example.android.ollintest.DatabaseHelper;
import com.example.android.ollintest.EditActivity;
import com.example.android.ollintest.R;
import com.example.android.ollintest.Utilities;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class PatientDBHandlerUtils {

    private SQLiteDatabase db;
    private DatabaseHelper mDbHelper;
    private Context mPatientDBHandlerUtils;

    public PatientDBHandlerUtils(Context context) {
        mPatientDBHandlerUtils = context;
        mDbHelper = new DatabaseHelper(mPatientDBHandlerUtils);
    }

    public void openDB() {
        db = mDbHelper.getWritableDatabase();
    }

    public void closeDB() {
        db.close();
    }

    // This method allows to store info into database
    public long insertData(String patientName, String patientSurname, String patientGender,
                           String patientBirthday, byte[] patientPhoto) {

        // Gets the data repository in write mode
        DateUtil dateObj = new DateUtil();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL1, patientName);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL2, patientSurname);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL3, patientGender);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL4, patientBirthday);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL5, patientPhoto);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL6, 2);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL7, dateObj.getCurrentDate());
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL8, dateObj.getCurrentDate());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.Patient.TABLE_NAME,
                null,
                values);

//        db.close();

        return newRowId;
    }

    // This method delete a specific patient record
    public int removeData(long uniquePatientId) {
        /**
         * IMPORTANT. Data will not be removed at any time, instead, it will be marked at not visible
         */

//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//        db.delete(DatabaseContract.Patient.TABLE_NAME, DatabaseContract.Patient._ID + " = " + id, null);

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL6, 1);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL8, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.Patient._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniquePatientId)};

        int count = db.update(
                DatabaseContract.Patient.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;

    }

    // This method reads info from database
    public Cursor readAllData() {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Patient._ID,
                DatabaseContract.Patient.COLUMN_NAME_COL1,
                DatabaseContract.Patient.COLUMN_NAME_COL2,
                DatabaseContract.Patient.COLUMN_NAME_COL3,
                DatabaseContract.Patient.COLUMN_NAME_COL4,
                DatabaseContract.Patient.COLUMN_NAME_COL5
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.Patient.COLUMN_NAME_COL1 + " ASC";

        // Define 'where' part of query.
        String selection = DatabaseContract.Patient.COLUMN_NAME_COL6 + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(2)};

        Cursor c = db.query(
                DatabaseContract.Patient.TABLE_NAME, // The table to query
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
    public Cursor readData(long uniquePatientId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.Patient._ID,
                DatabaseContract.Patient.COLUMN_NAME_COL1,
                DatabaseContract.Patient.COLUMN_NAME_COL2,
                DatabaseContract.Patient.COLUMN_NAME_COL3,
                DatabaseContract.Patient.COLUMN_NAME_COL4,
                DatabaseContract.Patient.COLUMN_NAME_COL5
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.Patient._ID + " DESC";

        // Define 'where' part of query.
        String selection = DatabaseContract.Patient._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniquePatientId)};

        Cursor c = db.query(
                DatabaseContract.Patient.TABLE_NAME, // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );

        return c;
    }

    // This method updates info from database
    public int updateData(long uniquePatientId, String patientName, String patientSurname,
                          String patientGender, String patientBirthday, byte[] patientPhoto) {

        DateUtil dateObj = new DateUtil();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL1, patientName);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL2, patientSurname);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL3, patientGender);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL4, patientBirthday);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL5, patientPhoto);
        values.put(DatabaseContract.Patient.COLUMN_NAME_COL8, dateObj.getCurrentDate());

        // Which row to update, based on the ID
        String selection = DatabaseContract.Patient._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(uniquePatientId)};

        int count = db.update(
                DatabaseContract.Patient.TABLE_NAME,
                values,
                selection,
                selectionArgs);

//        db.close();

        return count;
    }

}
