package com.inger.android.ollintest;

import android.provider.BaseColumns;

/**
 * Created by netzahdzc on 7/4/16.
 */
public final class DatabaseContract {

    public static final int DATABASE_VERSION = 28;
    public static final String DATABASE_NAME = "ollintest.db";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String TEXT_TYPE = " TEXT";
    private static final String IMAGE_TYPE = " BLOB ";
    private static final String COMMA_SEP = ", ";

    private DatabaseContract() {
    }

    public static abstract class Patient implements BaseColumns {
        public static final String TABLE_NAME = "patients";
        public static final String COLUMN_NAME_COL1 = "name";
        public static final String COLUMN_NAME_COL2 = "surname";
        public static final String COLUMN_NAME_COL3 = "gender";
        public static final String COLUMN_NAME_COL4 = "birthday";
        public static final String COLUMN_NAME_COL5 = "photo";
        public static final String COLUMN_NAME_COL6 = "trash";
        public static final String COLUMN_NAME_COL7 = "created";
        public static final String COLUMN_NAME_COL8 = "last_updated";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_NAME_COL1 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + IMAGE_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL8 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Control implements BaseColumns {
        public static final String TABLE_NAME = "controls";
        public static final String COLUMN_NAME_COL1 = "patient_id";
        public static final String COLUMN_NAME_COL2 = "weight";
        public static final String COLUMN_NAME_COL3 = "height";
        public static final String COLUMN_NAME_COL4 = "waist_size";
        public static final String COLUMN_NAME_COL5 = "heart_rate";
        public static final String COLUMN_NAME_COL6 = "systolic_blood";
        public static final String COLUMN_NAME_COL7 = "diastolic_blood";
        public static final String COLUMN_NAME_COL8 = "created";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL8 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Test implements BaseColumns {
        public static final String TABLE_NAME = "tests";
        public static final String COLUMN_NAME_COL1 = "patient_id";
        public static final String COLUMN_NAME_COL2 = "type_test";
        public static final String COLUMN_NAME_COL3 = "test_option";
        public static final String COLUMN_NAME_COL4 = "beginning_sensor_collection_timestamp";
        public static final String COLUMN_NAME_COL5 = "finishing_sensor_collection_timestamp";
        public static final String COLUMN_NAME_COL6 = "q1";
        public static final String COLUMN_NAME_COL7 = "q2";
        public static final String COLUMN_NAME_COL8 = "q3";
        public static final String COLUMN_NAME_COL9 = "q4";
        public static final String COLUMN_NAME_COL10 = "q5";
        public static final String COLUMN_NAME_COL11 = "q6";
        public static final String COLUMN_NAME_COL12 = "q7";
        public static final String COLUMN_NAME_COL13 = "q8";
        public static final String COLUMN_NAME_COL14 = "q9";
        public static final String COLUMN_NAME_COL15 = "q10";
        public static final String COLUMN_NAME_COL16 = "data_evaluation_score";
        public static final String COLUMN_NAME_COL17 = "data_evaluation_description";
        public static final String COLUMN_NAME_COL18 = "status";
        public static final String COLUMN_NAME_COL19 = "last_updated";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL8 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL9 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL10 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL11 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL12 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL13 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL14 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL15 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL16 + REAL_TYPE + COMMA_SEP +
                COLUMN_NAME_COL17 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL18 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL19 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Technical implements BaseColumns {
        public static final String TABLE_NAME = "technicals";
        public static final String COLUMN_NAME_COL1 = "patient_id";
        public static final String COLUMN_NAME_COL2 = "test_id";
        public static final String COLUMN_NAME_COL3 = "mobile_model";
        public static final String COLUMN_NAME_COL4 = "mobile_brand";
        public static final String COLUMN_NAME_COL5 = "mobile_android_api";
        public static final String COLUMN_NAME_COL6 = "app_version";
        public static final String COLUMN_NAME_COL7 = "acc_model";
        public static final String COLUMN_NAME_COL8 = "created";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL8 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_COL1 = "name";
        public static final String COLUMN_NAME_COL2 = "surname";
        public static final String COLUMN_NAME_COL3 = "gender";
        public static final String COLUMN_NAME_COL4 = "email";
        public static final String COLUMN_NAME_COL5 = "password";
        public static final String COLUMN_NAME_COL6 = "activation_code";
        public static final String COLUMN_NAME_COL7 = "activation_status";
        public static final String COLUMN_NAME_COL8 = "trash";
        public static final String COLUMN_NAME_COL9 = "created";
        public static final String COLUMN_NAME_COL10 = "last_update";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_NAME_COL1 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL8 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL9 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL10 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Session implements BaseColumns {
        public static final String TABLE_NAME = "sessions";
        public static final String COLUMN_NAME_COL1 = "user_id";
        public static final String COLUMN_NAME_COL2 = "created";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}