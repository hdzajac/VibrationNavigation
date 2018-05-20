package com.navigation.vibration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.io.Serializable;

public class PlaceSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selection);
        initListener();
    }

    public void initListener(){
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("DEBUG", "Place: " + place.getName());
                Intent intent = new Intent(PlaceSelectionActivity.this, MapsActivity.class);
                intent.putExtra("place_latlng",  place.getLatLng());
                intent.putExtra("place_address",  place.getAddress());
                intent.putExtra("place_id",  place.getId());
                intent.putExtra("place_name",  place.getName());
                startActivity(intent);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("ERROR", "An error occurred: " + status);
            }
        });
    }
}
