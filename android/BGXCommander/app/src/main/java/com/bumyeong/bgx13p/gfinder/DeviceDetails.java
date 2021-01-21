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

package com.bumyeong.bgx13p.gfinder;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bw.ydb.widgets.TextProgressBar;
import com.bw.yml.YModem;
import com.bw.yml.YModemListener;
import com.silabs.bgxpress.BGX_CONNECTION_STATUS;
import com.silabs.bgxpress.BGXpressService;
import com.silabs.bgxpress.BusMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static com.bumyeong.bgx13p.gfinder.PasswordKind.BusModePasswordKind;
import static com.bumyeong.bgx13p.gfinder.TextSource.LOCAL;
import static com.bumyeong.bgx13p.gfinder.TextSource.REMOTE;
import static java.lang.Thread.sleep;

public class DeviceDetails extends AppCompatActivity {
    private final static String TAG = "bgx_dbg"; //DeviceList.class.getSimpleName();

    private YModem mYModem;
    private TextProgressBar mUpdateProgressBar;
    private boolean mIsSendData;
    private boolean mIsFWUpdate;

   // public BluetoothDevice mBluetoothDevice;
    public String mDeviceAddress;
    public String mDeviceName;

    public Handler mHandler;

    public int mDeviceConnectionState;

    private BroadcastReceiver mConnectionBroadcastReceiver;
    private BroadcastReceiver mBondReceiver;
    public final Context mContext = this;


    // UI Elements
//    private TextView tvDeviceName;
    private TextView tvMacAddress;
    private TextView tvRemainTime;
    private TextView tvO2Low;
    private TextView tvO2High;
    private TextView tvO2Current;
    private TextView tvCh4Low;
    private TextView tvCh4High;
    private TextView tvCh4Current;
    private TextView tvH2sLow;
    private TextView tvH2sHigh;
    private TextView tvH2sCurrent;
    private TextView tvCombLow;
    private TextView tvCombHigh;
    private TextView tvCombCurrent;
    private TextView tvLastTime;
    private TextView tvMacAddress2;
    private TextView tvRemainTime2;
    private TextView tvO2Low2;
    private TextView tvO2High2;
    private TextView tvO2Current2;
    private TextView tvCh4Low2;
    private TextView tvCh4High2;
    private TextView tvCh4Current2;
    private TextView tvH2sLow2;
    private TextView tvH2sHigh2;
    private TextView tvH2sCurrent2;
    private TextView tvCombLow2;
    private TextView tvCombHigh2;
    private TextView tvCombCurrent2;
    private TextView tvLastTime2;

    private int mBusMode;

    private TextSource mTextSource = TextSource.UNKNOWN;
    private final int kAutoScrollMessage = 0x5C011;
    private final int kAutoScrollDelay = 800; // the time in ms between adding text and autoscroll.

    private MenuItem mIconItem;
    private MenuItem mUpdateItem;

    private BGXpressService.BGXPartID mBGXPartID;
    private String mBGXDeviceID;
    private String mBGXPartIdentifier;

    final static Integer kBootloaderSecurityVersion = 1229;

    private boolean mIsTransferDataType = true;
    private GFinderComm mGFinderComm = new GFinderComm();

//    public class rnHandler extends Handler { // (r)ead (n)otification 인듯
//        public rnHandler() {
//        }
//
//        public void handleMessage(Message message) {
//            removeMessages(0);
//
//            byte[] sendData = Arrays.copyOf(mGFinderComm.getPacketRequestInformation(), mGFinderComm.getPacketSize());
//
//            Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_BIN_DATA);
//            writeIntent.setClass(mContext, BGXpressService.class);
//            writeIntent.putExtra("DeviceAddress", mDeviceAddress);
//            writeIntent.putExtra("value", sendData);
//            startService(writeIntent);
//
//            if (mRnState) {
//                sendEmptyMessageDelayed(0, 1000);
//            }
//        }
//    }

//    private rnHandler mRnHandler = new rnHandler();
//    private boolean mRnState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        mIsSendData = false;
        mIsFWUpdate = false;

        mBusMode = BusMode.UNKNOWN_MODE;

//        tvDeviceName = (TextView)findViewById(R.id.txtDeviceName);
        tvMacAddress = (TextView)findViewById(R.id.txtMacAddress);
        tvRemainTime = (TextView)findViewById(R.id.txtRemainTime);
        tvO2Low = (TextView)findViewById(R.id.txtO2Low);
        tvO2High = (TextView)findViewById(R.id.txtO2High);
        tvO2Current = (TextView)findViewById(R.id.txtO2Current);
        tvCh4Low = (TextView)findViewById(R.id.txtCH4Low);
        tvCh4High = (TextView)findViewById(R.id.txtCH4High);
        tvCh4Current = (TextView)findViewById(R.id.txtCH4Current);
        tvH2sLow = (TextView)findViewById(R.id.txtH2SLow);
        tvH2sHigh = (TextView)findViewById(R.id.txtH2SHigh);
        tvH2sCurrent = (TextView)findViewById(R.id.txtH2SCurrent);
        tvCombLow = (TextView)findViewById(R.id.txtCombLow);
        tvCombHigh = (TextView)findViewById(R.id.txtCombHigh);
        tvCombCurrent = (TextView)findViewById(R.id.txtCombCurrent);
        tvLastTime = (TextView)findViewById(R.id.txtUpdateTime);

        tvMacAddress2 = (TextView)findViewById(R.id.txtMacAddress2);
        tvRemainTime2 = (TextView)findViewById(R.id.txtRemainTime2);
        tvO2Low2 = (TextView)findViewById(R.id.txtO2Low2);
        tvO2High2 = (TextView)findViewById(R.id.txtO2High2);
        tvO2Current2 = (TextView)findViewById(R.id.txtO2Current2);
        tvCh4Low2 = (TextView)findViewById(R.id.txtCH4Low2);
        tvCh4High2 = (TextView)findViewById(R.id.txtCH4High2);
        tvCh4Current2 = (TextView)findViewById(R.id.txtCH4Current2);
        tvH2sLow2 = (TextView)findViewById(R.id.txtH2SLow2);
        tvH2sHigh2 = (TextView)findViewById(R.id.txtH2SHigh2);
        tvH2sCurrent2 = (TextView)findViewById(R.id.txtH2SCurrent2);
        tvCombLow2 = (TextView)findViewById(R.id.txtCombLow2);
        tvCombHigh2 = (TextView)findViewById(R.id.txtCombHigh2);
        tvCombCurrent2 = (TextView)findViewById(R.id.txtCombCurrent2);
        tvLastTime2 = (TextView)findViewById(R.id.txtUpdateTime2);

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
                if (intentDeviceAddress != null && intentDeviceAddress.length() > 1 && !intentDeviceAddress.equalsIgnoreCase(mDeviceAddress)) {
                    return;
                }

                switch(intent.getAction()) {

                    case BGXpressService.BGX_INVALID_GATT_HANDLES: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Invalid GATT Handles");
                        builder.setMessage("The bonding information on this device is invalid (probably due to a firmware update). You should select "+mDeviceName+" in the Bluetooth Settings and choose \"Forget\".");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dlg = builder.create();
                        dlg.show();
                    }
                    break;

                    case BGXpressService.DMS_VERSIONS_AVAILABLE: {

                        Integer bootloaderVersion = BGXpressService.getBGXBootloaderVersion(mDeviceAddress);
                        if ( bootloaderVersion >= kBootloaderSecurityVersion) {

                            Log.d(TAG, "Received DMS Versions.");

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

                    case BGXpressService.BGX_CONNECTION_STATUS_CHANGE: {
                        Log.d(TAG, "BGX Connection State Change");

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
                    case BGXpressService.BGX_MODE_STATE_CHANGE: {
                        Log.d(TAG, "BGX Bus Mode Change");
                        setBusMode(intent.getIntExtra("busmode", BusMode.UNKNOWN_MODE));
                    }
                    break;
                    case BGXpressService.BGX_DATA_RECEIVED: {
                        if( mIsTransferDataType ) {
                            String stringReceived = intent.getStringExtra("data");
                            processText(stringReceived, REMOTE);
                        }
                        else {
                            byte[] data = intent.getByteArrayExtra("dataBytes");

                            if( mIsFWUpdate ) {
                                mIsSendData = true;
                                mYModem.onReceiveData(data);
                            }
                            else {
//                                Log.e(TAG, "BGX binary data receiver : " + data.length);
                                switch(mGFinderComm.parse(data)) {

                                    case GFinderComm.COMMAND_SEND_DATA_INIT: {
                                        sendPacket(Arrays.copyOf(mGFinderComm.getPacketAck(), mGFinderComm.getPacketSize()), 1);

                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tvMacAddress.setText(mGFinderComm.getMacAddress());
                                                tvMacAddress2.setText(mGFinderComm.getMacAddress2());
                                                showInitData();
                                            }
                                        });
                                    }
                                    break;

                                    case GFinderComm.COMMAND_SEND_DATA: {
                                        sendPacket(Arrays.copyOf(mGFinderComm.getPacketAck(), mGFinderComm.getPacketSize()), 1);

                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tvMacAddress.setText(mGFinderComm.getMacAddress());
                                                tvMacAddress2.setText(mGFinderComm.getMacAddress2());
                                                showCurrentData();
                                            }
                                        });
                                    }
                                    break;

                                    case GFinderComm.COMMAND_ACK: {
                                    }
                                    break;

                                    case GFinderComm.COMMAND_UNKNOWN: {
                                    }
                                    break;

                                    default: {
                                        Log.e(TAG, "Unknown return value !!!");
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    break;
                    case BGXpressService.BGX_DEVICE_INFO: {

                        Integer bootloaderVersion = BGXpressService.getBGXBootloaderVersion(mDeviceAddress);

                        mBGXDeviceID = intent.getStringExtra("bgx-device-uuid");
                        mBGXPartID = (BGXpressService.BGXPartID) intent.getSerializableExtra("bgx-part-id" );
                        mBGXPartIdentifier = intent.getStringExtra("bgx-part-identifier");

                        if ( bootloaderVersion >= kBootloaderSecurityVersion) {
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
                        } else if ( bootloaderVersion > 0) {
                            mIconItem.setIcon(ContextCompat.getDrawable(mContext, R.drawable.security_decoration));
                        }
                    }
                    break;
                    case BGXpressService.BUS_MODE_ERROR_PASSWORD_REQUIRED: {

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

                try {
                    sleep(100);
                    setBleDataTypeBinary();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

//        sendPacket(Arrays.copyOf(mGFinderComm.getPacketConnectionOk(), mGFinderComm.getPacketSize()), 1);

        BGXpressService.getBGXDeviceInfo(this, mDeviceAddress);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Unregistering the connectionBroadcastReceiver");

        if(mYModem != null) {
            mYModem.stop();
        }

        unregisterReceiver(mConnectionBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.devicedetails, menu);
//
//        mIconItem = menu.findItem(R.id.icon_menuitem);
//        mUpdateItem = menu.findItem(R.id.update_menuitem);
//        mIconItem.setIcon(null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {

        switch(mi.getItemId()) {
            case R.id.update_menuitem: {
                Log.d(TAG, "Update menu item pressed.");

                String api_key = null;
                try {
                    ComponentName myService = new ComponentName(this, BGXpressService.class);
                    Bundle data = getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData;
                    if (null != data) {
                        api_key = data.getString("DMS_API_KEY");
                    }
                } catch (PackageManager.NameNotFoundException exception) {
                    exception.printStackTrace();
                }

                if (null == api_key || 0 == api_key.compareTo("MISSING_KEY") ) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("MISSING_KEY");
                    builder.setMessage("The DMS_API_KEY supplied in your app's AndroidManifest.xml is missing. Contact Silicon Labs xpress@silabs.com for a DMS API Key for BGX.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dlg = builder.create();
                    dlg.show();

                } else if ( null == mBGXPartID || BGXpressService.BGXPartID.BGXInvalid == mBGXPartID ) {
                    Toast.makeText(this, "Invalid BGX Part ID", Toast.LENGTH_LONG).show();
                } else {

                    Intent intent = new Intent(this, FirmwareUpdate.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("bgx-device-uuid", mBGXDeviceID);
                    intent.putExtra("bgx-part-id", mBGXPartID);
                    intent.putExtra("bgx-part-identifier", mBGXPartIdentifier);
                    intent.putExtra("DeviceAddress", mDeviceAddress);
                    intent.putExtra("DeviceName", mDeviceName);

                    startActivity(intent);
                }
            }
                break;
            case R.id.options_menuitem: {
                final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.bgx13p.gfinder", MODE_PRIVATE);
                Boolean fNewLinesOnSendValue =  sp.getBoolean("newlinesOnSend", true);
                Boolean fUseAckdWritesForOTA = sp.getBoolean("useAckdWritesForOTA", true);

                final Dialog optionsDialog = new Dialog(this);
                optionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                optionsDialog.setContentView(R.layout.optionsbox);

                final CheckBox newLineCB = optionsDialog.findViewById(R.id.newline_cb);
                final CheckBox otaAckdWrites = (CheckBox) optionsDialog.findViewById(R.id.acknowledgedOTA);

                newLineCB.setChecked(fNewLinesOnSendValue);
                otaAckdWrites.setChecked(fUseAckdWritesForOTA);

                Button saveButton = (Button)optionsDialog.findViewById(R.id.save_btn);
                saveButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Boolean fValue = newLineCB.isChecked();
                        Boolean fValue2 = otaAckdWrites.isChecked();

                        SharedPreferences.Editor editor = sp.edit();

                        editor.putBoolean("newlinesOnSend", fValue);
                        editor.putBoolean("useAckdWritesForOTA", fValue2);
                        
                        editor.apply();

                        optionsDialog.dismiss();
                    }
                });

                Button cancelButton = (Button)optionsDialog.findViewById(R.id.cancel_btn);
                cancelButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        optionsDialog.dismiss();
                    }
                });

                optionsDialog.show();
            }
            break;

            case R.id.fwupdate_menuitem: {
                final Dialog updateDialog = new Dialog(this);
                updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                updateDialog.setContentView(R.layout.fw_update_ymodem);

                Button btnStart = (Button)updateDialog.findViewById(R.id.fwupdate_start_btn);
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsFWUpdate = true;
                        startYModem();
                    }
                });

                Button btnCancel = (Button)updateDialog.findViewById(R.id.fwupdate_cancel_btn);
                btnCancel.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if(mYModem != null) {
                            mYModem.stop();
                        }

                        updateDialog.dismiss();
                    }
                });

                mUpdateProgressBar = (TextProgressBar)updateDialog.findViewById(R.id.mUpgradeBar);

                updateDialog.show();
            }
            break;
        }

        return super.onOptionsItemSelected(mi);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed.");
        if(mYModem != null) {
            mYModem.stop();
        }

        disconnect();

        finish();
        super.onBackPressed();
    }

    void disconnect()
    {
        Intent intent = new Intent(BGXpressService.ACTION_BGX_DISCONNECT);
        intent.setClass(mContext, BGXpressService.class);
        intent.putExtra("DeviceAddress", mDeviceAddress);
        startService(intent);
    }





    public void setBusMode(int busMode) {
        if (mBusMode != busMode) {

            mBusMode = busMode;
        }
    }

    public void sendBusMode(int busMode)
    {
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

    public int getBusMode() {
        return mBusMode;
    }


    void processText(String text, TextSource ts ) {

        String newText;

        SpannableStringBuilder ssb = new SpannableStringBuilder();

        final SharedPreferences sp = mContext.getSharedPreferences("com.bumyeong.bgx13p.gfinder", MODE_PRIVATE);
        Boolean fNewLinesOnSendValue =  sp.getBoolean("newlinesOnSend", true);

        switch (ts) {
            case LOCAL: {
                if (LOCAL != mTextSource && fNewLinesOnSendValue) {
                    ssb.append("\n>", new ForegroundColorSpan(Color.WHITE), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                ssb.append(text, new ForegroundColorSpan(Color.WHITE), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            break;
            case REMOTE: {
                if (REMOTE != mTextSource && fNewLinesOnSendValue) {
                    ssb.append("\n<", new ForegroundColorSpan(Color.GREEN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                ssb.append(text, new ForegroundColorSpan(Color.GREEN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            }
        }

        mTextSource = ts;

    }

    /*
     * The purpose of this function is to test ACTION_WRITE_SERIAL_BIN_DATA
     * by sending all the possible byte values.
     */
    void bytetest() {
        byte[] myByteArray = new byte[256];
        for (int i = 0; i < 256; ++i) {
            myByteArray[i] = (byte)i;
        }

        Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_BIN_DATA);
        writeIntent.putExtra("value", myByteArray);
        writeIntent.setClass(mContext, BGXpressService.class);
        writeIntent.putExtra("DeviceAddress", mDeviceAddress);
        startService(writeIntent);
    }

    private void sendPacket(final byte[] packet, long millisec) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_BIN_DATA);
                writeIntent.setClass(mContext, BGXpressService.class);
                writeIntent.putExtra("DeviceAddress", mDeviceAddress);
                writeIntent.putExtra("value", packet);
                startService(writeIntent);
            }
        }, millisec);
    }

    private void startYModem() {
        if( mYModem != null ) {
            mYModem.stop();
        }

        setBleDataTypeBinary();

        mIsSendData = true;
        mYModem = new YModem.Builder()
                .with(this)
                .filePath("assets://simple.bin")
                .fileName("simple.bin")
                .checkMd5("")
                .sendSize(1024)
                .callback(new YModemListener() {
                    @Override
                    public void onDataReady(byte[] data) {
                        if(mIsSendData) {
                            Intent writeIntent = new Intent(BGXpressService.ACTION_WRITE_SERIAL_BIN_DATA);
                            writeIntent.setClass(mContext, BGXpressService.class);
                            writeIntent.putExtra("value", data);
                            writeIntent.putExtra("DeviceAddress", mDeviceAddress);
                            startService(writeIntent);
                            mIsSendData = false;
                        }
                    }

                    @Override
                    public void onProgress(int currentSent, int total) {
                        float currentPt = (float)currentSent/total;
                        int a = (int)(currentPt*100);

                        mUpdateProgressBar.setProgress(currentSent);   // Main Progress
                        mUpdateProgressBar.setMax(total); // Maximum Progress

                        if(a <= 100){
                            mUpdateProgressBar.setProgressText(""+a+"%");
                        }else{
                            mUpdateProgressBar.setProgressText("100%");
                        }
                    }

                    @Override
                    public void onSuccess() {
                        setBleDataTypeString();
                        Toast.makeText(DeviceDetails.this,"YMODEM SUCCESS !",Toast.LENGTH_LONG).show();
                        mIsFWUpdate = false;
                    }

                    @Override
                    public void onFailed(String reason) {
                        Toast.makeText(DeviceDetails.this,"YMODEM FAILED : " + reason,Toast.LENGTH_LONG).show();
                    }
                }).build();
        mYModem.start();
    }

    private void setBleDataTypeBinary() {
        mIsTransferDataType = false;

        Intent typeIntent = new Intent(BGXpressService.ACTION_DATA_DEFINE_DATA_TYPE);
        typeIntent.setClass(mContext, BGXpressService.class);
        typeIntent.putExtra("mIsTransferDataType", mIsTransferDataType);
        startService(typeIntent);
    }

    private void setBleDataTypeString() {
        mIsTransferDataType = true;

        Intent typeIntent = new Intent(BGXpressService.ACTION_DATA_DEFINE_DATA_TYPE);
        typeIntent.setClass(mContext, BGXpressService.class);
        typeIntent.putExtra("mIsTransferDataType", mIsTransferDataType);
        startService(typeIntent);
    }

    private void showInitData() {
        tvO2Low.setText(mGFinderComm.getInitDataO2Low());
        tvO2High.setText(mGFinderComm.getInitDataO2High());
        tvCh4Low.setText(mGFinderComm.getInitDataCH4Low());
        tvCh4High.setText(mGFinderComm.getInitDataCH4High());
        tvH2sLow.setText(mGFinderComm.getInitDataH2SLow());
        tvH2sHigh.setText(mGFinderComm.getInitDataH2SHigh());
        tvCombLow.setText(mGFinderComm.getInitDataCoLow());
        tvCombHigh.setText(mGFinderComm.getInitDataCoHigh());

        tvO2Low2.setText(mGFinderComm.getInitDataO2Low2());
        tvO2High2.setText(mGFinderComm.getInitDataO2High2());
        tvCh4Low2.setText(mGFinderComm.getInitDataCH4Low2());
        tvCh4High2.setText(mGFinderComm.getInitDataCH4High2());
        tvH2sLow2.setText(mGFinderComm.getInitDataH2SLow2());
        tvH2sHigh2.setText(mGFinderComm.getInitDataH2SHigh2());
        tvCombLow2.setText(mGFinderComm.getInitDataCoLow2());
        tvCombHigh2.setText(mGFinderComm.getInitDataCoHigh2());
    }

    private void showCurrentData() {
        tvO2Current.setText(mGFinderComm.getCurrentDataO2());
        tvCh4Current.setText(mGFinderComm.getCurrentDataCH4());
        tvH2sCurrent.setText(mGFinderComm.getCurrentDataH2S());
        tvCombCurrent.setText(mGFinderComm.getCurrentDataCo());
        tvLastTime.setText(mGFinderComm.getTime());

        if(mGFinderComm.isAlarmO2() == true) {
            tvO2Current.setBackgroundResource(R.color.colorPrimary);
            tvO2Current.setTextColor(R.color.white);
        }
        else {
            tvO2Current.setBackgroundResource(R.color.white);
            tvO2Current.setTextColor(R.color.black);
        }
        if(mGFinderComm.isAlarmCH4() == true) {
            tvCh4Current.setBackgroundResource(R.color.colorPrimary);
            tvCh4Current.setTextColor(R.color.white);
        }
        else {
            tvCh4Current.setBackgroundResource(R.color.white);
            tvCh4Current.setTextColor(R.color.black);
        }
        if(mGFinderComm.isAlarmH2S() == true) {
            tvH2sCurrent.setBackgroundResource(R.color.colorPrimary);
            tvH2sCurrent.setTextColor(R.color.white);
        }
        else {
            tvH2sCurrent.setBackgroundResource(R.color.white);
            tvH2sCurrent.setTextColor(R.color.black);
        }
        if(mGFinderComm.isAlarmCo() == true) {
            tvCombCurrent.setBackgroundResource(R.color.colorPrimary);
            tvCombCurrent.setTextColor(R.color.white);
        }
        else {
            tvCombCurrent.setBackgroundResource(R.color.white);
            tvCombCurrent.setTextColor(R.color.black);
        }

        tvO2Current2.setText(mGFinderComm.getCurrentDataO22());
        tvCh4Current2.setText(mGFinderComm.getCurrentDataCH42());
        tvH2sCurrent2.setText(mGFinderComm.getCurrentDataH2S2());
        tvCombCurrent2.setText(mGFinderComm.getCurrentDataCo2());
        tvLastTime2.setText(mGFinderComm.getTime2());

        if(mGFinderComm.isAlarmO22() == true) {
            tvO2Current2.setBackgroundResource(R.color.colorPrimary);
            tvO2Current2.setTextColor(R.color.white);
        }
        else {
            tvO2Current2.setBackgroundResource(R.color.white);
            tvO2Current2.setTextColor(R.color.black);
        }
        if(mGFinderComm.isAlarmCH42() == true) {
            tvCh4Current2.setBackgroundResource(R.color.colorPrimary);
            tvCh4Current2.setTextColor(R.color.white);
        }
        else {
            tvCh4Current2.setBackgroundResource(R.color.white);
            tvCh4Current2.setTextColor(R.color.black);
        }
        if(mGFinderComm.isAlarmH2S2() == true) {
            tvH2sCurrent2.setBackgroundResource(R.color.colorPrimary);
            tvH2sCurrent2.setTextColor(R.color.white);
        }
        else {
            tvH2sCurrent2.setBackgroundResource(R.color.white);
            tvH2sCurrent2.setTextColor(R.color.black);
        }
        if(mGFinderComm.isAlarmCo2() == true) {
            tvCombCurrent2.setBackgroundResource(R.color.colorPrimary);
            tvCombCurrent2.setTextColor(R.color.white);
        }
        else {
            tvCombCurrent2.setBackgroundResource(R.color.white);
            tvCombCurrent2.setTextColor(R.color.black);
        }
    }
}
