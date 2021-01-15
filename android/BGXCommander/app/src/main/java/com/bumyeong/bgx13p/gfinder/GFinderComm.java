package com.bumyeong.bgx13p.gfinder;

import android.util.Log;

import java.util.Arrays;

public class GFinderComm {
    private final static String TAG = "bgx_dbg"; //DeviceList.class.getSimpleName();

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

    private byte[] mPacket;
    private int mPacketSize = 2;

    private String mMacAddress;
    private String mDeviceName;

    public GFinderComm() {
        mPacket = new byte [MAX_PACKET_SIZE];
    }

    public byte[] getPacketConnectionOk() {
        Arrays.fill(mPacket, (byte)0);
        mPacket[POS_COMMAND] = COMMAND_CONNECTION_OK;

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

    public String getDeviceName() { return mDeviceName; }

    public int getPacketSize() { return mPacketSize; }

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
            }
            break;

            case COMMAND_SEND_DATA: {

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
