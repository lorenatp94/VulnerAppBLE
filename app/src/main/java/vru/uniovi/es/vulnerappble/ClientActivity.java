package vru.uniovi.es.vulnerappble;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
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

    private static final String TAG = "ClientActivity";
    public static String SERVICE_STRING = "5377e081-74a8-4e92-86c1-ec474ec11d61";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);

    private TextView userType;
    private ListView deviceList;
    private TextView mProgressText;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    private FloatingActionButton mapButton;
    private Button startButton, stopButton, clearButton;

    int count=1;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    public static final long SCAN_PERIOD = 50000;

    private boolean mScanning;
    private Handler mHandler;
    private Handler mLogHandler;
    private Map<BluetoothDevice, String> mScanResults;

    private boolean mConnected;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;
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


        arrayList=new ArrayList<String>();
        adapter = new ArrayAdapter<String>(ClientActivity.this, android.R.layout.simple_expandable_list_item_1, arrayList);

        //Sacamos el intent con el que se inició la activity
        Intent i= getIntent();
        //Del intent sacamos el bundle
        Bundle b =i.getExtras();
        // Comprobamos que el Bundle contenga datos, para evitar posibles
        // errores. Si no lo comprobamos y el Intent no tiene incorporado un
        // bundle, al intentar utilizar el bundle después nos saltará una
        // excepción por intentar un objeto que no existe
        // (NullPointerException).
        if (b != null) {
            String UsrType = b.getString("usr");
            // Establecemos el texto del TextView a partir de la cadena de texto
            // que hemos sacado del Bundle.
            switch(UsrType){
                case "1":
                    userType.setText("Motorist/Cyclist user");
                    break;
                case "2":
                    userType.setText("Car driver user");
                    break;
                case "3":
                    userType.setText("Pedestrian user");
                    break;
                default:
                    System.out.println("Error");
            }

            // Se puede hacer la asignación directamente:
            //mostrarDatos.setText(getIntent().getExtras().getString("datos"));

            //Inicialización adaptador Bluetooth
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

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

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        }
         else{
            return true;
        }
    }// hasPermissions

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth. Try starting the scan again.");

    }


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
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

        //BtleScanCallback para manejar los resutados y se añade un mapa para guardarlos

        mScanResults=new HashMap<>();
        mScanCallback = new BleScanCallback(mScanResults);

        //BluetoothLeScanner inicia el escaneo
        mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        //mBluetoothLeScanner.startScan(mScanCallback);

        Log.d(TAG, "Started scanning.");
        //Handler para parar el escaneo tras un tiempo en milisegundos
        mScanning = true;
       /* mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                // Tras el SCAN_PERIOD se ejecuta stopScan para que no esté escaneando infinitamente.
                stopScan();
            }
        }, SCAN_PERIOD);*/

    }//startScan




    public void stopScan(){
        //Para detener el escaneo usamos el mismo ScanCallback de antes.
        // Se limpian variables relacionadas con el escaneo
        mProgressBar.setVisibility(View.INVISIBLE);
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;

        Log.d(TAG, "Stopped scanning.");

    }//stopScan

    public void scanComplete() {
        //Realizará cualquier acción usando los resultados
        //Por ahora simlemente se desconectará de los dispositivos encontrados durante el escaneo



        if (mScanResults.isEmpty()) {
            Log.d(TAG, "No se encuentran dispositivos.");
            String mensaje= "No se encuentran dispositivos";
            arrayList.clear();
            arrayList.add(mensaje);
            adapter.notifyDataSetChanged();

            return;
        }
        for (BluetoothDevice device : mScanResults.keySet()) {
            //Se saca por pantalla cada devideAddres contenida en el mapa
            Log.d(TAG, "Found device: " + device);
            //deviceList.setText("Found device: " + deviceAddress);
            arrayList.clear();
            arrayList.add(device.getName() + "\n" + device.getAddress());
            adapter.notifyDataSetChanged();

        }
    }//scanComplete



    private void clearList(){
        deviceList.setAdapter(null);
        mScanResults.clear();
        arrayList.clear();
    }

    private void connectDevice(BluetoothDevice device) {
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(this, false, gattClientCallback);
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
            Log.e(TAG, "BLE Scan Failed with code " + errorCode);
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


    private class GattClientCallback extends BluetoothGattCallback {
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

    public void disconnectGattServer(){
        mConnected =false;
        if(mGatt != null){
            mGatt.disconnect();
            mGatt.close();
        }
    } //disconnectGattServer

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
            //arrayList=new ArrayList<String>();
            //adapter = new ArrayAdapter<String>(ClientActivity.this, android.R.layout.simple_expandable_list_item_1, arrayList);
            scanComplete();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //progressDialog.incrementProgressBy(1);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... args) {

            try{
                //startScan();

                List<ScanFilter> filters = new ArrayList<>();
                //Añadimos filtro para el UUID del servicio de servidor Gatt
        /*ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        filters.add(scanFilter);*/
                ScanSettings settings =new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

                //BtleScanCallback para manejar los resutados y se añade un mapa para guardarlos

                mScanResults=new HashMap<>();
                mScanCallback = new BleScanCallback(mScanResults);

                //BluetoothLeScanner inicia el escaneo
                mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
                mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
                //mBluetoothLeScanner.startScan(mScanCallback);

                Log.d(TAG, "Started scanning.");
                //Handler para parar el escaneo tras un tiempo en milisegundos
                mScanning = true;
                Thread.sleep(300000);
                publishProgress();



            }catch (Exception e){
                e.printStackTrace();
            }
            return arrayList;
        }



        protected void onProgressUpdate(Float... percent){
            //int p =Math.round(100*percent[0]);
           // mProgressText.setText(""+percent[0]);
            //mProgressBar.setProgress((float) percent[0]);
        }


    }//ScanTask

}
