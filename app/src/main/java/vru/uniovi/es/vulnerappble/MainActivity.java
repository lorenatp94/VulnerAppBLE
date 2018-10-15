package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "MainActivity";

    private CardView motoCard, carCard, pedesCard;
    private Button nextButton;
    private BluetoothAdapter mBluetoothAdapter;
    public String UsrType;
    public LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

        //Definición de las cards y el boton
        motoCard=(CardView) findViewById(R.id.moto_card);
        carCard=(CardView) findViewById(R.id.car_card);
        pedesCard=(CardView) findViewById(R.id.pedes_card);
        nextButton = (Button) findViewById(R.id.next_button);

        //Se añade función al boton y a las card
        motoCard.setOnTouchListener(this);
        carCard.setOnTouchListener(this);
        pedesCard.setOnTouchListener(this);
        nextButton.setOnTouchListener(this);


     }

    @Override
    public void onResume(){
        super.onResume();
        //Comprobación de que el terminal soporta BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "No LE support.");
            finish();
        }
        // Inicialización adaptador Bluetooth
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Comprobación de que Bluetooth está encendido. Si no,
        // muestra un cuadro de diálogo pidiendo al usuario que lo encienda.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 5);
        }

        //Comprobación de que la localización está encendida. Si no,
        //abre el menú de configuración para activarla.
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }

        UsrType= "0";

    }//onResume


    @Override
    public boolean onTouch(View w, MotionEvent event) {
        switch (w.getId()) {
            case R.id.moto_card:
                UsrType = "1";
                break;

            case R.id.car_card:
                UsrType = "2";
                break;

            case R.id.pedes_card:
                UsrType= "3";
                break;

            case R.id.next_button:
                nextButton.requestFocusFromTouch();
                if (UsrType.equals("0")) {
                    Snackbar.make(w, "Please choose an option", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
                    Snackbar.make(w, "Please turn on the location service", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }else {

                    // Utilizamos un objeto de la clase Bundle para incluir un par
                    // "Clave/Valor", este objeto tendrá como clave "datos", y su valor
                    // será el tipo de usuario
                    Bundle b = new Bundle();
                    b.putString("usr", UsrType.toString());
                    UsrType="0"; //Para cuando se vueva al menú

                    // La clase Intent establece un link entre  MainActivity y la siguiente Activity
                    // que queremos lanzar, para ello al instanciar el Intent
                    // introducimos como parámetros esta propia Activity, y la clase que
                    // representa a la nueva Activity.
                    Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                    intent.putExtras(b);
                    startActivity(intent);
                }
        }

        return false;
    }

    private void AlertNoGps() {
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("To continue, turn on device location")
                .setCancelable(false)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }
}//MainActivity
