package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;


//Clase para añadir o quitar servicioss de la lista
public class GattServerCallback extends BluetoothGattServerCallback {
    private static final String GattServerTAG = "GattServer";

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.e(GattServerTAG, "GattServer Connected" );

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.e(GattServerTAG, "GattServer Disconnected" );
        }
    }
}
