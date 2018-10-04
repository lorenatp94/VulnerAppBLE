package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Map;

//Clase para añadir o quitar servicioss de la lista
public class GattServerCallback extends BluetoothGattServerCallback {
    public Map<BluetoothDevice, String> mScanResults;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;


    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);


        if (newState == BluetoothProfile.STATE_CONNECTED) {
            //mScanResults.put(device, deviceAddress);

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            //mScanResults.remove(deviceAddress);
            //deleteResult(device);

        }
    }

    public void deleteResult(BluetoothDevice device) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).contains(device.getAddress())) {
                arrayList.remove(i);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
