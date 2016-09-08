package com.inger.android.ollintest;

import android.provider.BaseColumns;

/**
 * Created by netzahdzc on 7/4/16.
 */
public final class DatabaseContractOrient {

    public static final int DATABASE_VERSION = 30;
    public static final String DATABASE_NAME = "ollintest_orientation.db";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";

    private DatabaseContractOrient() {
    }

    public static abstract class SensorOrient implements BaseColumns {
        public static final String TABLE_NAME = "sensor_orientation";
        public static final String COLUMN_NAME_COL1 = "patient_id";
        public static final String COLUMN_NAME_COL2 = "test_id";
        public static final String COLUMN_NAME_COL3 = "azimuth";
        public static final String COLUMN_NAME_COL4 = "pitch";
        public static final String COLUMN_NAME_COL5 = "roll";
        public static final String COLUMN_NAME_COL6 = "created";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}