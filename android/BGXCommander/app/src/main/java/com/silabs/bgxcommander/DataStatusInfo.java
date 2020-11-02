package com.bumyeong.rfhook;

import android.app.DownloadManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataStatusInfo {
    public enum DATA_COMM_STATUS {
        WAIT,
        SEND_START,
        SENDING,
        COMPLETE,
        RECEIVE_START,
        RECEIVING
    }

    public enum DATA_COMM_TYPE {
        SENDER,
        RECEIVER
    }

    private SimpleDateFormat mSDF = new SimpleDateFormat("yyyy년MM월dd일 HH시mm분");

    private DATA_COMM_TYPE type;
    private byte ID;
    private byte Command;
    private byte SenderID;
    private String Time;
    private DATA_COMM_STATUS Reqeust;
    private DATA_COMM_STATUS Response;

    public DataStatusInfo(DATA_COMM_TYPE create_type, byte btDeviceNumber) {
        type = create_type;
        ID = btDeviceNumber;
        Command = 0;
        SenderID = btDeviceNumber;
        Time = "";
        Reqeust = DATA_COMM_STATUS.WAIT;
        Response = DATA_COMM_STATUS.WAIT;
    }

    public byte getCommand() {
        return Command;
    }

    public void setCommand(byte command) {
        Command = command;
    }

    public void setCurrentTime() {
        Time = mSDF.format(Calendar.getInstance().getTime());
    }

    public byte getSenderID() {
        return SenderID;
    }

    public void setSenderID(byte senderID) {
        SenderID = senderID;
    }

    public DATA_COMM_STATUS getReqeust() {
        return Reqeust;
    }

    public void setReqeust(DATA_COMM_STATUS reqeust) {
        Reqeust = reqeust;
    }

    public DATA_COMM_STATUS getResponse() {
        return Response;
    }

    public void setResponse(DATA_COMM_STATUS response) {
        Response = response;
    }

    public String getResult() {
        String result;

        if( type == DATA_COMM_TYPE.SENDER ) {
            result = String.format("송신데이터 : %d, %d\r\n데이터 송신시간 : %s\r\n데이터 송신상태 : ", ID, Command,Time);
            result += getStatusToString(Reqeust);
        }
        else {
            result = String.format("수신데이터 : %d, %d\r\n데이터 수신시간 : %s\r\n데이터 수신상태 : ", SenderID, Command,Time);
            result += getStatusToString(Reqeust);
        }

        result += "데이터 응답 : " + getStatusToString(Response);
        result += "\r\n\r\n";

        return result;
    }

    public String getStatusToString(DATA_COMM_STATUS status) {
        switch( status ) {
            case WAIT:
                return "대기\r\n";

            case SEND_START:
                return "송신시작\r\n";

            case SENDING:
                return "송신중\r\n";

            case COMPLETE:
                return "완료\r\n";

            case RECEIVE_START:
                return "수신시작\r\n";

            case RECEIVING:
                return "수신중\r\n";
        }

        return "";
    }
}
