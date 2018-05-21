package com.navigation.vibration;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng placeLatLng;
    private String placeAddress;
    private String placeId;
    private String placeName;
    private Location currentLocation = null;

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(final Location location) {
            if(location != null) {
                Log.d("DEBUG", "Updating location\n" + location.toString());
                currentLocation = location;
                FloatingActionButton fab = findViewById(R.id.maps_floating_action_button);
                fab.setEnabled(true);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast toast = Toast.makeText(getApplicationContext(), "GPS available", Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast toast = Toast.makeText(getApplicationContext(), "GPS not available", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Still no permissions", Toast.LENGTH_SHORT);
            toast.show();

            //permissions check
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
            }
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(currentLocation == null){
            FloatingActionButton fab = findViewById(R.id.maps_floating_action_button);
            fab.setEnabled(false);
            Toast toast = Toast.makeText(getApplicationContext(), "Waiting for your location", Toast.LENGTH_SHORT);
            toast.show();
        }
        Log.d("Info", "Last known location: " + locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        Intent intent = getIntent();

        placeLatLng = intent.getParcelableExtra("place_latlng");
        placeAddress = (String) intent.getSerializableExtra("place_address");
        placeId = (String) intent.getSerializableExtra("place_id");
        placeName = (String) intent.getSerializableExtra("place_name");

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
        // Add a marker in Sydney and move the camera
        LatLng placeCoordinates = placeLatLng;
        mMap.addMarker(new MarkerOptions().position(placeCoordinates).title(placeName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeCoordinates, 12));
    }

    public void findRoute(final View view) throws MalformedURLException {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        URL url = new URL(String.format("https://api.openrouteservice.org/directions?api_key=58d904a497c67e00015b45fc279fc62181024e75b2771aff9a578caa&coordinates=%s,%s|%s,%s&profile=cycling-road&preference=fastest&format=json&units=m&language=en&instructions=true&instructions_format=text&roundabout_exits=true&maneuvers=true&optimized=true&&geometry=true&geometry_format=encodedpolyline",placeLatLng.longitude,placeLatLng.latitude, currentLocation.getLongitude(), currentLocation.getLatitude()));

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("INFO", "GOT THE JSON REPSONSE");
                        Intent intent = new Intent(MapsActivity.this, NavigationActivity.class);
                        intent.putExtra("response", response);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // todo : do something
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

}
