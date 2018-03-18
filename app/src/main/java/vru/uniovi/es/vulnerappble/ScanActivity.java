package vru.uniovi.es.vulnerappble;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ScanActivity extends AppCompatActivity {
private TextView mostrarDatos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mostrarDatos = (TextView) findViewById(R.id.mostrarDatos);
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
            String datos = b.getString("datos");

            // Establecemos el texto del TextView a partir de la cadena de texto
            // que hemos sacado del Bundle.
            mostrarDatos.setText(datos);

            // Se puede hacer la asignación directamente:
            mostrarDatos.setText(getIntent().getExtras().getString("datos"));
        }

    }
}
