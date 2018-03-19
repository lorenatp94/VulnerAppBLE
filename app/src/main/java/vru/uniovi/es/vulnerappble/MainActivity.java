package vru.uniovi.es.vulnerappble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView motoCard, carCard, pedesCard;
    private Button startButton;
    public int b;
    private BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Definición de las cards y el boton
        motoCard=(CardView) findViewById(R.id.moto_card);
        carCard=(CardView) findViewById(R.id.car_card);
        pedesCard=(CardView) findViewById(R.id.pedes_card);
        startButton= (Button) findViewById(R.id.start_button);

        //Se añade función al boton y a las card
        motoCard.setOnClickListener(this);
        carCard.setOnClickListener(this);
        pedesCard.setOnClickListener(this);
        startButton.setOnClickListener(this);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 5);
        }


     }

    @Override
    public void onClick(View w){
        //objeto Bunlde que incluye el par "Clave/Valor"
        //la clave será "user" y el texto dependerá de la opción escogida

        /*switch (w.getId()){
            case R.id.moto_card: b.putString("user", "moto");break;
            case R.id.car_card:b.putString("user", "car");break;
            case R.id.pedes_card:b.putString("user", "car");break;
            default: break;
        }*/
        switch (w.getId()) {
            case R.id.moto_card:
                b = 1;
                carCard.setBackgroundColor(Color.WHITE);
                pedesCard.setBackgroundColor(Color.WHITE);
                motoCard.setBackgroundColor(Color.GRAY);
                break;
            case R.id.car_card:
                b = 2;
                motoCard.setBackgroundColor(Color.WHITE);
                pedesCard.setBackgroundColor(Color.WHITE);
                carCard.setBackgroundColor(Color.GRAY);
                break;
            case R.id.pedes_card:
                b = 3;
                carCard.setBackgroundColor(Color.WHITE);
                motoCard.setBackgroundColor(Color.WHITE);
                pedesCard.setBackgroundColor(Color.GRAY);
                break;
            case R.id.start_button:
                if (b == 0) {
                    Snackbar.make(w, "Please choose an option", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    b=0;
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);

                }
            default:
                break;
        }

    }
    public void set_backgroundcard(View v){

    }

}
