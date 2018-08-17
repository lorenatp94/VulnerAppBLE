package vru.uniovi.es.vulnerappble;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

public class ServerActivity extends AppCompatActivity {
    private static final String TAG = "ServerActivity";

    public static String SERVICE_STRING = "5377e081-74a8-4e92-86c1-ec474ec11d61";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);
    private TextView serverAddres;

    private Handler mHandler;
    private Handler mLogHandler;
    private List<BluetoothDevice> mDevices;

    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private ProgressDialog dialog;

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

        serverAddres = (TextView) findViewById(R.id.serverAddres);


    }//onCreate

    @Override
    protected void onResume() {
        super.onResume();

        // Chequeo Bluetooth encendido
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Petición de encender Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            finish();
            return;
        }

        //Chequeo de que el Advertising es compatible con el hardware del dispositivo
        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()){
            Log.d(TAG, "No ad suppported.");
            finish();

            return;
        }
        //Acceso al Advertiser
        mBluetoothLeAdvertiser=mBluetoothAdapter.getBluetoothLeAdvertiser();

        GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);

        setupServer();
        startAdvertising();

        //Muestra MAC local por pantalla
        String macAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
        serverAddres.setText(getString(R.string.serverAd, macAddress ));

    } //onResume

    //Clase para añadir y quitar servicios de la lista basada en newState
    private class GattServerCallback extends BluetoothGattServerCallback{
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mDevices.add(device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mDevices.remove(device);
            }
        }
    }//GattServerCalback

    //Se añade el Servicio al Servidor
    //SERVICE_UUID es un identificador unico que asegura que se está conectando al servidor Gatt correcto.
    private void setupServer(){
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mGattServer.addService(service);
    }

    //Configuración del Advertising del servidor Gatt
    private void startAdvertising(){
        if (mBluetoothLeAdvertiser==null){
            return;
        }

        AdvertiseSettings settings =new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();
        //Balanced Advertise para que sea rápidamente detectable pero no consuma tanta energía como Low Latency
        //Connectable true porque queremos pasar datos en ambos sentidos no como una baliza.
        //Timeout a 0 para anunciarse siempre
        //Low Power Level ya que estamos utilizando BLE

        ParcelUuid parcelUuid= new ParcelUuid(SERVICE_UUID);
        AdvertiseData data =new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build();
        //Incluir el nombre del dispositivo hace que sea más fácil de identificar el servidor

        mBluetoothLeAdvertiser.startAdvertising(settings,data,mAdvertiseCallback);

        //serverAddres.setText("Server Address: " + mDevices.toString());
    }//startAdvertising

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "Peripheral advertising started.");

        }

        @Override
        public void onStartFailure (int errorCode){
            Log.d(TAG, "Peripheral advertising failed: " + errorCode);
        }
    };

    protected void onPause(){
        super.onPause();
        stopAdvertising();
        stopServer();
    }

    private void stopServer(){
        if(mGattServer != null){
            mGattServer.close();
        }
        Log.d(TAG, "Server stopped.");
    }

    //Para gastar menos batería, el servidor debe pararse cuando esté en background
    private void stopAdvertising(){
        if(mBluetoothLeAdvertiser !=null){
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }

   /* private class ServerTask extends AsyncTask <Void, Float, String>{

        protected void onPreExecute(){
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            Context cont= getApplicationContext();
            String macAddress = android.provider.Settings.Secure.getString(cont.getContentResolver(), "bluetooth_address");
            return macAddress;
        }

        protected void onProgressUpdate(Float... percent){
            int p =Math.round(100*percent[0]);
            dialog.setProgress(p);
        }
    }//Asynctask*/

}
