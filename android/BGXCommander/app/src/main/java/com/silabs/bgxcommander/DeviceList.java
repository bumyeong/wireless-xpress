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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.silabs.bgxpress.BGX_CONNECTION_STATUS;
import com.silabs.bgxpress.BGXpressService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DeviceList extends AppCompatActivity {
    private final static String TAG = "bgx_dbg"; //DeviceList.class.getSimpleName();

    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;

    private static final long SCAN_PERIOD = 10000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private RecyclerView mDeviceListRecyclerView;
    private RecyclerView.Adapter mDeviceListAdapter;
    private RecyclerView.LayoutManager mDeviceListLayoutManager;
    private ArrayList<Map<String, String> > mScanResults;
    private TextView mBluetoothDisabledWarningTextView;
    private TextView mLocationPermissionDeniedTextView;

    private boolean fScanning = false;
    private boolean fLocationPermissionGranted = false;
    private boolean fInvalidGattHandles = false;

    private BroadcastReceiver mDeviceDiscoveryReceiver;

    private MenuItem mScanItem;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mContext = this;
        fInvalidGattHandles = false;

        mDeviceDiscoveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    //------------------------------------------------------------------------------
                    case BGXpressService.BGX_SCAN_DEVICE_DISCOVERED: {
                    //------------------------------------------------------------------------------
                        Log.d(TAG, "BroadcastReceiver - BGXpressService.BGX_SCAN_DEVICE_DISCOVERED");
                        HashMap<String, String> deviceRecord =
                                (HashMap<String, String>) intent.getSerializableExtra("DeviceRecord");

                        // must now check if the scan results already contain this device
                        // because we are no longer clearing the scan results
                        // when scan starts because in multi-connect scenario you wouldn't rediscover devices you are already connected to.

                        String devAddr = deviceRecord.get("uuid");
                        boolean fContainsRecord = false;

                        for (int i = 0; i < mScanResults.size(); ++i) {
                            HashMap<String, String> iDeviceRecord = (HashMap<String, String>) mScanResults.get(i);
                            String iDeviceAddr = iDeviceRecord.get("uuid");

                            if (devAddr.equalsIgnoreCase(iDeviceAddr)) {
                                fContainsRecord = true;
                                break;
                            }
                        }

                        if (fContainsRecord == false) {
                            mScanResults.add(deviceRecord);

                            Collections.sort(mScanResults, new Comparator<Map<String, String>>() {
                                @Override
                                public int compare(Map<String, String> leftRecord, Map<String, String> rightRecord) {
                                    String leftRssi = leftRecord.get("rssi");
                                    String rightRssi = rightRecord.get("rssi");
                                    return leftRssi.compareTo(rightRssi);

                                }
                            });

                            mDeviceListAdapter = new BGXDeviceListAdapter(getApplicationContext(), mScanResults);
                            mDeviceListRecyclerView.swapAdapter(mDeviceListAdapter, true);
                        }
                    }
                    break;

                    //------------------------------------------------------------------------------
                    case BGXpressService.BGX_CONNECTION_STATUS_CHANGE: {
                    //------------------------------------------------------------------------------
                        Log.d(TAG, "BroadcastReceiver - BGXpressService.BGX_CONNECTION_STATUS_CHANGE");
                        if (fInvalidGattHandles == false) {
                            BGX_CONNECTION_STATUS connectionStatusValue = (BGX_CONNECTION_STATUS) intent.getSerializableExtra("bgx-connection-status");

                            if (BGX_CONNECTION_STATUS.CONNECTED == connectionStatusValue) {
                                BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra("device");
                                Intent intent2 = new Intent(context, DeviceDetails.class);
                                intent2.putExtra("BLUETOOTH_DEVICE", btDevice);
                                intent2.putExtra("DeviceName", btDevice.getName());
                                intent2.putExtra("DeviceAddress", btDevice.getAddress());
                                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                BGXpressService.setBGXAcknowledgedReads(btDevice.getAddress(), true);
                                BGXpressService.setBGXAcknowledgedWrites(btDevice.getAddress(), true);

                                Log.d(TAG, "startActivity - " + DeviceDetails.class.toString());
                                context.startActivity(intent2);
                            }
                        }
                    }
                    break;

                    //------------------------------------------------------------------------------
                    case BGXpressService.BGX_SCAN_MODE_CHANGE: {
                    //------------------------------------------------------------------------------
                        Log.d(TAG, "BroadcastReceiver - BGXpressService.BGX_SCAN_MODE_CHANGE");
                        fScanning = intent.getBooleanExtra("isscanning", false);
                        boolean fScanFailed = intent.getBooleanExtra("scanFailed", false);
                        if (fScanFailed) {
                            int error = intent.getIntExtra("error", 0);
                            Toast.makeText(mContext, "Scan Failed. Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;

                    //------------------------------------------------------------------------------
                    case BGXpressService.BGX_INVALID_GATT_HANDLES: {
                    //------------------------------------------------------------------------------
                        Log.d(TAG, "BroadcastReceiver - BGXpressService.BGX_INVALID_GATT_HANDLES");
                        String deviceAddress = intent.getStringExtra("DeviceAddress");
                        String deviceName = intent.getStringExtra("DeviceName");
                        BGXpressService.startActionBGXCancelConnect(mContext, deviceAddress);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Invalid GATT Handles");
                        builder.setMessage("The bonding information on this device is invalid (probably due to a firmware update). You should select " + deviceName + " in the Bluetooth Settings and choose \"Forget\" and then turn Bluetooth off and back on.");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog dlg = builder.create();
                        dlg.show();
                    }
                    break;
                }
            }
        };

        IntentFilter listIntentFilter = new IntentFilter(BGXpressService.BGX_SCAN_DEVICE_DISCOVERED);
        listIntentFilter.addAction(BGXpressService.BGX_CONNECTION_STATUS_CHANGE);
        listIntentFilter.addAction(BGXpressService.BGX_SCAN_MODE_CHANGE);
        listIntentFilter.addAction(BGXpressService.BGX_INVALID_GATT_HANDLES);
        registerReceiver(mDeviceDiscoveryReceiver, listIntentFilter);

        mDeviceListRecyclerView = (RecyclerView) findViewById(R.id.DeviceListRecyclerView);
        mDeviceListRecyclerView.setHasFixedSize(true);

        mDeviceListLayoutManager = new LinearLayoutManager(this);
        mDeviceListRecyclerView.setLayoutManager(mDeviceListLayoutManager);

        if (null == mScanResults) {
            mScanResults = new ArrayList<Map<String, String>>();
        }

        mDeviceListAdapter = new BGXDeviceListAdapter(this, mScanResults);
        mDeviceListRecyclerView.setAdapter(mDeviceListAdapter);

        mBluetoothDisabledWarningTextView = (TextView) findViewById(R.id.BluetoothDisabledWarning);
        mLocationPermissionDeniedTextView = (TextView) findViewById(R.id.LocationPermissionDenied);

        mHandler = new Handler();

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == false) {
            Log.e(TAG, "BLE Not Supported.");
            finish();
        }

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        startService(new Intent(this, BGXpressService.class));

        // ?????
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mDeviceDiscoveryReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.devicelist, menu);
        mScanItem = menu.findItem(R.id.scan_menuitem);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if (mi.getItemId() == R.id.scan_menuitem) {
                Log.d(TAG, "menu - SCAN");

                // clear the scan list
                // ????

                if (fLocationPermissionGranted == false) {
                    Toast.makeText(this, "NO Location Permission", Toast.LENGTH_LONG).show();
                    return true;
                }

                if (fScanning == true) {
                    BGXpressService.startActionStopScan(this);
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanForDevices();
                    }
                }, 200);
                return true;
        }

        return super.onOptionsItemSelected(mi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                    Log.d(TAG, "Received permissions to use location.");
                    fLocationPermissionGranted = true;
                    mLocationPermissionDeniedTextView.setVisibility(View.GONE);
                    mDeviceListRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    Log.e(TAG, "Did not get permissions to use location.");
                    fLocationPermissionGranted = false;

                    mLocationPermissionDeniedTextView.setVisibility(View.VISIBLE);
                    mDeviceListRecyclerView.setVisibility(View.GONE);
                    mBluetoothDisabledWarningTextView.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        boolean fAdapterEnabled = false;
        try {
            fAdapterEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();

            if (!fAdapterEnabled) {
                mDeviceListRecyclerView.setVisibility(View.GONE);
                mBluetoothDisabledWarningTextView.setVisibility(View.VISIBLE);
            } else {
                mDeviceListRecyclerView.setVisibility(View.VISIBLE);
                mBluetoothDisabledWarningTextView.setVisibility(View.GONE);
            }


        } catch (Exception e) {
            Log.d(TAG, "Exception caught while calling isEnabled.");
            Toast.makeText(this, "Exception caught", Toast.LENGTH_LONG).show();
        }

        if (fAdapterEnabled && fLocationPermissionGranted) {

            if (null != mScanItem) {
                mScanItem.setEnabled(true);
            }

            if (null == BluetoothAdapter.getDefaultAdapter() || !fAdapterEnabled) {
                Log.d(TAG, "bluetooth adapter is not available.");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                scanForDevices();

            }
        } else {
            if (null != mScanItem) {
                mScanItem.setEnabled(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Result canceled.");
                finish();
                return;
            }

            Log.d(TAG, "request enabled");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanForDevices() {
        final Context myContext = this;

        mScanResults = new ArrayList<Map<String, String>>();
        mDeviceListAdapter = new BGXDeviceListAdapter(getApplicationContext(), mScanResults);
        mDeviceListRecyclerView.swapAdapter(mDeviceListAdapter, true);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Starting SCAN");
                BGXpressService.startActionStartScan(myContext);
            }
        }, 200);
    }

}


