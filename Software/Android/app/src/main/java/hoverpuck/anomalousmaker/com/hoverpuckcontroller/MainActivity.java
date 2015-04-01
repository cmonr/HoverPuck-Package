package hoverpuck.anomalousmaker.com.hoverpuckcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public final static UUID UUID_BLUEGIGA_SSP =
            UUID.fromString("af20fbac-2518-4998-9af7-af42540731b3");

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create UI
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ControllerFragment(), "ControllerUI")
                    .commit();
        }

        // Enable Bluetooth if needed
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                // Start scan for BLE Devices
                btAdapter.startLeScan(leScanCallback);
                Log.i(TAG, "Started Scan (1)");
            }
        }
    }

    //
    // BLE Functions
    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:	// Enable Bluetooth request
                if (resultCode == RESULT_CANCELED) {
                    // Request to enable Bluetooth denied
                    //  Quitting due to rejection
                    //exit("Could not enable Bluetooth");
                } else {
                    // Start scan for BLE Devices
                    btAdapter.startLeScan(leScanCallback);
                    Log.i(TAG, "Started Scan (2)");
                }
                break;
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (device.getAddress().contentEquals("00:07:80:06:01:E0"))
            {
                // Device found. Stop scan
                btAdapter.stopLeScan(leScanCallback);
                Log.i(TAG, "Stopped Scan");

                BluetoothGatt bluetoothGatt = device.connectGatt(getBaseContext(), false, bleGattCallback);
            }
        }
    };

    private final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            byte[] data = characteristic.getValue();


            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.i(TAG, stringBuilder.toString());


                characteristic.setValue("abcde\n");
                gatt.writeCharacteristic(characteristic);
            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            Log.i(TAG, "State changed!");

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                gatt.discoverServices();
                Log.i(TAG, "Connected");

                // TODO: Init UI here
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            for (BluetoothGattService service : gatt.getServices()) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (UUID_BLUEGIGA_SSP.compareTo(characteristic.getUuid()) == 0)
                    {
                        Log.i(TAG, "SSP RX found!");


                        gatt.setCharacteristicNotification(characteristic, true);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        gatt.writeDescriptor(descriptor);


                    }/* else if (UUID_BLUEGIGA_SSP_TX.compareTo(characteristic.getUuid()) == 0) {
                        Log.i(TAG, "SSP TX found!");


                        characteristic.setValue("abcde");
                        gatt.writeCharacteristic(characteristic);
                    }*/
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status){

            if (status == BluetoothGatt.GATT_SUCCESS){

                Log.d(TAG,"Write to Characteristic Success! !");
            }

        }
    };


    //
    // Options Menu Functions
    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.ble_connect) {
            // TODO: Implement Start Of BLE connection here
            Log.i(TAG, "Connect button pressed");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}