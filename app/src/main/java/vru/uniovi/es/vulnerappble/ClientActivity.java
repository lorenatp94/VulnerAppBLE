package vru.uniovi.es.vulnerappble;

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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ClientActivity extends AppCompatActivity {

    private static final String ClientTAG = "Client";
    private static final String ServerTAG = "Server";
    public static String SERVICE_STRING = "5377e081-74a8-4e92-86c1-ec474ec11d61";
    private static final int REQUEST_ENABLE_BT = 1;
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);
    private TextView userType;
    //private TextView serverAddres;
    private ListView deviceList;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    private FloatingActionButton mapButton;
    private Button startButton, stopButton, clearButton;

    private List<BluetoothDevice> mDevices;

    private boolean mScanning;
    private Handler mHandler;
    private Map<BluetoothDevice, String> mScanResults;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        //Icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

        userType = (TextView) findViewById(R.id.userType); //Tipo de usuario
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar); //ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);

        //Inicialización del adaptador entre el arrayList y el ListView
        arrayList=new ArrayList<>();
        adapter = new ArrayAdapter<>(ClientActivity.this, android.R.layout.simple_expandable_list_item_1, arrayList);

        //Establecer el tipo de usuario escogido en el Main
        Intent i= getIntent();//Sacamos el intent con el que se inició la activity
        Bundle b =i.getExtras();//Del intent sacamos el bundle
        if (b != null) {
            String UsrType = b.getString("usr");// TextView a partir de la cadena de texto del Bundle.
            switch (UsrType) {
                case "1":
                    userType.setText(R.string.moto);
                    break;
                case "2":
                    userType.setText(R.string.car);
                    break;
                case "3":
                    userType.setText(R.string.walk);
                    break;
                default:
                    System.out.println("Error");
            }//switch
        }//if

        //Inicialización adaptador Bluetooth
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //Botones
        mapButton = (FloatingActionButton) findViewById(R.id.fab);
        mapButton.setFocusableInTouchMode(true);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        startButton= (Button) findViewById(R.id.start_scanning_button);
        startButton.setFocusableInTouchMode(true);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startScan();
                new ScanTask().execute();
            }
        });
        stopButton= (Button) findViewById(R.id.stop_scanning_button);
        stopButton.setFocusableInTouchMode(true);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });

        clearButton= (Button) findViewById(R.id.clear_scanning_button);
        clearButton.setFocusableInTouchMode(true);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearList();
            }
        });

    }//OnCreate

    protected void onResume() {
        super.onResume();

        //Chequeo de que el Advertising es compatible con el hardware del dispositivo
        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()){
            Log.d(ServerTAG, "No ad suppported.");
            finish();
            return;
        }
        Log.d(ServerTAG, "Ad suppported.");

        //Acceso al Advertiser
        mBluetoothLeAdvertiser=mBluetoothAdapter.getBluetoothLeAdvertiser();
        GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);
        setupServer();
        startAdvertising();

        //Muestra MAC local por pantalla
        /*String macAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
        serverAddres.setText(getString(R.string.serverAd, macAddress ));*/

    } //onResume
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
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();
        //Balanced Advertise para que sea rápidamente detectable pero no consuma tanta energía como Low Latency
        //Connectable true porque queremos pasar datos en ambos sentidos no como una baliza.
        //Timeout a 0 para anunciarse siempre
        //Low Power Level ya que estamos utilizando BLE

        ParcelUuid parcelUuid= new ParcelUuid(SERVICE_UUID);
        AdvertiseData data =new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                //.addServiceUuid(parcelUuid)
                .setIncludeTxPowerLevel(true)
                .build();
        //Incluir el nombre del dispositivo hace que sea más fácil de identificar el servidor

        mBluetoothLeAdvertiser.startAdvertising(settings,data,mAdvertiseCallback);

        //serverAddres.setText("Server Address: " + mDevices.toString());
    }//startAdvertising

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(ServerTAG, "Peripheral advertising started.");

        }

        @Override
        public void onStartFailure (int errorCode){
            Log.d(ServerTAG, "Peripheral advertising failed: " + errorCode);
        }
    };

    //Para gastar menos batería, el servidor para cuando esté en background
    /*protected void onPause(){
        super.onPause();
        stopAdvertising();
        stopServer();
    }*/

    private void stopServer(){
        if(mGattServer != null){
            mGattServer.close();
        }
        Log.d(ServerTAG, "Server stopped.");
    }


    private void stopAdvertising(){
        if(mBluetoothLeAdvertiser !=null){
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            Log.d(ServerTAG, "Advertising stopped.");
        }
    }


    //Chequeo encendido Bluetooth
    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        }
         else{
            return true;
        }
    }// hasPermissions

   //Petición encendido Bluetooth
    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(ClientTAG, "Requested user enables Bluetooth. Try starting the scan again.");
    }

    //Inicialización del escaneo
    public void startScan(){
        if (!hasPermissions() || mScanning) {
            return;
        }

        List<ScanFilter> filters = new ArrayList<>();
        //Añadimos filtro para el UUID del servicio de servidor Gatt
        /*ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        filters.add(scanFilter);*/
        ScanSettings settings =new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        //BleScanCallback para manejar los resutados y se añade un mapa para guardarlos
        mScanResults=new HashMap<>();
        mScanCallback = new BleScanCallback(mScanResults);

        //BluetoothLeScanner inicia el escaneo. Devuelve os resultados en la variable mScanCallback
        mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        Log.d(ClientTAG, "Started scanning.");
        mScanning = true;

    }//startScan

    //Parada del escaneo
    public void stopScan(){
        mProgressBar.setVisibility(View.INVISIBLE);
        scanComplete();

        //Se limpian variables relacionadas con el escaneo
        mScanCallback = null;
        mScanning = false;
        mHandler = null;

        Log.d(ClientTAG, "Stopped scanning.");

    }//stopScan

    public void scanComplete() {
    //Si hay, se saca por pantalla cada deviceAddres contenida en el mapa

        if (mScanResults.isEmpty()) {
            Log.d(ClientTAG, "No se encuentran dispositivos.");
            String mensaje= "No se encuentran dispositivos";
            arrayList.clear();
            arrayList.add(mensaje);
            adapter.notifyDataSetChanged();
            return;
        }

        for (BluetoothDevice device : mScanResults.keySet()) {
            Log.d(ClientTAG, "Found device: " + device);
            //deviceList.setText("Found device: " + deviceAddress);
            arrayList.clear();
            arrayList.add(device.getName() + "\n" + device.getAddress());
            adapter.notifyDataSetChanged();
        }

    }//scanComplete

    //Cuando se pulsa el botón Clear
    private void clearList(){
        deviceList.setAdapter(null);
        mScanResults.clear();
        arrayList.clear();
    }

    private class BleScanCallback extends ScanCallback {
        private Map<BluetoothDevice, String> mScanResults;

        BleScanCallback(Map<BluetoothDevice, String> scanResults) {
            mScanResults = scanResults;
        }
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(ClientTAG, "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(device, deviceAddress);
            arrayList.clear();
            arrayList.add(device.getName() + "\n" + device.getAddress());
            adapter.notifyDataSetChanged();
        }
    }//BleScanCallBack

    //Clase para añadir y quitar servicios de la lista basada en newState
    public class GattServerCallback extends BluetoothGattServerCallback {
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


   /* private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            super.onConnectionStateChange(gatt, status, newState);
            if(status == BluetoothGatt.GATT_FAILURE){
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS){
                disconnectGattServer();
                return;
            }
            if (newState== BluetoothProfile.STATE_CONNECTED){
                mConnected=true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                disconnectGattServer();
            }
        }
    }//GatClientCallback

    /* private void connectDevice(BluetoothDevice device) {
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(this, false, gattClientCallback);
    }*/


    /*public void disconnectGattServer(){
        mConnected =false;
        if(mGatt != null){
            mGatt.disconnect();
            mGatt.close();
        }
    } //disconnectGattServer*/

    public class ScanTask extends AsyncTask<Void, Void, ArrayList>
    {
        @Override
        protected  void onPreExecute(){
            mProgressBar.setVisibility(View.VISIBLE);
            deviceList= (ListView) findViewById(R.id.deviceList); //Lista de dispositivos
            deviceList.setAdapter(adapter);
        }

        @Override
        protected void onPostExecute(ArrayList s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            scanComplete();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //progressDialog.incrementProgressBy(1);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... args) {
            try{
                startScan();
                Thread.sleep(300000);
                publishProgress();

            }catch (Exception e){
                e.printStackTrace();
            }
            return arrayList;
        }

    }//ScanTask

}//ClientActivity
