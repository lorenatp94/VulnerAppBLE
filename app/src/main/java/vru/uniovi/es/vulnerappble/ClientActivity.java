package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;




public class ClientActivity extends AppCompatActivity {

    private static final String ClientTAG = "Client";
    private static final String ServerTAG = "Server";
    private static final int REQUEST_ENABLE_BT = 1;

    //public static String SERVICE_STRING = "5377e081-74a8-4e92-86c1-ec474ec11d61";
    public static String SERVICE_STRING = "00001811-0000-1000-8000-0080F9B34FB";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);
    public static UUID SERVICE_UUID2 = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c");


    private TextView userType;
    public String UsrType;
    private ListView deviceList;
    public ArrayList<Device> arrayList;
    public ArrayAdapter adapter;
    private FloatingActionButton mapButton;
    private Button startButton, stopButton, clearButton;
    private boolean mScanning;

    public  Map<BluetoothDevice, String> mScanResults;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private ProgressBar mProgressBar;
    private Timer timer;
    private TimerTask task;

    private static boolean run=true;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        //Icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

        userType = (TextView) findViewById(R.id.userType); //Tipo de usuario
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar); //ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE); //ProgressBar invisibe hasta que no se empiece a escanear

        //Inicialización del adaptador entre el arrayList y el ListView
        arrayList=new ArrayList<>();
        adapter=new DeviceAdapter(this, R.layout.list_item, arrayList);

        mScanResults=new HashMap<>();
        mScanCallback = new BleScanCallback(mScanResults);
        deviceList= (ListView) findViewById(R.id.deviceList); //Lista de dispositivos
        deviceList.setAdapter(adapter);

        //Establecer el tipo de usuario escogido en el Main
        Intent i= getIntent();//Sacamos el intent con el que se inició la activity
        Bundle b =i.getExtras();//Del intent sacamos el bundle
        if (b != null) {
            UsrType = b.getString("usr");// TextView a partir de la cadena de texto del Bundle.
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
        Handler handler=new Handler();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        stopButton= (Button) findViewById(R.id.stop_scanning_button);
        stopButton.setFocusableInTouchMode(true);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.INVISIBLE);

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

    public void onResume() {
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
    } //onResume

    //Se añade el Servicio al Servidor
    //SERVICE_UUID es un identificador unico que asegura que se está conectando al servidor Gatt correcto.
    private void setupServer(){
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Counter characteristic (read-only, supports subscriptions)
       /* BluetoothGattCharacteristic counter = new BluetoothGattCharacteristic(CHARACTERISTIC_COUNTER_UUID, PROPERTY_READ | PROPERTY_NOTIFY, PERMISSION_READ);
        BluetoothGattDescriptor counterConfig = new BluetoothGattDescriptor(DESCRIPTOR_CONFIG_UUID, PERMISSION_READ | PERMISSION_WRITE);
        counter.addDescriptor(counterConfig);*/

        //service.addCharacteristic(counter);
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
        //High Level para mayor rango de visibilidad del paquete publicitario
        byte[] userData;
        ParcelUuid parcelUuid= new ParcelUuid(SERVICE_UUID2);
        /*try{
            userData = UsrType.getBytes("UTF-8");
        }catch(UnsupportedEncodingException e){ }*/
        userData = UsrType.getBytes();

        AdvertiseData data =new AdvertiseData.Builder()
                .addManufacturerData(65535, userData)
               // .addServiceData(parcelUuid,userData)
                .setIncludeTxPowerLevel(true) //Se incluye el nivel de transmision para luego calcular la posicion
                .build();

        AdvertiseData scanResponse =new AdvertiseData.Builder()
                .setIncludeDeviceName(true)  //Incluir el nombre del dispositivo hace que sea más fácil de identificar el servidor
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings,data, scanResponse, mAdvertiseCallback);
    }//startAdvertising

    //Callback del Advertising
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
        mProgressBar.setVisibility(View.VISIBLE);

        mScanResults.clear();
        arrayList.clear();
        adapter.notifyDataSetChanged();
        List<ScanFilter> filters = new ArrayList<>();
        //Añadimos filtro para el UUID del servicio de servidor Gatt
       /* ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID2))
                .build();
        filters.add(scanFilter);*/
        ScanSettings settings =new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();



        //BleScanCallback para manejar los resutados y se añade un mapa para guardarlos



        //BluetoothLeScanner inicia el escaneo. Devuelve os resultados en la variable mScanCallback
        mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        Log.d(ClientTAG, "Started scanning.");
        mScanning = true;

    }//startScan

    //Callback del escaneo
    public class BleScanCallback extends ScanCallback {
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

    }//BleScanCallBack

    public void addScanResult(ScanResult result) {

        BluetoothDevice device = result.getDevice();

        ScanRecord scanRecord = result.getScanRecord();
        String manufacturedDataStr;
        //byte[] manufacturerData;
        byte[] manufacturerData = scanRecord.getManufacturerSpecificData(65535);
        manufacturedDataStr= new String(manufacturerData);

        String deviceAddress=device.getAddress();
        String deviceName=device.getName();
        int rssi=result.getRssi();
        Device disp = new Device(deviceName, deviceAddress,manufacturedDataStr , rssi);


        if (!mScanResults.containsKey(device)){
            mScanResults.put(device, deviceAddress);
            arrayList.add(disp);
            Log.d(ClientTAG, "Device: "+ deviceName+ " RSSI: "+ result.getRssi());
            Log.d(ClientTAG, "Usertype: "+ manufacturedDataStr);

            adapter.notifyDataSetChanged();
        }
    }//addScanResult


    //Parada del escaneo
    public void stopScan(){

        //scanComplete();


        //Se limpian variables relacionadas con el escaneo
        //mScanCallback = null;
        mScanning = false;
        mBluetoothLeScanner.stopScan(mScanCallback);
        Log.d(ClientTAG, "Stopped scanning.");

    }//stopScan

    public void scanComplete() {
    //Si no hay dispositivos se saca mensaje informando
        if (mScanResults.isEmpty()) {
            Log.d(ClientTAG, "Devices not found.");
            String mensaje= "Devices not found";
            arrayList.clear();
            //arrayList.add(mensaje);
            adapter.notifyDataSetChanged();
        }
    }//scanComplete

    //Cuando se pulsa el botón Clear
    private void clearList(){
        mScanResults.clear();
        arrayList.clear();
        adapter.notifyDataSetChanged();
    }

}//ClientActivity
