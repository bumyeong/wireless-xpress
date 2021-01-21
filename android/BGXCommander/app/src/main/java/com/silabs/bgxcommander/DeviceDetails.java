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

package com.bumyeong.rfhook;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.silabs.bgxpress.BGX_CONNECTION_STATUS;
import com.silabs.bgxpress.BGXpressService;
import com.silabs.bgxpress.BusMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static com.bumyeong.rfhook.PasswordKind.BusModePasswordKind;

public class DeviceDetails extends AppCompatActivity {
    private final static String TAG = "bgx_dbg"; //DeviceDetails.class.getSimpleName();
    final static Integer kBootloaderSecurityVersion = 1229;

    public String mDeviceAddress;
    public String mDeviceName;
    public Handler mHandler;

    private BroadcastReceiver mConnectionBroadcastReceiver;
    private BroadcastReceiver mBondReceiver;
    public final Context mContext = this;

    // UI Elements
    private EditText mEditTextID;
    private RadioButton mRadioButtonSteady;
    private RadioButton mRadioButtonBlinking;
    private Button mButtonSend;

    private TextView mReceiveID;
    private TextView mPushSwitchState;
    private TextView mIrSensorState;
    private TextView mTouchSensorState;
    private TextView mBatteryState;

    private int mBusMode;

    private com.bumyeong.rfhook.TextSource mTextSource = com.bumyeong.rfhook.TextSource.UNKNOWN;
    private final int kAutoScrollMessage = 0x5C011;
    private final int kAutoScrollDelay = 800; // the time in ms between adding text and autoscroll.

    private MenuItem mIconItem;
    private MenuItem mUpdateItem;

    private BGXpressService.BGXPartID mBGXPartID;
    private String mBGXDeviceID;
    private String mBGXPartIdentifier;

    private byte mDeviceNumber;

    final static int COMMAND_FRAME_POS_LENGTH = 2;
    final static int COMMAND_FRAME_POS_ID_START = 4;
    final static int COMMAND_FRAME_POS_TYPE = 8;
    final static int COMMAND_FRAME_POS_STATUS = 9;
    final static int COMMAND_FRAME_POS_DEL_ETX = 10;
    final static int COMMAND_FRAME_POS_CS = 12;
    //                                      DLE        STX     LENGTH    COMMAND       ID#1       ID#2       ID#3       ID#4
    private byte[] mCommandFrame = { (byte)0x10,(byte)0x02,(byte)0x07,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01
    //                                     TYPE     STATUS        DLE        ETX         CS
                                   , (byte)0x00,(byte)0x00,(byte)0x10,(byte)0x03,(byte)0x00};
    private byte[] mCommandAck   = { (byte) 0x10, (byte) 0x02, (byte) 0x01, (byte) 0x05, (byte) 0x10, (byte) 0x03, (byte) 0x06};
    private byte[] mCommandNck   = { (byte) 0x10, (byte) 0x02, (byte) 0x01, (byte) 0x07, (byte) 0x10, (byte) 0x03, (byte) 0x08};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        mEditTextID = (EditText) findViewById(R.id.editTextID);
        mRadioButtonSteady = (RadioButton) findViewById(R.id.radioButtonSteady);
        mRadioButtonBlinking = (RadioButton) findViewById(R.id.radioButtonBlinking);
        mButtonSend = (Button) findViewById(R.id.buttonSend);

        mReceiveID = (TextView)findViewById(R.id.editTextReceiveID);
        mPushSwitchState = (TextView)findViewById(R.id.textviewPushSwitchState);
        mIrSensorState = (TextView)findViewById(R.id.textviewIrSensorState);
        mTouchSensorState = (TextView)findViewById(R.id.textviewTouchSensorState);
        mBatteryState = (TextView)findViewById(R.id.textviewBatteryState);

        mBusMode = BusMode.UNKNOWN_MODE;

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
                            com.bumyeong.rfhook.Version vFirmwareRevision = new com.bumyeong.rfhook.Version(BGXpressService.getFirmwareRevision(mDeviceAddress));

                            for (int i = 0; i < myDMSVersions.length(); ++i) {
                                JSONObject rec = (JSONObject) myDMSVersions.get(i);
                                String sversion = (String) rec.get("version");
                                com.bumyeong.rfhook.Version iversion = new com.bumyeong.rfhook.Version(sversion);

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
                            Log.d(TAG, "  DeviceDetails - connection state changed to CONNECTED");
                            break;
                        case CONNECTING:
                            Log.d(TAG, "  DeviceDetails - connection state changed to CONNECTING");
                            break;
                        case DISCONNECTING:
                            Log.d(TAG, "  DeviceDetails - connection state changed to DISCONNECTING");
                            break;
                        case DISCONNECTED:
                            Log.d(TAG, "  DeviceDetails - connection state changed to DISCONNECTED");
                            finish();
                            break;
                        case INTERROGATING:
                            Log.d(TAG, "  DeviceDetails - connection state changed to INTERROGATING");
                            break;
                        default:
                            Log.d(TAG, "  DeviceDetails - connection state changed to Unknown connection state.");
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
                    String stringReceived = intent.getStringExtra("data");

//                    Log.d(TAG, "- string.length : " + String.valueOf(stringReceived.length()));
//                    Log.d(TAG, "- string : " + stringReceived);

                    byte[] receive = stringReceived.getBytes();
                    Log.d(TAG, "   byte.length : " + String.valueOf(receive.length) + " -> " + bytesToHex(receive));

                    do {
                        if( Arrays.equals(mCommandAck, receive) == true ) {
                            Log.d(TAG, "   RECEIVE ACK");
                            ShowNotification("RECEIVE ACK", Toast.LENGTH_SHORT);
                            break;
                        }

                        if( Arrays.equals(mCommandNck, receive) == true ) {
                            Log.e(TAG, "   RECEIVE NCK");
                            ShowNotification("[ERR] RECEIVE NCK !!!", Toast.LENGTH_LONG);
                            break;
                        }

//                        if( mCommandFrame.length != receive.length ) {
//                            Log.e(TAG, "   UNKNOWN PACKET");
//                            ShowNotification("[ERR] UNKNOWN PACKET !!!", Toast.LENGTH_LONG);
//                            break;
//                        }

                        StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < 4; i++ ) {
                            sb.append(String.valueOf(receive[COMMAND_FRAME_POS_ID_START + i]));
                        }

                        String strID = sb.toString();
                        String strColorName = "";
                        int result = 0;

                        result = receive[COMMAND_FRAME_POS_STATUS] & 0x08;
                        if( result != 0 ) {
                            mPushSwitchState.setBackgroundResource(R.color.normal);
                        }
                        else {
                            mPushSwitchState.setBackgroundResource(R.color.abnormal);
                        }

                        result = receive[COMMAND_FRAME_POS_STATUS] & 0x04;
                        if( result != 0 ) {
                            mIrSensorState.setBackgroundResource(R.color.normal);
                        }
                        else {
                            mIrSensorState.setBackgroundResource(R.color.abnormal);
                        }

                        result = receive[COMMAND_FRAME_POS_STATUS] & 0x02;
                        if( result != 0 ) {
                            mTouchSensorState.setBackgroundResource(R.color.normal);
                        }
                        else {
                            mTouchSensorState.setBackgroundResource(R.color.abnormal);
                        }

                        result = receive[COMMAND_FRAME_POS_STATUS] & 0x01;
                        if( result != 0 ) {
                            mBatteryState.setBackgroundResource(R.color.normal);
                        }
                        else {
                            mBatteryState.setBackgroundResource(R.color.abnormal);
                        }

                        mReceiveID.setText(strID);
                        ShowNotification(strID , Toast.LENGTH_SHORT);

                        sendACK();
                    } while( false );

                    Log.d(TAG, "-----------------------------------------");
                    Log.d(TAG, "BGXpressService.BGX_DATA_RECEIVED - END");
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

                    Intent passwordIntent = new Intent(context, com.bumyeong.rfhook.Password.class);
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
                Log.d(TAG, "Handle message - " + msg);
                return false;
            }
        });

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

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strID = mEditTextID.getText().toString();
                int i = 0;

                if( strID.length() != 4) {
                    ShowNotification("ID length must be 4 !!!", Toast.LENGTH_LONG);
                    return;
                }

                // ID copy to FRAME
                byte btValue = 0;
                for( i = 0; i < strID.length(); i++ ) {
                    btValue = Byte.valueOf(strID.substring(i, i+1));
                    mCommandFrame[COMMAND_FRAME_POS_ID_START + i] = btValue;
                }

                // Steady OR Blinking
                if( mRadioButtonSteady.isChecked() == true ) {
                    mCommandFrame[COMMAND_FRAME_POS_TYPE] = (byte)0x01;
                }
                else {
                    mCommandFrame[COMMAND_FRAME_POS_TYPE] = (byte)0x02;
                }

                computeCheckSum();
                ShowNotification("SEND OK!", Toast.LENGTH_SHORT);

                String message = new String(mCommandFrame);
                sendToBgxService(message);
            }
        });

        BGXpressService.getBGXDeviceInfo(this, mDeviceAddress);
    }

    @Override
    protected void onDestroy() {
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
        String password = com.bumyeong.rfhook.Password.RetrievePassword(am, BusModePasswordKind, mDeviceAddress);

        if (null != password) {
            intent.putExtra("password", password);
        }

        startService(intent);
    }

    public void sendACK() {
        Log.d(TAG, "[PROTOCOL] sendACK() - START");
        String msg2Send = new String(mCommandAck);
        sendToBgxService(msg2Send);
        Log.d(TAG, "[PROTOCOL] sendACK() - END");
    }

    public void sendNCK() {
        Log.d(TAG, "[PROTOCOL] sendNCK() - START");
        String msg2Send = new String(mCommandNck);
        sendToBgxService(msg2Send);
        Log.d(TAG, "[PROTOCOL] sendNCK() - END");
    }

    public void sendToBgxService(String message) {
        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_DATA);
        final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.rfhook", MODE_PRIVATE);

        writeIntent.putExtra("value", message);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);
    }

    public void computeCheckSum() {
        int cs = 0;

        for(int i = COMMAND_FRAME_POS_LENGTH; i <COMMAND_FRAME_POS_DEL_ETX; i++) {
            cs += mCommandFrame[i] & 0xFF;
        }

        mCommandFrame[COMMAND_FRAME_POS_CS] = (byte)(cs & 0xFF);
    }

    public void ShowNotification(String message, int duration) {
        Toast.makeText(getApplicationContext(), message, duration).show();
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
}
