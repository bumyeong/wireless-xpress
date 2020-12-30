package com.bumyeong.batterystarter;

public class COMMAND_DEFINE {
    public static final byte COMMAND_REQUEST_PASSWORD = (byte)0x01;
    public static final byte COMMAND_RESPONSE_PASSWORD = (byte)0x11;

    public static final byte COMMAND_CHANGE_PASSWORD = (byte)0x02;
    public static final byte COMMAND_RESPONSE_CHANGE_PASSWORD = (byte)0x12;

    public static final int POS_COMMAND = 0;
    public static final int POS_SIZE = 1;
    public static final int POS_DATA_START = 2;
}
