package vru.uniovi.es.vulnerappble;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String MapsTAG = "Maps";
    private Marker marker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        }


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Se debe adquirir una referencia al Location Manager del sistema
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        // Se crea un listener de la clase que se va a definir luego
        MyLocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Se registra el listener con el Location Manager para recibir actualizaciones
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 2, locationListener);


            try {
                //Se obtiene la posición
                Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (location != null) {
                    Log.d(MapsTAG, "Location no null.");
                    double lati = location.getLatitude();
                    double longi = location.getLongitude();
                    LatLng myPosition = new LatLng(lati, longi);
                    Log.d(MapsTAG, "Lat: "+ lati + "Long: " + longi);
                    marker=mMap.addMarker(new MarkerOptions().position(myPosition).title("My Position"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 20));
                } else {
                    Log.d(MapsTAG, "Location null.");
                }

            } catch (Exception e) {
            }

        }

            // Add a marker in my position and move the camera

    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location)
        {
            double lati = location.getLatitude();
            double longi = location.getLongitude();
            LatLng myPosition = new LatLng(lati, longi);
            marker.setPosition(myPosition);
           // mMap.addMarker(new MarkerOptions().position(myPosition).title("My Position"));
           // mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        }

        // Se llama cuando cambia el estado
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        // Se llama cuando se activa el provider
        @Override
        public void onProviderEnabled(String provider) {}

        // Se llama cuando se desactiva el provider
        @Override
        public void onProviderDisabled(String provider) {}

    }

}
