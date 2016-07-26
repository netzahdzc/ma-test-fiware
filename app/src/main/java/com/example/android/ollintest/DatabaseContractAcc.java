package com.example.android.ollintest;

import android.provider.BaseColumns;

/**
 * Created by netzahdzc on 7/4/16.
 */
public final class DatabaseContractAcc {

    public static final int DATABASE_VERSION = 27;
    public static final String DATABASE_NAME = "ollintest_acc.db";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String TEXT_TYPE = " TEXT";
    private static final String IMAGE_TYPE = " BLOB ";
    private static final String COMMA_SEP = ", ";

    private DatabaseContractAcc() {
    }

    public static abstract class SensorAcc implements BaseColumns {
        public static final String TABLE_NAME = "sensor_acc";
        public static final String COLUMN_NAME_COL1 = "patient_id";
        public static final String COLUMN_NAME_COL2 = "test_id";
        public static final String COLUMN_NAME_COL3 = "timestamp";
        public static final String COLUMN_NAME_COL4 = "accuracy";
        public static final String COLUMN_NAME_COL5 = "x";
        public static final String COLUMN_NAME_COL6 = "y";
        public static final String COLUMN_NAME_COL7 = "z";
        public static final String COLUMN_NAME_COL8 = "type";
        public static final String COLUMN_NAME_COL9 = "created";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL8 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL9 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}