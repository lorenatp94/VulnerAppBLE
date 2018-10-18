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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;




public class ClientActivity extends AppCompatActivity {

    public static final String ClientTAG = "Client";
    private static final String ServerTAG = "Server";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MANUFACTURED_ID = 65535;
    public static final String MOTO= "1";
    public static final String CAR= "2";
    public static final String PED= "3";

    //public static String SERVICE_STRING = "00001811-0000-1000-8000-0080F9B34FB";
    public static String SERVICE_STRING = "795090c7-420d-4048-a24e-18e60180e23c";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);



    private TextView userType;
    public  static TextView total;
    public String UsrType;
    private ListView deviceList;
    public static ArrayList<Device> arrayList;
    public static ArrayAdapter adapter;
    private Button startButton, stopButton, clearButton;
    private boolean mScanning;

    public  static Map<Device, String> mScanResults;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private ProgressBar mProgressBar;
    private Runnable StartTask;
    private static byte[] manufacturerData;
    private static String manufacturedDataStr;
    private boolean run;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);


        //Icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

        userType = (TextView) findViewById(R.id.userType); //Para mostrar el tipo de usuario escogido en el Main
        total=(TextView)findViewById(R.id.total);//Para mostrar el número de dispositivos encontrados

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar); //ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE); //ProgressBar invisibe hasta que no se empiece a escanear

        //Inicialización del adaptador entre el arrayList y el ListView
        arrayList=new ArrayList<>();
        adapter=new DeviceAdapter(this, R.layout.list_item, arrayList);

        mScanResults=new HashMap<>();
        mScanCallback = new BleScanCallback();
        deviceList= (ListView) findViewById(R.id.deviceList); //Lista de dispositivos
        deviceList.setAdapter(adapter);

        //Inicialización adaptador Bluetooth
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        String deviceUsrName= mBluetoothAdapter.getName();


        //Establecer el tipo de usuario escogido en el Main
        Intent intentClient= getIntent();//Sacamos el intent con el que se inició la activity
        Bundle bundleClient =intentClient.getExtras();//Del intent sacamos el bundle
        if (bundleClient != null) {
            UsrType = bundleClient.getString("usr");// TextView a partir de la cadena de texto del Bundle.
            switch (UsrType) {
                case MOTO:
                    userType.setText("User Type: Motorist/Cyclist"+'\n'+"Name: "+ deviceUsrName);
                    //userType.setText(getString(R.string.userDisplay, R.string.moto, deviceUsrName));
                    break;
                case CAR:
                    userType.setText("User type: Car"+'\n'+"Name: "+ deviceUsrName);

                    break;
                case PED:
                    userType.setText("User type: Pedestrian"+'\n'+"Name: "+ deviceUsrName);

                    break;
                default:
                    System.out.println("Error");
            }//switch

        }//if


        //Botones

        startButton= (Button) findViewById(R.id.start_scanning_button);
        startButton.setFocusableInTouchMode(true);
        final Handler handler=new Handler();

        //Tarea que realiza un escaneo nuevo cada 7 segundos para actualizar la lista de dispositivos encontrados
        StartTask = new Runnable(){

            @Override
            public void run() {
                if(run){
                    stopScan();
                    startScan();
                    handler.postDelayed(StartTask,7000);
                }
            }
        };//StartTask

        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                run=true;
                startScan();
                handler.post(StartTask);
                return false;
            }
        });

        stopButton= (Button) findViewById(R.id.stop_scanning_button);
        stopButton.setFocusableInTouchMode(true);
        stopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mProgressBar.setVisibility(View.INVISIBLE); //Se quita la ProgressBar cuando el escaneo está parado
                run=false; //Se para la StartTask
                handler.removeCallbacksAndMessages(StartTask);//Eliminación de todos los callbacks y mensajes de StartTask
                stopScan();
                scanComplete();
                return false;
            }
        });

        clearButton= (Button) findViewById(R.id.clear_scanning_button);
        clearButton.setFocusableInTouchMode(true);
        clearButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearList();
                return false;
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
        //setupServer();
        //stopAdvertising();
        startAdvertising();
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
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();
        //Balanced Advertise para que sea rápidamente detectable pero no consuma tanta energía como Low Latency.
        //Connectable false el dispositivo solo va a anunciarse, no necesita establecer conexiones.
        //Timeout a 0 para anunciarse siempre.
        //High Level para mayor rango de visibilidad del paquete de Advertising.

        byte[] userData;
        //ParcelUuid parcelUuid= new ParcelUuid(SERVICE_UUID);
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
        if(mBluetoothLeAdvertiser!=null){
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            Log.d(ServerTAG, "Advertising stopped.");
        }
    }

    @Override
    public void onBackPressed(){
        stopAdvertising();
        Intent intent = new Intent(ClientActivity.this, MainActivity.class);
        startActivity(intent);
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
        mProgressBar.setVisibility(View.VISIBLE); //ProgressBar visible

        mScanResults.clear();
        arrayList.clear();
        total.setText(null);
        adapter.notifyDataSetChanged();
        List<ScanFilter> filters = new ArrayList<>();
        //Añadimos filtro para el UUID del servicio de servidor Gatt
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        //filters.add(scanFilter);
        ScanSettings settings =new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();



        //BluetoothLeScanner inicia el escaneo. Devuelve los resultados en mScanCallback
        mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        Log.d(ClientTAG, "Started scanning.");
        mScanning = true;

    }//startScan


    public static void addScanResult(ScanResult result) {

        BluetoothDevice device = result.getDevice();
        ScanRecord scanRecord = result.getScanRecord();
        manufacturedDataStr="";
        manufacturerData=null;
        try{
            manufacturerData = scanRecord.getManufacturerSpecificData(MANUFACTURED_ID);
            manufacturedDataStr= new String(manufacturerData);
        }catch (Exception e){
            return;
        }

        String deviceAddress=device.getAddress();
        String deviceName=device.getName();
        int rssi=result.getRssi();
        Device disp = new Device(deviceName, deviceAddress,manufacturedDataStr , rssi);


        if (!mScanResults.containsValue(deviceAddress)){
            mScanResults.put(disp, deviceAddress);
            arrayList.add(disp);
            Log.d(ClientTAG, "Device: "+ deviceName+ " RSSI: "+ result.getRssi());
            Log.d(ClientTAG, "Usertype: "+ manufacturedDataStr);
            adapter.notifyDataSetChanged();
            total.setText("Found users: "+ mScanResults.size());
        }
    }//addScanResult


    //Parada del escaneo
    public void stopScan(){
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
            Toast toast =Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0,0);
            toast.show();
            arrayList.clear();
            adapter.notifyDataSetChanged();
        }
    }//scanComplete


    private void clearList(){
        //Función del botón Clear
        mScanResults.clear();
        arrayList.clear();
        adapter.notifyDataSetChanged();
        total.setText(null);
    }

}//ClientActivity
