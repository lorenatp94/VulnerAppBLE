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
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private TextView userType;
    private TextView deviceList;
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
        deviceList= (TextView) findViewById(R.id.deviceList);
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


            // Establecemos el texto del TextView a partir de la cadena de texto
            // que hemos sacado del Bundle.


            // Se puede hacer la asignación directamente:
            //mostrarDatos.setText(getIntent().getExtras().getString("datos"));
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        mapButton = (FloatingActionButton) findViewById(R.id.fab);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        startButton= (Button) findViewById(R.id.start_scanning_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        stopButton= (Button) findViewById(R.id.stop_scanning_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });

        clearButton= (Button) findViewById(R.id.clear_scanning_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }//OnCreate

    private void startScan(){
        if (!hasPermissions() || mScanning) {
            return;
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings =new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();
    //ScanCallback para manejar los resutados y se añade un mapa para guardarlos

        mScanResults=new HashMap<>();
        mScanCallback = new BleScanCallback(mScanResults);
        //BluetoothLeScanner inicia el escaneo y pone el booleano a true
        mBluetoothLeScanner  = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mScanning = true;
        //Handler para parar el escaneo tras un tiempo en milisegundos
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                // Tras el SCAN_PERIOD se ejecuta stopScan para que no esté escaneando infinitamente.
                stopScan();
            }
        }, SCAN_PERIOD);

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
   /* private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }*/


    private void stopScan(){
        //Para detener el escaneo usamos el mismo ScanCallback de antes.  Ahora es buen momento para limpiar cualquier variable relacionada con el escaneo.
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;
    }//stopScan

    private void scanComplete() {
        //Realizará cualquier acción usando los resultados
        //Por ahora simlemente se desconectará de los dispositivos encontrados durante el escaneo

        if (mScanResults.isEmpty()) {
            return;
        }
        for (String deviceAddress : mScanResults.keySet()) {
            //Se saca por pantalla cada devideAddres contenida en el mapa
            Log.d(TAG, "Found device: " + deviceAddress);
            //deviceList.setText("Found device: " + deviceAddress);
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
            deviceList.setText("Found device: " + deviceAddress + "\r\n");
        }
    }//BleScanCallBack


}
