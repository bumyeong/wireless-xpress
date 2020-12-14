package com.bumyeong.batterystarter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

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
    private final String SQL_COUNT = "SELECT COUNT(*) AS COUNTER FROM DEVICE_INFO "
            + " WHERE IS_DELETE = 0 ";

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

    public int getDataCounter() {
        int result = 0;

        try {
            Cursor rd = mDB.rawQuery(SQL_COUNT, null);

            if( rd != null ) {
                rd.moveToFirst();
                result = rd.getInt(0);
            }
        }
        catch (SQLiteException ex) {
            mErrorMeesage = ex.getMessage();
            Log.e(TAG, ex.getMessage());
        }

        Log.d(TAG, "getDataCounter() return " + result);
        return result;
    }
}
