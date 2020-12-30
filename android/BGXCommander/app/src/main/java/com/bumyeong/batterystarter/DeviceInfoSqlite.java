package com.bumyeong.batterystarter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.Date;

public class DeviceInfoSqlite {
    private static final String TAG = "BATTERY_STARTER";
    private static DeviceInfoSqlite mInstance = null;
    private static String mErrorMeesage = "";

    private final String DATABASE_NAME = "BATTER_STARTER.db";
    private final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS DEVICE_INFO "
            + "( ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "  NAME TEXT, "
            + "  MAC_ADDRESS TEXT, "
            + "  IS_DELETE INTEGER, "
            + "  CHANGE_DATE LONG );";

    private SQLiteDatabase mDB = null;
    Context mContext = null;

    public static DeviceInfoSqlite getInstance(Context context) {
        try {
            if( mInstance == null ) {
                mInstance = new DeviceInfoSqlite(context);
            }
        }
        catch (SQLiteException ex) {
            mErrorMeesage = ex.getMessage();
            Log.e(TAG, ex.getMessage());
            mInstance = null;
        }

        return mInstance;
    }

    public static String getErrorMessage() {
        return mErrorMeesage;
    }

    private DeviceInfoSqlite(Context context) {
        mContext = context;
        mDB = context.openOrCreateDatabase(DATABASE_NAME, context.MODE_PRIVATE, null);
        mDB.execSQL(SQL_CREATE);
    }

    private int getSQLCounter(String SQL) {
        int result = 0;

        try {
            Cursor rd = mDB.rawQuery(SQL, null);

            if( rd != null ) {
                rd.moveToFirst();
                result = rd.getInt(0);
            }
        }
        catch (SQLiteException ex) {
            mErrorMeesage = ex.getMessage();
            Log.e(TAG, ex.getMessage());
        }

        Log.d(TAG, "getSQLCounter() return " + result);
        return result;
    }

    public int getDataCounter() {
        String SQL_COUNT = "SELECT COUNT(*) AS COUNTER FROM DEVICE_INFO WHERE IS_DELETE = 0;";
        return getSQLCounter(SQL_COUNT);
    }

    private int getDataCounter(String name, String address) {
        String sql = "SELECT COUNT(*) FROM DEVICE_INFO WHERE IS_DELETE = 0 "
                + "AND NAME = '" + name + "' "
                + "AND MAC_ADDRESS = '" + address +"'; ";
        return getSQLCounter(sql);
    }

    private void updateTime(String name, String address) {
        long datetime = new Date().getTime();
        String sql = "UPDATE DEVICE_INFO SET CHANGE_DATE = " + datetime + " WHERE IS_DELETE = 0 "
                + "AND NAME = '" + name + "' "
                + "AND MAC_ADDRESS = '" + address +"'; ";
        mDB.execSQL(sql);
    }

    private void insertNewDevice(String name, String address) {
        long datetime = new Date().getTime();
        String sql = "INSERT INTO DEVICE_INFO(NAME, MAC_ADDRESS, IS_DELETE, CHANGE_DATE) VALUES ('"
                + name + "' , '" + address + "' , 0 , " + datetime + ");";
        mDB.execSQL(sql);
    }

    public void UpdateDevcieInfoSQLite(String name, String address) {
        if( getDataCounter(name, address) == 0 ) {
            Log.d(TAG, "NEW DEVICE - " + name + "," + address);
            insertNewDevice(name, address);
        }
        else  {
            Log.d(TAG, "UPDATE DEVICE - " + name + "," + address);
            updateTime(name, address);
        }
    }

}
