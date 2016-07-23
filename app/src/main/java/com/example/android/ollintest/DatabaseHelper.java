package com.example.android.ollintest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by netzahdzc on 7/4/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.Patient.CREATE_TABLE);
        db.execSQL(DatabaseContract.User.CREATE_TABLE);
        db.execSQL(DatabaseContract.Session.CREATE_TABLE);
        db.execSQL(DatabaseContract.Control.CREATE_TABLE);
        db.execSQL(DatabaseContract.Test.CREATE_TABLE);
        db.execSQL(DatabaseContract.SensorAcc.CREATE_TABLE);
        db.execSQL(DatabaseContract.Technical.CREATE_TABLE);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.Patient.DELETE_TABLE);
        db.execSQL(DatabaseContract.User.DELETE_TABLE);
        db.execSQL(DatabaseContract.Session.DELETE_TABLE);
        db.execSQL(DatabaseContract.Control.DELETE_TABLE);
        db.execSQL(DatabaseContract.Test.DELETE_TABLE);
        db.execSQL(DatabaseContract.SensorAcc.DELETE_TABLE);
        db.execSQL(DatabaseContract.Technical.DELETE_TABLE);
        onCreate(db);
    }

    // Method is called during an upgrade of the database
    public void deleteDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.Patient.DELETE_TABLE);
        db.execSQL(DatabaseContract.User.DELETE_TABLE);
        db.execSQL(DatabaseContract.Session.DELETE_TABLE);
        db.execSQL(DatabaseContract.Control.DELETE_TABLE);
        db.execSQL(DatabaseContract.Test.DELETE_TABLE);
        db.execSQL(DatabaseContract.SensorAcc.DELETE_TABLE);
        db.execSQL(DatabaseContract.Technical.DELETE_TABLE);
        onCreate(db);
    }


}
