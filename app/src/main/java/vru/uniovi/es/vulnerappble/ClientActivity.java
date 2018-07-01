package vru.uniovi.es.vulnerappble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";

    private TextView userType;
    private ListView deviceList;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    private FloatingActionButton mapButton;
    private Button startButton, stopButton, clearButton;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private boolean mScanning;
    private Handler mHandler;
    private Handler mLogHandler;
    private Map<String, BluetoothDevice> mScanResults;

    private boolean mConnected;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;

    public static final long SCAN_PERIOD = 100000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        //Icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

        userType = (TextView) findViewById(R.id.userType);
        deviceList= (ListView) findViewById(R.id.deviceList);

        arrayList=new ArrayList<String>();
        adapter = new ArrayAdapter<String>(ClientActivity.this, android.R.layout.simple_expandable_list_item_1,arrayList);

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
                    userType.setText("Usuario motorista");
                    break;
                case "2":
                    userType.setText("Usuario en coche");
                    break;
                case "3":
                    userType.setText("Usuario peatón");
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
                startScan();
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

    private void startScan(){
        if (!hasPermissions() || mScanning) {
            return;
        }

        deviceList.setAdapter(adapter);

        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings =new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();
    //ScanCallback para manejar los resutados y se añade un mapa para guardarlos

        mScanResults=new HashMap<>();
        mScanCallback = new BleScanCallback(mScanResults);
        //BluetoothLeScanner inicia el escaneo y pone el booleano a true
        mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
        //mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mBluetoothLeScanner.startScan(mScanCallback);
        //Handler para parar el escaneo tras un tiempo en milisegundos
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                // Tras el SCAN_PERIOD se ejecuta stopScan para que no esté escaneando infinitamente.
                stopScan();
            }
        }, SCAN_PERIOD);
        mScanning = true;mScanning = true;
        Log.d(TAG, "Started scanning.");


    }//startScan

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else /*if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }*/
        return true;
    }
    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth. Try starting the scan again.");

    }



    private void stopScan(){
        //Para detener el escaneo usamos el mismo ScanCallback de antes.  Ahora es buen momento para limpiar cualquier variable relacionada con el escaneo.
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;

        Log.d(TAG, "Stopped scanning.");
    }//stopScan

    private void clearList(){
        deviceList.setAdapter(null);
        mScanResults.clear();
        arrayList.clear();
    }

    private void scanComplete() {
        //Realizará cualquier acción usando los resultados
        //Por ahora simlemente se desconectará de los dispositivos encontrados durante el escaneo

        if (mScanResults.isEmpty()) {
            Log.d(TAG, "No se encuentran dispositivos.");
            String mensaje= "No se encuentran dispositivos";
            arrayList.add(mensaje);
            adapter.notifyDataSetChanged();

            return;
        }
        for (String deviceAddress : mScanResults.keySet()) {
            //Se saca por pantalla cada devideAddres contenida en el mapa
            Log.d(TAG, "Found device: " + deviceAddress);
            //deviceList.setText("Found device: " + deviceAddress);
            arrayList.add(deviceAddress);
            adapter.notifyDataSetChanged();

        }
    }//scanComplete

    private class BleScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> mScanResults;

        BleScanCallback(Map<String, BluetoothDevice> scanResults) {
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
            mScanResults.put(deviceAddress, device);


        }
    }//BleScanCallBack


}
