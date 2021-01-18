package com.bumyeong.bgx13p.gfinder;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GFinderComm {
    private final static String TAG = "bgx_dbg"; //DeviceList.class.getSimpleName();

    public enum GFINDER_COMM_STATUS {
        NONE,
        CONNECTION,
        NAME,
        INIT_DATA,
        DISCONNECT,
        CURRENT
    }

    public final static byte COMMAND_CONNECTION_OK = (byte)0x01;
    public final static byte COMMAND_GF_MAC_NAME = (byte)0x03;
    public final static byte COMMAND_SEND_DATA = (byte)0x05;
    public final static byte COMMAND_GF_DISCONNECT = (byte)0x07;
    public final static byte COMMAND_ACK = (byte)0xfc;
    public final static byte COMMAND_NACK = (byte)0xff;
    public final static byte COMMAND_UNKNOWN = (byte)0xfe;

    private final static int MAX_PACKET_SIZE = 255;
    private final static int POS_COMMAND = 0;
    private final static int POS_LENGTH = 1;
    private final static int POS_DATA_START = 2;

    private final static int MAX_INIT_DATA_COUNT = 8;

    private final static int POS_INIT_DATA_O2_LOW = 3;
    private final static int POS_INIT_DATA_O2_HIGH = 5;
    private final static int POS_INIT_DATA_CH4_LOW = 7;
    private final static int POS_INIT_DATA_CH4_HIGH = 9;
    private final static int POS_INIT_DATA_H2S_LOW = 3;
    private final static int POS_INIT_DATA_H2S_HIGH = 5;
    private final static int POS_INIT_DATA_CO_LOW = 7;
    private final static int POS_INIT_DATA_CO_HIGH = 9;

    private final static int MAX_INIT_DATA_RESULT_COUNT= 8;
    private final static int POS_INIT_DATA_RESULT_O2_LOW = 0;
    private final static int POS_INIT_DATA_RESULT_O2_HIGH = 1;
    private final static int POS_INIT_DATA_RESULT_CH4_LOW = 2;
    private final static int POS_INIT_DATA_RESULT_CH4_HIGH = 3;
    private final static int POS_INIT_DATA_RESULT_H2S_LOW = 4;
    private final static int POS_INIT_DATA_RESULT_H2S_HIGH = 5;
    private final static int POS_INIT_DATA_RESULT_CO_LOW = 6;
    private final static int POS_INIT_DATA_RESULT_CO_HIGH = 7;

    private final static int POS_CURRENT_DATA_O2 = 3;
    private final static int POS_CURRENT_DATA_CH4 = 5;
    private final static int POS_CURRENT_DATA_H2S = 7;
    private final static int POS_CURRENT_DATA_CO = 9;

    private final static int MAX_CURRENT_DATA_RESULT_COUNT= 4;
    private final static int POS_CURRENT_DATA_RESULT_O2 = 0;
    private final static int POS_CURRENT_DATA_RESULT_CH4 = 1;
    private final static int POS_CURRENT_DATA_RESULT_H2S = 2;
    private final static int POS_CURRENT_DATA_RESULT_CO = 3;

    private byte[] mPacket;
    private int mPacketSize = 2;

    private String mMacAddress;
    private String mDeviceName;
    private boolean[] mIsReceive;

    private GFINDER_COMM_STATUS mCommStatus = GFINDER_COMM_STATUS.NONE;
    private String[] mInitDataResult;
    private String[] mDataCurrent;
    private long mLastTime;

    public GFinderComm() {
        mPacket = new byte [MAX_PACKET_SIZE];
        mIsReceive = new boolean[MAX_INIT_DATA_COUNT];
        mInitDataResult = new String [MAX_INIT_DATA_RESULT_COUNT];
        mDataCurrent = new String [MAX_CURRENT_DATA_RESULT_COUNT];

        mInitDataResult[POS_INIT_DATA_RESULT_O2_LOW] = mInitDataResult[POS_INIT_DATA_RESULT_O2_HIGH] = "00.0";
        mInitDataResult[POS_INIT_DATA_RESULT_CH4_LOW] = mInitDataResult[POS_INIT_DATA_RESULT_CH4_HIGH] = "0";
        mInitDataResult[POS_INIT_DATA_RESULT_H2S_LOW] = mInitDataResult[POS_INIT_DATA_RESULT_H2S_HIGH] = "0.0";
        mInitDataResult[POS_INIT_DATA_RESULT_CO_LOW] = mInitDataResult[POS_INIT_DATA_RESULT_CO_HIGH] = "0";

        mDataCurrent[POS_CURRENT_DATA_RESULT_O2] = "00.0";
        mDataCurrent[POS_CURRENT_DATA_RESULT_CH4] = "0";
        mDataCurrent[POS_CURRENT_DATA_RESULT_H2S] = "0.0";
        mDataCurrent[POS_CURRENT_DATA_RESULT_CO] = "0";

        resetReceiveFlag();

        mLastTime = System.currentTimeMillis();
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

        mCommStatus = GFINDER_COMM_STATUS.CONNECTION;
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

        mCommStatus = GFINDER_COMM_STATUS.INIT_DATA;
        mPacketSize = 5;
        return mPacket;
    }

    public byte[] getPacketDisconnect() {
        Arrays.fill(mPacket, (byte)0);
        mPacket[POS_COMMAND] = COMMAND_GF_DISCONNECT;

        mCommStatus = GFINDER_COMM_STATUS.CURRENT;
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

    public String getDeviceName() { return mDeviceName; }

    public int getPacketSize() { return mPacketSize; }

    public GFINDER_COMM_STATUS getCommStatus() { return mCommStatus; }

    public String getInitDataO2Low() { return mInitDataResult[POS_INIT_DATA_RESULT_O2_LOW]; }
    public String getInitDataO2High() { return mInitDataResult[POS_INIT_DATA_RESULT_O2_HIGH]; }
    public String getInitDataCH4Low() { return mInitDataResult[POS_INIT_DATA_RESULT_CH4_LOW]; }
    public String getInitDataCH4High() { return mInitDataResult[POS_INIT_DATA_RESULT_CH4_HIGH]; }
    public String getInitDataH2SLow() { return mInitDataResult[POS_INIT_DATA_RESULT_H2S_LOW]; }
    public String getInitDataH2SHigh() { return mInitDataResult[POS_INIT_DATA_RESULT_H2S_HIGH]; }
    public String getInitDataCoLow() { return mInitDataResult[POS_INIT_DATA_RESULT_CO_LOW]; }
    public String getInitDataCoHigh() { return mInitDataResult[POS_INIT_DATA_RESULT_CO_HIGH]; }
    public String getTime() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(mLastTime));
    }
    public String getCurrentDataO2() { return mDataCurrent[POS_CURRENT_DATA_RESULT_O2]; }
    public String getCurrentDataCH4() { return mDataCurrent[POS_CURRENT_DATA_RESULT_CH4]; }
    public String getCurrentDataH2S() { return mDataCurrent[POS_CURRENT_DATA_RESULT_H2S]; }
    public String getCurrentDataCo() { return mDataCurrent[POS_CURRENT_DATA_RESULT_CO]; }

    public byte parse(byte[] data) {
        int length = data[POS_LENGTH];
        byte btValue = data[POS_COMMAND];

        switch( btValue) {
            case COMMAND_GF_MAC_NAME: {
                byte[] bytestring = new byte [length];
                System.arraycopy(data, 2, bytestring, 0, length);
                String receiveString = new String(bytestring);
                Log.d(TAG, "COMMAND_GF_MAC_NAME:" + receiveString);

                mMacAddress = receiveString.substring(0, 16);
                mDeviceName = receiveString.substring(16);
                mCommStatus = GFINDER_COMM_STATUS.NAME;
            }
            break;

            case COMMAND_SEND_DATA: {
                if( mCommStatus == GFINDER_COMM_STATUS.INIT_DATA ) {
                    int index = (int)data[POS_DATA_START];
                    int result = 0;

                    if( index == 2 ) {
                        result = data[POS_INIT_DATA_O2_LOW] & 0xff;
                        result += (int)(data[POS_INIT_DATA_O2_LOW + 1] & 0xff) << 8;
                        result -= 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_O2_LOW] = String.format("%.1f", (float)result / 10);

                        result = data[POS_INIT_DATA_O2_HIGH] & 0xff;
                        result += (int)(data[POS_INIT_DATA_O2_HIGH + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_O2_HIGH] = String.format("%.1f", (float)result / 10);

                        result = data[POS_INIT_DATA_CH4_LOW] & 0xff;
                        result += (data[POS_INIT_DATA_CH4_LOW + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_CH4_LOW] = String.valueOf(result);

                        result = data[POS_INIT_DATA_CH4_HIGH] & 0xff;
                        result +=(data[POS_INIT_DATA_CH4_HIGH + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_CH4_HIGH] = String.valueOf(result);
                    }
                    else if( index == 3 ) {
                        result = data[POS_INIT_DATA_H2S_LOW] & 0xff;
                        result +=(data[POS_INIT_DATA_H2S_LOW + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_H2S_LOW] = String.format("%.1f", (float)result / 10);

                        result = data[POS_INIT_DATA_H2S_HIGH] & 0xff;
                        result += (data[POS_INIT_DATA_H2S_HIGH + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_H2S_HIGH] = String.format("%.1f", (float)result / 10);

                        result = data[POS_INIT_DATA_CO_LOW] & 0xff;
                        result += (data[POS_INIT_DATA_CO_LOW + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_CO_LOW] = String.valueOf(result);

                        result = data[POS_INIT_DATA_CO_HIGH] & 0xff;
                        result += (data[POS_INIT_DATA_CO_HIGH + 1] & 0xff) << 8;
                        result += 1;
                        mInitDataResult[POS_INIT_DATA_RESULT_CO_HIGH] = String.valueOf(result);
                    }

                    mIsReceive[index] = true;

                    if( isReceiveFlag() == true ) {
                        mCommStatus = GFINDER_COMM_STATUS.DISCONNECT;
                    }
                }
                else if( mCommStatus == GFINDER_COMM_STATUS.CURRENT ) {
                    long current = System.currentTimeMillis();

                    if( current < mLastTime + 1000 ) {
                        Log.e(TAG, "Data period is short. LastTime = " +  getTime());
                        break;
                    }

                    int result = 0;
                    mLastTime = current;

                    result = data[POS_CURRENT_DATA_O2] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_O2 + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_O2] = String.format("%.1f", (float)result / 10);

                    result = data[POS_CURRENT_DATA_CH4] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_CH4 + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_CH4] = String.valueOf(result);;

                    result = data[POS_CURRENT_DATA_H2S] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_H2S + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_H2S] = String.valueOf(result);;

                    result = data[POS_CURRENT_DATA_CO] & 0xff;
                    result += (int)(data[POS_CURRENT_DATA_CO + 1] & 0xff) << 8;
                    mDataCurrent[POS_CURRENT_DATA_RESULT_CO] = String.format("%.1f", (float)result / 10);
                }
                else {
                    Log.e(TAG, "Unknown COMMAND_SEND_DATA,data length:" + String.valueOf(length) );
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
}
