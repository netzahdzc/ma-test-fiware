package com.cicese.android.matest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * Created by netzahdzc on 7/4/16.
 */
public class DatabaseHelperOrient extends SQLiteOpenHelper {

    private static final String APP_NAME = "three_ollin_test";
    private static final String APP_DIRECTORY_PATH = String.valueOf(
            Environment.getExternalStorageDirectory() + "/" + APP_NAME);
    private static final String[] DB_NAME_ARRAY = DatabaseContractOrient.DATABASE_NAME.split("\\.");

    public DatabaseHelperOrient(Context context) {
        super(context, APP_DIRECTORY_PATH +
                        File.separator +
                        "orient" +
                        File.separator +
                        DB_NAME_ARRAY[0] + "_" + System.currentTimeMillis() + "." + DB_NAME_ARRAY[1],
                null,
                DatabaseContractOrient.DATABASE_VERSION);
    }

    public DatabaseHelperOrient(Context context, String filePath) {
        super(context, filePath, null, DatabaseContractOrient.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContractOrient.SensorOrient.CREATE_TABLE);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContractOrient.SensorOrient.DELETE_TABLE);
        onCreate(db);
    }
}