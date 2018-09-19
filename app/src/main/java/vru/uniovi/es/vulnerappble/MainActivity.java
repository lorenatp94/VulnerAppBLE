package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private CardView motoCard, carCard, pedesCard;
    private Button nextButton, connectButton, scanButton;
    private BluetoothAdapter mBluetoothAdapter;
    public String UsrType;

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
        connectButton= (Button) findViewById(R.id.connect_button);


        //Se añade función al boton y a las card
        motoCard.setOnClickListener(this);
        carCard.setOnClickListener(this);
        pedesCard.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);



        carCard.setFocusableInTouchMode(true);
        pedesCard.setFocusableInTouchMode(true);
        motoCard.setFocusableInTouchMode(true);



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
        UsrType= "0";
    }//onResume

    @Override
    public void onClick(View w){
        //objeto Bunlde que incluye el par "Clave/Valor"
        //la clave será "user" y el texto dependerá de la opción escogida


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

            case R.id.connect_button:
                Intent intent_connect = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(intent_connect);
                break;

            case R.id.next_button:
                //Intent intentserv = new Intent(MainActivity.this, ServerActivity.class);
                if (UsrType.equals("0")) {
                    Snackbar.make(w, "Please choose an option", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {

                    // Utilizamos un objeto de la clase Bundle para incluir un par
                    // "Clave/Valor", este objeto tendrá como clave "datos", y su valor
                    // será el tipo de usuario
                    Bundle b = new Bundle();
                    b.putString("usr", UsrType.toString());
                    UsrType="0"; //Para cuando se vueva al menú

                    // La clase Intent establece un link entre esta Activity y la nueva
                    // que queremos lanzar, para ello al instanciar el Intent
                    // introducimos como parámetros esta propia Activity, y la clase que
                    // representa a la nueva Activity.
                    Intent intent = new Intent(MainActivity.this, ClientActivity.class);

                    intent.putExtras(b);
                    startActivity(intent);

                }



        }

    }//onClick

}
