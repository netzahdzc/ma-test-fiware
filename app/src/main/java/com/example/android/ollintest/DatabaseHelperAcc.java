package com.example.android.ollintest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * Created by netzahdzc on 7/4/16.
 */
public class DatabaseHelperAcc extends SQLiteOpenHelper {

    private static final String APP_NAME = "three_ollin_test";
    private static final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);

    public DatabaseHelperAcc(Context context) {
        super(context, APP_DIRECTORY_PATH +
                        File.separator +
                        "acc" +
                        File.separator +
                        System.currentTimeMillis() + "_" + DatabaseContractAcc.DATABASE_NAME,
                null,
                DatabaseContractAcc.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContractAcc.SensorAcc.CREATE_TABLE);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContractAcc.SensorAcc.DELETE_TABLE);
        onCreate(db);
    }
}