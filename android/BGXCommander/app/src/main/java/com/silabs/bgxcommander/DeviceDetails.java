/*
 * Copyright 2018-2019 Silicon Labs
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * {{ http://www.apache.org/licenses/LICENSE-2.0}}
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bumyeong.btlinkap;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.silabs.bgxpress.BGX_CONNECTION_STATUS;
import com.silabs.bgxpress.BGXpressService;
import com.silabs.bgxpress.BusMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.bumyeong.btlinkap.PasswordKind.BusModePasswordKind;
import static com.bumyeong.btlinkap.TextSource.LOCAL;
import static com.bumyeong.btlinkap.TextSource.REMOTE;


public class DeviceDetails extends AppCompatActivity {
    private final static String TAG = "bgx_dbg"; //DeviceDetails.class.getSimpleName();
    private final static byte DEVICE_NUMBER = 2;
    final static Integer kBootloaderSecurityVersion = 1229;

    public enum COMMAND_STATUS {
        NONE,
        REQUEST,
        RESPONSE,
        ACK,
        NAK,
        PUSH
    }

    class RepeatSendTimer extends TimerTask {
        @Override
        public void run() {
            DeviceDetails.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendRequest();
                    }
                    catch (Exception ex) {
                            ex.printStackTrace();
                    }
                }
            });
        }
    }

    // public BluetoothDevice mBluetoothDevice;
    public String mDeviceAddress;
    public String mDeviceName;
    public Handler mHandler;

    public int mDeviceConnectionState;

    private BroadcastReceiver mConnectionBroadcastReceiver;
    private BroadcastReceiver mBondReceiver;
    public final Context mContext = this;

    // UI Elements
    private EditText mStreamEditText;
    private RadioButton mStreamRB;
    private RadioButton mCommandRB;
    private Button mGetButton;

    private int mBusMode;

    private TextSource mTextSource = TextSource.UNKNOWN;
    private final int kAutoScrollMessage = 0x5C011;
    private final int kAutoScrollDelay = 800; // the time in ms between adding text and autoscroll.

    private MenuItem mIconItem;
    private MenuItem mUpdateItem;

    private BGXpressService.BGXPartID mBGXPartID;
    private String mBGXDeviceID;
    private String mBGXPartIdentifier;

    private byte mDeviceNumber;

    final static int COMMAND_POS_CMD = 3;
    final static int COMMAND_POS_SRCID = 4;
    final static int COMMAND_POS_CS = 7;

    private byte[] mCommandRequestValue = { (byte) 0x10, (byte) 0x02, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x03, (byte) 0x03};
    private byte[] mCommandReponseValue = { (byte) 0x10, (byte) 0x02, (byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x10, (byte) 0x03, (byte) 0x05};
    private byte[] mCommandAck          = { (byte) 0x10, (byte) 0x02, (byte) 0x01, (byte) 0x05, (byte) 0x10, (byte) 0x03, (byte) 0x06};
    private byte[] mCommandNck          = { (byte) 0x10, (byte) 0x02, (byte) 0x01, (byte) 0x07, (byte) 0x10, (byte) 0x03, (byte) 0x08};

    private COMMAND_STATUS mCommandStatus = COMMAND_STATUS.RESPONSE;
    private COMMAND_STATUS mLastCommand = COMMAND_STATUS.RESPONSE;
    private DataStatusInfo mSendInfo;
    private DataStatusInfo mReceviceInfo;
    private boolean mIsAutoSend = false;
    private boolean mIsStartTimer = false;
    private Timer mRepeaterTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        mRepeaterTimer = new Timer();
        mBusMode = BusMode.UNKNOWN_MODE;

        mStreamEditText = (EditText) findViewById(R.id.streamEditText);
//        mMessageEditText = (EditText) findViewById(R.id.msgEditText);
        mStreamRB = (RadioButton) findViewById(R.id.streamRB);
        mCommandRB = (RadioButton) findViewById(R.id.commandRB);
        mGetButton = (Button) findViewById(R.id.getButton);

        final IntentFilter bgxpressServiceFilter = new IntentFilter(BGXpressService.BGX_CONNECTION_STATUS_CHANGE);
        bgxpressServiceFilter.addAction(BGXpressService.BGX_MODE_STATE_CHANGE);
        bgxpressServiceFilter.addAction(BGXpressService.BGX_DATA_RECEIVED);
        bgxpressServiceFilter.addAction(BGXpressService.BGX_DEVICE_INFO);
        bgxpressServiceFilter.addAction(BGXpressService.DMS_VERSIONS_AVAILABLE);
        bgxpressServiceFilter.addAction(BGXpressService.BUS_MODE_ERROR_PASSWORD_REQUIRED);
        bgxpressServiceFilter.addAction(BGXpressService.BGX_INVALID_GATT_HANDLES);

        mConnectionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            String intentDeviceAddress = intent.getStringExtra("DeviceAddress");
            if (intentDeviceAddress != null
                    && intentDeviceAddress.length() > 1
                    && intentDeviceAddress.equalsIgnoreCase(mDeviceAddress) == false) {
                Log.d(TAG, "return - intentDeviceAddress != null && intentDeviceAddress.length() > 1 intentDeviceAddress.equalsIgnoreCase(mDeviceAddress) == false");
                return;
            }

            switch (intent.getAction()) {
                //-----------------------------------------------------------------------------
                case BGXpressService.BGX_INVALID_GATT_HANDLES: {
                //-----------------------------------------------------------------------------
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Invalid GATT Handles");
                    builder.setMessage("The bonding information on this device is invalid (probably due to a firmware update). You should select " + mDeviceName + " in the Bluetooth Settings and choose \"Forget\".");
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dlg = builder.create();
                    dlg.show();
                }
                break;

                //-----------------------------------------------------------------------------
                case BGXpressService.DMS_VERSIONS_AVAILABLE: {
                //-----------------------------------------------------------------------------
                    Integer bootloaderVersion = BGXpressService.getBGXBootloaderVersion(mDeviceAddress);
                    if (bootloaderVersion >= kBootloaderSecurityVersion) {
                        Log.d(TAG, "BGXpressService.DMS_VERSIONS_AVAILABLE");

                        String versionJSON = intent.getStringExtra("versions-available-json");
                        try {
                            JSONArray myDMSVersions = new JSONArray(versionJSON);

                            Log.d(TAG, "Device Address: " + mDeviceAddress);
                            Version vFirmwareRevision = new Version(BGXpressService.getFirmwareRevision(mDeviceAddress));

                            for (int i = 0; i < myDMSVersions.length(); ++i) {
                                JSONObject rec = (JSONObject) myDMSVersions.get(i);
                                String sversion = (String) rec.get("version");
                                Version iversion = new Version(sversion);

                                if (iversion.compareTo(vFirmwareRevision) > 0) {
                                    // newer version available.
                                    mIconItem.setIcon(ContextCompat.getDrawable(mContext, R.drawable.update_decoration));
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

                //-----------------------------------------------------------------------------
                case BGXpressService.BGX_CONNECTION_STATUS_CHANGE: {
                //-----------------------------------------------------------------------------
                    Log.d(TAG, "BGXpressService.BGX_CONNECTION_STATUS_CHANGE");

                    BGX_CONNECTION_STATUS connectionState = (BGX_CONNECTION_STATUS) intent.getSerializableExtra("bgx-connection-status");
                    switch (connectionState) {
                        case CONNECTED:
                            Log.d(TAG, "DeviceDetails - connection state changed to CONNECTED");
                            break;
                        case CONNECTING:
                            Log.d(TAG, "DeviceDetails - connection state changed to CONNECTING");
                            break;
                        case DISCONNECTING:
                            Log.d(TAG, "DeviceDetails - connection state changed to DISCONNECTING");
                            break;
                        case DISCONNECTED:
                            Log.d(TAG, "DeviceDetails - connection state changed to DISCONNECTED");
                            finish();
                            break;
                        case INTERROGATING:
                            Log.d(TAG, "DeviceDetails - connection state changed to INTERROGATING");
                            break;
                        default:
                            Log.d(TAG, "DeviceDetails - connection state changed to Unknown connection state.");
                            break;
                    }
                }
                break;

                //-----------------------------------------------------------------------------
                case BGXpressService.BGX_MODE_STATE_CHANGE: {
                //-----------------------------------------------------------------------------
                    Log.d(TAG, "BGXpressService.BGX_MODE_STATE_CHANGE");
                    setBusMode(intent.getIntExtra("busmode", BusMode.UNKNOWN_MODE));
                }
                break;

                //-----------------------------------------------------------------------------
                case BGXpressService.BGX_DATA_RECEIVED: {
                //-----------------------------------------------------------------------------
                    Log.d(TAG, "-----------------------------------------");
                    Log.d(TAG, "BGXpressService.BGX_DATA_RECEIVED - START");
                    Log.d(TAG, "-----------------------------------------");
                    String stringReceived = intent.getStringExtra("data");

//                    Log.d(TAG, "- string.length : " + String.valueOf(stringReceived.length()));
//                    Log.d(TAG, "- string : " + stringReceived);

                    byte[] receive = stringReceived.getBytes();
                    Log.d(TAG, "   byte.length : " + String.valueOf(receive.length) + " -> " + bytesToHex(receive));

                    switch (mCommandStatus)
                    {
                        case REQUEST: {
                            if( Arrays.equals(mCommandAck, receive) == true ) {
                                mSendInfo.setReqeust(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);
                                mSendInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.RECEIVE_START);
                            }
                            else if( Arrays.equals(mCommandNck, receive) == true ) {
                                Log.e(TAG, "- receive NAK. retry " + bytesToHex(receive));
                                sendRetry();
                            }
                            else if ( compareResponse(receive) == true ) {
                                mSendInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);

                                mReceviceInfo.setReqeust(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);
                                mReceviceInfo.setCurrentTime();
                                mReceviceInfo.setCommand(receive[COMMAND_POS_CMD]);
                                mReceviceInfo.setSenderID(receive[COMMAND_POS_SRCID]);
                                mReceviceInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);

//                                new Thread(new Runnable() {
//                                    @Override public void run() {
//                                        try {
//                                            Thread.sleep(1000);
                                            sendACK();
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                } }).start();

                                mCommandStatus = COMMAND_STATUS.RESPONSE;
                            }
                            else {
                                Log.e(TAG, "Unknown Packet : " + bytesToHex(receive));
                            }
                        }
                        break;

                        case RESPONSE: {
                            if( Arrays.equals(mCommandAck, receive) == true ) {
                                mReceviceInfo.setReqeust(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);
                                mReceviceInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);
                            }
                            else if( Arrays.equals(mCommandNck, receive) == true ) {
                                Log.e(TAG, "- receive NAK. retry " + bytesToHex(receive));
                                sendRetry();
                            }
                            else if ( compareRequest(receive) == true ) {
                                mReceviceInfo.setReqeust(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);
                                mReceviceInfo.setCurrentTime();
                                mReceviceInfo.setCommand(receive[COMMAND_POS_CMD]);
                                mReceviceInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.COMPLETE);

//                                new Thread(new Runnable() {
//                                    @Override public void run() {
//                                        try {
//                                            Thread.sleep(1000);
                                            sendACK();
//                                            Thread.sleep(2000);
                                            sendResponse();
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                    } }).start();
                            }
                            else {
                                Log.e(TAG, "Unknown Packet : " + bytesToHex(receive));
                            }

                            Log.d(TAG, "COMMAND_STATUS.RESPONSE - CASE / END");
                        }
                        break;
                    }

                    processText();

                    Log.d(TAG, "-----------------------------------------");
                    Log.d(TAG, "BGXpressService.BGX_DATA_RECEIVED - END");
                    Log.d(TAG, "-----------------------------------------");
                }
                break;

                //-----------------------------------------------------------------------------
                case BGXpressService.BGX_DEVICE_INFO: {
                //-----------------------------------------------------------------------------
                    Log.d(TAG, "BGXpressService.BGX_DEVICE_INFO");
                    Integer bootloaderVersion = BGXpressService.getBGXBootloaderVersion(mDeviceAddress);

                    mBGXDeviceID = intent.getStringExtra("bgx-device-uuid");
                    mBGXPartID = (BGXpressService.BGXPartID) intent.getSerializableExtra("bgx-part-id");
                    mBGXPartIdentifier = intent.getStringExtra("bgx-part-identifier");

                    if (bootloaderVersion >= kBootloaderSecurityVersion) {
                        // request DMS VERSIONS at this point because now we know the part id.
                        Intent intent2 = new Intent(BGXpressService.ACTION_DMS_GET_VERSIONS);
                        String platformID = BGXpressService.getPlatformIdentifier(mDeviceAddress);

                        intent2.setClass(mContext, BGXpressService.class);

                        intent2.putExtra("bgx-part-id", mBGXPartID);
                        intent2.putExtra("DeviceAddress", mDeviceAddress);
                        intent2.putExtra("bgx-part-identifier", mBGXPartIdentifier);
                        if (null != platformID) {
                            intent2.putExtra("bgx-platform-identifier", platformID);
                        }

                        startService(intent2);
                    } else if (bootloaderVersion > 0) {
                        Log.d(TAG, "bootloaderVersion{" + bootloaderVersion.toString() + "} < kBootloaderSecurityVersion");
                        mIconItem.setIcon(ContextCompat.getDrawable(mContext, R.drawable.security_decoration));
                    }
                }
                break;

                //-----------------------------------------------------------------------------
                case BGXpressService.BUS_MODE_ERROR_PASSWORD_REQUIRED: {
                //-----------------------------------------------------------------------------
                    Log.d(TAG, "BGXpressService.BUS_MODE_ERROR_PASSWORD_REQUIRED");

                    setBusMode(BusMode.STREAM_MODE);

                    Intent passwordIntent = new Intent(context, Password.class);
                    passwordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    passwordIntent.putExtra("DeviceAddress", mDeviceAddress);
                    passwordIntent.putExtra("PasswordKind", BusModePasswordKind);
                    passwordIntent.putExtra("DeviceName", mDeviceName);

                    context.startActivity(passwordIntent);
                }
                break;
            }
            }
        };

        registerReceiver(mConnectionBroadcastReceiver, bgxpressServiceFilter);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.d(TAG, "Handle message.");
                return false;
            }
        });

        mStreamRB.setEnabled(false);
        mStreamRB.setVisibility(View.INVISIBLE);
        mStreamRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (mBusMode != BusMode.STREAM_MODE) {
                    sendBusMode(BusMode.STREAM_MODE);
                    setBusMode(BusMode.STREAM_MODE);
                }
            }
            }
        });

        mCommandRB.setEnabled(false);
        mCommandRB.setVisibility(View.INVISIBLE);
        mCommandRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (mBusMode != BusMode.REMOTE_COMMAND_MODE && mBusMode != BusMode.LOCAL_COMMAND_MODE) {
                    sendBusMode(BusMode.REMOTE_COMMAND_MODE);
                    setBusMode(BusMode.REMOTE_COMMAND_MODE);
                }
            }
            }
        });

        mGetButton.setEnabled(true);
        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mIsAutoSend == true ) {
                    Spinner spInterval = (Spinner)findViewById(R.id.sprTimerInterval);
                    Switch sw1 = (Switch) findViewById(R.id.swAutoSend);

                    if( mIsStartTimer == true ) {
                        mRepeaterTimer.cancel();

                        mIsStartTimer = false;
                        mGetButton.setText("보내기");
                        spInterval.setEnabled(true);
                        sw1.setEnabled(true);
                    }
                    else {
                        int interval = Integer.parseInt(String.valueOf(spInterval.getSelectedItem())) * 1000;
                        Log.d(TAG, "Selected interval : " + interval + " (ms)");

                        mGetButton.setText("멈추기");
                        mIsStartTimer = true;
                        spInterval.setEnabled(false);
                        sw1.setEnabled(false);

                        sendRequest();

                        mRepeaterTimer.schedule(new RepeatSendTimer(), interval, interval);
                    }
                }
                else {
                    sendRequest();
                }
            }
        });

        final ImageButton clearButton = (ImageButton) findViewById(R.id.clearImageButton);
        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clear");
                mStreamEditText.setText("");
            }
        });

        Switch sw = (Switch) findViewById(R.id.swAutoSend);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mIsAutoSend = true;
                } else {
                    mIsAutoSend = false;
                }
            }
        });

        mDeviceNumber = DEVICE_NUMBER;

        mSendInfo = new DataStatusInfo(DataStatusInfo.DATA_COMM_TYPE.SENDER, mDeviceNumber);
        mReceviceInfo = new DataStatusInfo(DataStatusInfo.DATA_COMM_TYPE.RECEIVER, mDeviceNumber);

        mCommandRequestValue[COMMAND_POS_SRCID] = mDeviceNumber;
        mCommandRequestValue[COMMAND_POS_CS] += mDeviceNumber;

        mCommandReponseValue[COMMAND_POS_SRCID] = mDeviceNumber;
        mCommandReponseValue[COMMAND_POS_CS] += mDeviceNumber;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDeviceName = getIntent().getStringExtra("DeviceName");
        mDeviceAddress = getIntent().getStringExtra("DeviceAddress");

        androidx.appcompat.app.ActionBar ab = getSupportActionBar();
        if (null != ab) {
            ab.setTitle(mDeviceName);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BGXpressService.ACTION_READ_BUS_MODE);
                intent.setClass(mContext, BGXpressService.class);
                intent.putExtra("DeviceAddress", mDeviceAddress);
                startService(intent);
            }
        });

        BGXpressService.getBGXDeviceInfo(this, mDeviceAddress);
    }

    @Override
    protected void onDestroy() {
        mRepeaterTimer.cancel();

        Log.d(TAG, "Unregistering the connectionBroadcastReceiver");
        unregisterReceiver(mConnectionBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.devicedetails, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        return super.onOptionsItemSelected(mi);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed.");
        disconnect();

        finish();

        super.onBackPressed();
    }

    void disconnect() {
        Intent intent = new Intent(BGXpressService.ACTION_BGX_DISCONNECT);
        intent.setClass(mContext, BGXpressService.class);
        intent.putExtra("DeviceAddress", mDeviceAddress);
        startService(intent);
    }


    public void setBusMode(int busMode) {
        Log.d(TAG, "sendBusMode() - busMode:" + busMode);
        if (mBusMode != busMode) {
            mBusMode = busMode;
        }
    }

    public int getBusMode() {
        return mBusMode;
    }

    public void sendBusMode(int busMode) {
        Log.d(TAG, "sendBusMode() - busMode:" + busMode);

        Intent intent = new Intent(BGXpressService.ACTION_WRITE_BUS_MODE);
        intent.setClass(this, BGXpressService.class);
        intent.putExtra("busmode", busMode);
        intent.putExtra("DeviceAddress", mDeviceAddress);

        /* Here we need to check to see if a busmode password is set for this device.
         * If there is one, then we would need to add it to the intent.
         */

        AccountManager am = AccountManager.get(this);
        String password = Password.RetrievePassword(am, BusModePasswordKind, mDeviceAddress);

        if (null != password) {
            intent.putExtra("password", password);
        }

        startService(intent);
    }

    void processText() {
        String TEXTVIEW_TITLE = "Bluetooth 통신 상테 : 정상\r\nBT AP ID : " + String.valueOf(DEVICE_NUMBER) + "\r\n\r\n\r\n";
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        ssb.append(TEXTVIEW_TITLE);
        ssb.append(mSendInfo.getResult());
        ssb.append(mReceviceInfo.getResult());

        mStreamEditText.setText(ssb);
    }

    /*
     * The purpose of this function is to test ACTION_WRITE_SERIAL_BIN_DATA
     * by sending all the possible byte values.
     */
    void bytetest() {
        byte[] myByteArray = new byte[256];
        for (int i = 0; i < 256; ++i) {
            myByteArray[i] = (byte) i;
        }

        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_BIN_DATA);
        writeIntent.putExtra("value", myByteArray);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);
    }

    void sendRequest() {
        Log.d(TAG, "[PROTOCOL] sendRequest()");

        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_DATA);
        mCommandStatus = COMMAND_STATUS.REQUEST;

        mSendInfo.setCurrentTime();
        mSendInfo.setCommand((byte)1);
        mSendInfo.setReqeust(DataStatusInfo.DATA_COMM_STATUS.SENDING);
        mSendInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.WAIT);

        mReceviceInfo.setReqeust(DataStatusInfo.DATA_COMM_STATUS.WAIT);
        mReceviceInfo.setResponse(DataStatusInfo.DATA_COMM_STATUS.WAIT);

        processText();

        String msg2Send = new String(mCommandRequestValue);
        mLastCommand = COMMAND_STATUS.REQUEST;

        final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.btlinkap", MODE_PRIVATE);
        writeIntent.putExtra("value", msg2Send);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);

        Log.d(TAG, "[PROTOCOL] sendRequest() - END");
    }

    void sendACK() {
        Log.d(TAG, "[PROTOCOL] sendACK() - START");
        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_DATA);
        String msg2Send = new String(mCommandAck);
        mLastCommand = COMMAND_STATUS.ACK;

        final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.btlinkap", MODE_PRIVATE);
        writeIntent.putExtra("value", msg2Send);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);
        Log.d(TAG, "[PROTOCOL] sendACK() - END");
    }

    void sendNCK() {
        Log.d(TAG, "[PROTOCOL] sendNCK() - START");

        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_DATA);
        String msg2Send = new String(mCommandNck);
        mLastCommand = COMMAND_STATUS.NAK;

        final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.btlinkap", MODE_PRIVATE);
        writeIntent.putExtra("value", msg2Send);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);

        Log.d(TAG, "[PROTOCOL] sendNCK() - END");
    }

    void sendResponse() {
        Log.d(TAG, "[PROTOCOL] sendResponse() - START");

        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_DATA);
        String msg2Send = new String(mCommandReponseValue);
        mLastCommand = COMMAND_STATUS.RESPONSE;

        mSendInfo.setCurrentTime();
        mSendInfo.setCommand((byte)3);

        final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.btlinkap", MODE_PRIVATE);
        writeIntent.putExtra("value", msg2Send);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);

        Log.d(TAG, "[PROTOCOL] sendResponse() - END");
    }

    void sendRetry() {
        switch (mLastCommand) {
            case REQUEST: {
                sendRequest();
            }
            break;

            case RESPONSE: {
                sendResponse();
            }
            break;

            case ACK: {
                sendACK();
            }
            break;

            case NAK: {
                sendNCK();
            }
            break;
        }
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        char[] hexCharsAndSpace = new char[bytes.length * 3];

        int j = 0;
        for ( j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        int index = 0;
        for ( j = 0; j < hexChars.length; j++ ) {
            hexCharsAndSpace[index] = hexChars[j];
            index++;

            if( j % 2 == 1 )
            {
                hexCharsAndSpace[index] = ' ';
                index++;
            }
        }

        return new String(hexCharsAndSpace);
    }

    boolean compareResponse(byte[] receive) {
        boolean result = false;

        do {
            if( receive.length != mCommandReponseValue.length ) {
                Log.e(TAG, "Difference receive.length:" + String.valueOf(receive.length)
                        + ", Response.length:" + String.valueOf(mCommandReponseValue.length));
                Log.e(TAG, "Data : " + bytesToHex(receive));
                break;
            }

            // CheckSum

            result = true;
        } while (false);

        return result;
    }

    boolean compareRequest(byte[] receive) {
        boolean result = false;

        do {
            if( receive.length != mCommandRequestValue.length ) {
                Log.e(TAG, "Difference receive.length:" + String.valueOf(receive.length)
                        + ", Request.length:" + String.valueOf(mCommandRequestValue.length));
                Log.e(TAG, "Data : " + bytesToHex(receive));
                break;
            }

            // CheckSum

            result = true;
        } while (false);

        return result;
    }
}
