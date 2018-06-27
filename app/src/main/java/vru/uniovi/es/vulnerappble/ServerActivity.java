package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;

public class ServerActivity extends AppCompatActivity {
    private static final String TAG = "ServerActivity";

    private Handler mHandler;
    private Handler mLogHandler;
    private List<BluetoothDevice> mDevices;

    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

  /*  mBinding = DataBindingUtil.setContentView(this, R.layout.activity_server);
    mBinding.restartServerButton.setOnClickListener(v -> restartServer());
    mBinding.viewServerLog.clearLogButton.setOnClickListener(v -> clearLogs());
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }//onCreate

    @Override
    protected void onResume() {
        super.onResume();

        // Check if Bluetooth is enable
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Request user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            finish();
            return;
        }
        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()){
            finish();
            return;
        }
        mBluetoothLeAdvertiser=mBluetoothAdapter.getBluetoothLeAdvertiser();
        //GattServerCallback gattServerCallback= new GattServerCallback();
        //mGattServer=mBluetoothManager.openGattServer(this, gattServerCallback);

    } //onResume
  /*  private class GattServerCallback extends BluetoothGattServerCallback {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
           log ("onConnectionStateChange " + device.getAddress() + "\nstatus " + status + "\nnewState " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                addDevice(device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                removeDevice(device);
            }
        }
    }
    public void log(String msg) {
        Log.d(TAG, msg);
        mLogHandler.post(() -> {
            mBinding.viewServerLog.logTextView.append(msg + "\n");
            mBinding.viewServerLog.logScrollView.post(() -> mBinding.viewServerLog.logScrollView.fullScroll(View.FOCUS_DOWN));
        });
    }*/


}
