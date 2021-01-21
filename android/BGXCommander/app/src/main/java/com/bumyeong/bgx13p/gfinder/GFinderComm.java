package com.bumyeong.bgx13p.gfinder;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GFinderComm {
    private final static String TAG = "bgx_dbg"; //DeviceList.class.getSimpleName();

    public final static byte COMMAND_CONNECTION_OK = (byte)0x01;
    public final static byte COMMAND_SEND_DATA_INIT = (byte)0x03;
    public final static byte COMMAND_SEND_DATA = (byte)0x05;
    public final static byte COMMAND_GF_DISCONNECT = (byte)0x07;
    public final static byte COMMAND_ACK = (byte)0xfc;
    public final static byte COMMAND_NACK = (byte)0xff;
    public final static byte COMMAND_UNKNOWN = (byte)0xfe;

    private final static int MAX_PACKET_SIZE = 255;
    private final static int MIN_PACKET_SIZE = 14;
    private final static int POS_COMMAND = 0;
    private final static int POS_LENGTH = 1;
    private final static int POS_DATA_START = 2;

    private final static int MAX_INIT_DATA_COUNT = 8;

    //03 10 B2 9D F6 ED 04 18 FF 02 C4 00 EA 00 18 00 31 00
    private final static int POS_INIT_DATA_MAC = 2;
    private final static int POS_INIT_DATA_START = 9;
    private final static int POS_INIT_DATA_O2_LOW = 10;
    private final static int POS_INIT_DATA_O2_HIGH = 12;
    private final static int POS_INIT_DATA_CH4_LOW = 14;
    private final static int POS_INIT_DATA_CH4_HIGH = 16;
    private final static int POS_INIT_DATA_H2S_LOW = 10;
    private final static int POS_INIT_DATA_H2S_HIGH = 12;
    private final static int POS_INIT_DATA_CO_LOW = 14;
    private final static int POS_INIT_DATA_CO_HIGH = 16;

    private final static int POS_INIT_DATA_RESULT_O2_LOW = 0;
    private final static int POS_INIT_DATA_RESULT_O2_HIGH = 1;
    private final static int POS_INIT_DATA_RESULT_CH4_LOW = 2;
    private final static int POS_INIT_DATA_RESULT_CH4_HIGH = 3;
    private final static int POS_INIT_DATA_RESULT_H2S_LOW = 4;
    private final static int POS_INIT_DATA_RESULT_H2S_HIGH = 5;
    private final static int POS_INIT_DATA_RESULT_CO_LOW = 6;
    private final static int POS_INIT_DATA_RESULT_CO_HIGH = 7;

    //05 0F B2 9D F6 ED 04 18 FF D1 00 00 00 00 00 00 00
    private final static int CURRENT_DATA_SIZE = 15;
    private final static int POS_CURRENT_DATA_O2 = 9;
    private final static int POS_CURRENT_DATA_CH4 = 11;
    private final static int POS_CURRENT_DATA_H2S = 13;
    private final static int POS_CURRENT_DATA_CO = 15;

    private final static int POS_CURRENT_DATA_RESULT_O2 = 0;
    private final static int POS_CURRENT_DATA_RESULT_CH4 = 1;
    private final static int POS_CURRENT_DATA_RESULT_H2S = 2;
    private final static int POS_CURRENT_DATA_RESULT_CO = 3;

    private byte[] mPacket;
    private int mPacketSize = 2;

    private String mMacAddress="";
    private String mDeviceName="";
    private String mMacAddress2="";
    private String mDeviceName2="";

    private boolean[] mIsReceive;

    private int[] mInitDataResult;
    private int[] mDataCurrent;
    private long mLastTime;

    private int[] mInitDataResult2;
    private int[] mDataCurrent2;
    private long mLastTime2;

    public GFinderComm() {
        mPacket = new byte [MAX_PACKET_SIZE];
        mIsReceive = new boolean[MAX_INIT_DATA_COUNT];

        mInitDataResult = new int [] {195,235,25,50,100,150,35,200};
        mDataCurrent = new int [] {0,0,0,0};
        mInitDataResult2 = new int [] {195,235,25,50,100,150,35,200};
        mDataCurrent2 = new int [] {0,0,0,0};

        mDeviceName = "GFM-400";
        mDeviceName2 = "GFM-400";

        resetReceiveFlag();

        mLastTime = System.currentTimeMillis();
        mLastTime2 = System.currentTimeMillis();
    }

    private void resetReceiveFlag() {
        Arrays.fill(mIsReceive, false);
        mIsReceive[0] = true;
        mIsReceive[1] = true;
    }

    private boolean isReceiveFlag() {
        for (boolean flag:mIsReceive) {
            if( flag == false ) {
                return false;
            }
        }

        return true;
    }

    public byte[] getPacketConnectionOk() {
        Arrays.fill(mPacket, (byte)0);
        mPacket[POS_COMMAND] = COMMAND_CONNECTION_OK;

        resetReceiveFlag();

        mPacketSize = 2;
        return mPacket;
    }

    public byte[] getPacketSendData(byte[] data) {
        Arrays.fill(mPacket, (byte)0);

        mPacket[POS_COMMAND] = COMMAND_SEND_DATA;
        mPacket[POS_LENGTH] = (byte)data.length;

        for(int i = 0; i < data.length; i++ ) {
            mPacket[POS_DATA_START + i] = data[i];
        }

        mPacketSize = data.length + 2;
        return mPacket;
    }

    public byte[] getPacketRequestInformation() {
        Arrays.fill(mPacket, (byte)0);

        mPacket[POS_COMMAND] = COMMAND_SEND_DATA;
        mPacket[POS_LENGTH] = (byte)0x03;

        mPacket[POS_DATA_START + 0] = (byte)0x10;
        mPacket[POS_DATA_START + 1] = (byte)0x01;
        mPacket[POS_DATA_START + 2] = (byte)0x01;

        mPacketSize = 5;
        return mPacket;
    }

    public byte[] getPacketDisconnect() {
        Arrays.fill(mPacket, (byte)0);
        mPacket[POS_COMMAND] = COMMAND_GF_DISCONNECT;

        mPacketSize = 2;
        return mPacket;
    }

    public byte[] getPacketAck() {
        Arrays.fill(mPacket, (byte)0);
        mPacket[POS_COMMAND] = COMMAND_ACK;

        mPacketSize = 2;
        return mPacket;
    }

    public String getMacAddress()  { return mMacAddress; }
    public String getMacAddress2()  { return mMacAddress2; }

    public String getDeviceName() { return mDeviceName; }

    public int getPacketSize() { return mPacketSize; }

    public String getInitDataO2Low() { return String.format("%.1f", (float)mInitDataResult[POS_INIT_DATA_RESULT_O2_LOW] / 10); }
    public String getInitDataO2High() { return String.format("%.1f", (float)mInitDataResult[POS_INIT_DATA_RESULT_O2_HIGH] / 10); }
    public String getInitDataCH4Low() { return String.valueOf(mInitDataResult[POS_INIT_DATA_RESULT_CH4_LOW]); }
    public String getInitDataCH4High() { return String.valueOf(mInitDataResult[POS_INIT_DATA_RESULT_CH4_HIGH]); }
    public String getInitDataH2SLow() { return String.format("%.1f", (float)mInitDataResult[POS_INIT_DATA_RESULT_H2S_LOW] / 10); }
    public String getInitDataH2SHigh() { return String.format("%.1f", (float)mInitDataResult[POS_INIT_DATA_RESULT_H2S_HIGH] / 10); }
    public String getInitDataCoLow() { return String.valueOf(mInitDataResult[POS_INIT_DATA_RESULT_CO_LOW]); }
    public String getInitDataCoHigh() { return String.valueOf(mInitDataResult[POS_INIT_DATA_RESULT_CO_HIGH]); }
    public String getTime() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(mLastTime));
    }
    public String getCurrentDataO2() { return String.format("%.1f", (float)mDataCurrent[POS_CURRENT_DATA_RESULT_O2] / 10); }
    public String getCurrentDataCH4() { return String.valueOf(mDataCurrent[POS_CURRENT_DATA_RESULT_CH4]); }
    public String getCurrentDataH2S() { return String.format("%.1f", (float)mDataCurrent[POS_CURRENT_DATA_RESULT_H2S] / 10); }
    public String getCurrentDataCo() { return String.valueOf(mDataCurrent[POS_CURRENT_DATA_RESULT_CO]); }

    public boolean isAlarmO2() {
        if(mDataCurrent[POS_CURRENT_DATA_RESULT_O2] == 0
                || (mInitDataResult[POS_INIT_DATA_RESULT_O2_LOW] <= mDataCurrent[POS_CURRENT_DATA_RESULT_O2]
                && mDataCurrent[POS_CURRENT_DATA_RESULT_O2] <= mInitDataResult[POS_INIT_DATA_RESULT_O2_HIGH])) {
            return false;
        }

        return true;
    }
    public boolean isAlarmCH4() {
        if(mDataCurrent[POS_CURRENT_DATA_RESULT_CH4] == 0
                || (mInitDataResult[POS_INIT_DATA_RESULT_CH4_LOW] <= mDataCurrent[POS_CURRENT_DATA_RESULT_CH4]
                && mDataCurrent[POS_CURRENT_DATA_RESULT_CH4] <= mInitDataResult[POS_INIT_DATA_RESULT_CH4_HIGH])) {
            return false;
        }

        return true;
    }
    public boolean isAlarmH2S() {
        if(mDataCurrent[POS_CURRENT_DATA_RESULT_H2S] == 0
                || (mInitDataResult[POS_INIT_DATA_RESULT_H2S_LOW] <= mDataCurrent[POS_CURRENT_DATA_RESULT_H2S]
                && mDataCurrent[POS_CURRENT_DATA_RESULT_H2S] <= mInitDataResult[POS_INIT_DATA_RESULT_H2S_HIGH])) {
            return false;
        }

        return true;
    }
    public boolean isAlarmCo() {
        if(mDataCurrent[POS_CURRENT_DATA_RESULT_CO] == 0
                || (mInitDataResult[POS_INIT_DATA_RESULT_CO_LOW] <= mDataCurrent[POS_CURRENT_DATA_RESULT_CO]
                && mDataCurrent[POS_CURRENT_DATA_RESULT_CO] <= mInitDataResult[POS_INIT_DATA_RESULT_CO_HIGH])) {
            return false;
        }

        return true;
    }


    public String getInitDataO2Low2() { return String.format("%.1f", (float)mInitDataResult2[POS_INIT_DATA_RESULT_O2_LOW] / 10); }
    public String getInitDataO2High2() { return String.format("%.1f", (float)mInitDataResult2[POS_INIT_DATA_RESULT_O2_HIGH] / 10); }
    public String getInitDataCH4Low2() { return String.valueOf(mInitDataResult2[POS_INIT_DATA_RESULT_CH4_LOW]); }
    public String getInitDataCH4High2() { return String.valueOf(mInitDataResult2[POS_INIT_DATA_RESULT_CH4_HIGH]); }
    public String getInitDataH2SLow2() { return String.format("%.1f", (float)mInitDataResult2[POS_INIT_DATA_RESULT_H2S_LOW] / 10); }
    public String getInitDataH2SHigh2() { return String.format("%.1f", (float)mInitDataResult2[POS_INIT_DATA_RESULT_H2S_HIGH] / 10); }
    public String getInitDataCoLow2() { return String.valueOf(mInitDataResult2[POS_INIT_DATA_RESULT_CO_LOW]); }
    public String getInitDataCoHigh2() { return String.valueOf(mInitDataResult2[POS_INIT_DATA_RESULT_CO_HIGH]); }
    public String getTime2() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(mLastTime2));
    }
    public String getCurrentDataO22() { return String.format("%.1f", (float)mDataCurrent2[POS_CURRENT_DATA_RESULT_O2] / 10); }
    public String getCurrentDataCH42() { return String.valueOf(mDataCurrent[POS_CURRENT_DATA_RESULT_CH4]); }
    public String getCurrentDataH2S2() { return String.format("%.1f", (float)mDataCurrent2[POS_CURRENT_DATA_RESULT_H2S] / 10); }
    public String getCurrentDataCo2() { return String.valueOf(mDataCurrent2[POS_CURRENT_DATA_RESULT_CO]); }

    public boolean isAlarmO22() {
        if(mDataCurrent2[POS_CURRENT_DATA_RESULT_O2] == 0
                || (mInitDataResult2[POS_INIT_DATA_RESULT_O2_LOW] <= mDataCurrent2[POS_CURRENT_DATA_RESULT_O2]
                && mDataCurrent2[POS_CURRENT_DATA_RESULT_O2] <= mInitDataResult2[POS_INIT_DATA_RESULT_O2_HIGH])) {
            return false;
        }

        return true;
    }
    public boolean isAlarmCH42() {
        if(mDataCurrent2[POS_CURRENT_DATA_RESULT_CH4] == 0
                || (mInitDataResult2[POS_INIT_DATA_RESULT_CH4_LOW] <= mDataCurrent2[POS_CURRENT_DATA_RESULT_CH4]
                && mDataCurrent2[POS_CURRENT_DATA_RESULT_CH4] <= mInitDataResult2[POS_INIT_DATA_RESULT_CH4_HIGH])) {
            return false;
        }

        return true;
    }
    public boolean isAlarmH2S2() {
        if(mDataCurrent2[POS_CURRENT_DATA_RESULT_H2S] == 0
                || (mInitDataResult2[POS_INIT_DATA_RESULT_H2S_LOW] <= mDataCurrent2[POS_CURRENT_DATA_RESULT_H2S]
                && mDataCurrent2[POS_CURRENT_DATA_RESULT_H2S] <= mInitDataResult2[POS_INIT_DATA_RESULT_H2S_HIGH])) {
            return false;
        }

        return true;
    }
    public boolean isAlarmCo2() {
        if(mDataCurrent2[POS_CURRENT_DATA_RESULT_CO] == 0
                || (mInitDataResult2[POS_INIT_DATA_RESULT_CO_LOW] <= mDataCurrent2[POS_CURRENT_DATA_RESULT_CO]
                && mDataCurrent2[POS_CURRENT_DATA_RESULT_CO] <= mInitDataResult2[POS_INIT_DATA_RESULT_CO_HIGH])) {
            return false;
        }

        return true;
    }

    public byte parse(byte[] data) {
        if( data.length < MIN_PACKET_SIZE ) {
            Log.e(TAG, "Unknown Packet. length:" + data.length);
            return COMMAND_UNKNOWN;
        }

        int length = data[POS_LENGTH];
        byte btValue = data[POS_COMMAND];

        switch( btValue) {
            case COMMAND_SEND_DATA_INIT: {
                int result = 0;
                String receiveMac = parseMacAddress(data);
                int index = data[POS_INIT_DATA_START] & 0xFF;

                if( getIndexMacAddress(receiveMac) == 1 ) {
                    if( index == 2 ) {
                        resetReceiveFlag();
                    }

                    if( index == 2 ) {
                        result = data[POS_INIT_DATA_O2_LOW] & 0xff;
                        result += (int)(data[POS_INIT_DATA_O2_LOW + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_O2_LOW] = result - 1;

                        result = data[POS_INIT_DATA_O2_HIGH] & 0xff;
                        result += (int)(data[POS_INIT_DATA_O2_HIGH + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_O2_HIGH] = result + 1;

                        result = data[POS_INIT_DATA_CH4_LOW] & 0xff;
                        result += (data[POS_INIT_DATA_CH4_LOW + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_CH4_LOW] = result + 1;

                        result = data[POS_INIT_DATA_CH4_HIGH] & 0xff;
                        result +=(data[POS_INIT_DATA_CH4_HIGH + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_CH4_HIGH] = result + 1;
                    }
                    else if( index == 3 ) {
                        result = data[POS_INIT_DATA_H2S_LOW] & 0xff;
                        result +=(data[POS_INIT_DATA_H2S_LOW + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_H2S_LOW] = result + 1;

                        result = data[POS_INIT_DATA_H2S_HIGH] & 0xff;
                        result += (data[POS_INIT_DATA_H2S_HIGH + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_H2S_HIGH] = result + 1;

                        result = data[POS_INIT_DATA_CO_LOW] & 0xff;
                        result += (data[POS_INIT_DATA_CO_LOW + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_CO_LOW] = result + 1;

                        result = data[POS_INIT_DATA_CO_HIGH] & 0xff;
                        result += (data[POS_INIT_DATA_CO_HIGH + 1] & 0xff) << 8;
                        mInitDataResult[POS_INIT_DATA_RESULT_CO_HIGH] = result + 1;
                    }
                }
                else {
                    if( index == 2 ) {
                        resetReceiveFlag();
                    }

                    if( index == 2 ) {
                        result = data[POS_INIT_DATA_O2_LOW] & 0xff;
                        result += (int)(data[POS_INIT_DATA_O2_LOW + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_O2_LOW] = result - 1;

                        result = data[POS_INIT_DATA_O2_HIGH] & 0xff;
                        result += (int)(data[POS_INIT_DATA_O2_HIGH + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_O2_HIGH] = result + 1;

                        result = data[POS_INIT_DATA_CH4_LOW] & 0xff;
                        result += (data[POS_INIT_DATA_CH4_LOW + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_CH4_LOW] = result + 1;

                        result = data[POS_INIT_DATA_CH4_HIGH] & 0xff;
                        result +=(data[POS_INIT_DATA_CH4_HIGH + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_CH4_HIGH] = result + 1;
                    }
                    else if( index == 3 ) {
                        result = data[POS_INIT_DATA_H2S_LOW] & 0xff;
                        result +=(data[POS_INIT_DATA_H2S_LOW + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_H2S_LOW] = result + 1;

                        result = data[POS_INIT_DATA_H2S_HIGH] & 0xff;
                        result += (data[POS_INIT_DATA_H2S_HIGH + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_H2S_HIGH] = result + 1;

                        result = data[POS_INIT_DATA_CO_LOW] & 0xff;
                        result += (data[POS_INIT_DATA_CO_LOW + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_CO_LOW] = result + 1;

                        result = data[POS_INIT_DATA_CO_HIGH] & 0xff;
                        result += (data[POS_INIT_DATA_CO_HIGH + 1] & 0xff) << 8;
                        mInitDataResult2[POS_INIT_DATA_RESULT_CO_HIGH] = result + 1;
                    }
                }

                mIsReceive[index] = true;
            }
            break;

            case COMMAND_SEND_DATA: {
                if( CURRENT_DATA_SIZE != length ) {
                    Log.e(TAG, "Current Recevice Data Length : " + length + " != " + CURRENT_DATA_SIZE);
                    break;
                }

                long current = System.currentTimeMillis();
                int result = 0;

                String mac = parseMacAddress(data);
                if( getIndexMacAddress(mac) == 1 ) {
                    if( current < mLastTime + 1000 ) {
                        Log.e(TAG, "Data period is short. LastTime = " +  getTime());
                        break;
                    }

                    mLastTime = current;

                    result = data[POS_CURRENT_DATA_O2] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_O2 + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_O2] = result;

                    result = data[POS_CURRENT_DATA_CH4] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_CH4 + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_CH4] = result;

                    result = data[POS_CURRENT_DATA_H2S] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_H2S + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_H2S] = result;

                    result = data[POS_CURRENT_DATA_CO] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_CO + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_CO] = result;
                }
                else {
                    if( current < mLastTime2 + 1000 ) {
                        Log.e(TAG, "Data period is short. LastTime = " +  getTime());
                        break;
                    }

                    mLastTime2 = current;

                    result = data[POS_CURRENT_DATA_O2] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_O2 + 1] & 0xff) << 8;
                    mDataCurrent2[POS_CURRENT_DATA_RESULT_O2] = result;

                    result = data[POS_CURRENT_DATA_CH4] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_CH4 + 1] & 0xff) << 8;
                    mDataCurrent2[POS_CURRENT_DATA_RESULT_CH4] = result;

                    result = data[POS_CURRENT_DATA_H2S] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_H2S + 1] & 0xff) << 8;
                    mDataCurrent2[POS_CURRENT_DATA_RESULT_H2S] = result;

                    result = data[POS_CURRENT_DATA_CO] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_CO + 1] & 0xff) << 8;
                    mDataCurrent2[POS_CURRENT_DATA_RESULT_CO] = result;
                }

            }
            break;

            case COMMAND_ACK: {
                Log.i(TAG, "COMMAND_ACK");
            }
            break;

            case COMMAND_NACK: {
                Log.e(TAG, "COMMAND_NACK");
            }
            break;

            default: {
                Log.e(TAG, "Unknown Command:" + String.valueOf(data[POS_COMMAND]) + ",Length:" + String.valueOf(length));
                btValue = COMMAND_UNKNOWN;
            }
            break;
        }

        return btValue;
    }

    private String parseMacAddress(byte[] data) {
        StringBuilder builder = new StringBuilder();
        int value;

        for(int i = POS_INIT_DATA_START-2; i >= POS_INIT_DATA_MAC; i--) {
            value = data[i] & 0xFF;
            builder.append(Integer.toString((value & 0xF0) >> 4, 16));
            builder.append(Integer.toString((value & 0x0F), 16));

            if( i != POS_INIT_DATA_MAC ) {
                builder.append(":");
            }
        }

        return builder.toString();
    }

    private int getIndexMacAddress(String mac) {
        if(mMacAddress.isEmpty()) {
            mMacAddress = mac;
            return 1;
        }

        if(mMacAddress.equals(mac)) {
            return 1;
        }

        if(mMacAddress2.isEmpty()) {
            mMacAddress2 = mac;
            return 2;
        }

        if(mMacAddress2.equals(mac)) {
            return 2;
        }

        mMacAddress = mac;
        return 1;
    }
}
