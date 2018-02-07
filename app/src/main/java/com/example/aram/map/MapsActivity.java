package com.example.aram.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private GoogleMap map;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private ArrayList<LatLng> markersList;
    private Button btnCalculate;
    private Button btnReset;
    private TextView tvResult;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snakBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnReset = findViewById(R.id.btn_reset);
        tvResult = findViewById(R.id.tv_result);
        btnCalculate.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        markersList = new ArrayList<LatLng>();
        if (!checkPermission()){
            requestPermission();
        }else {
            checkLocationState(false);
        }


    }



    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new
                String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (checkPermission()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (checkPermission()) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void checkLocationState (boolean isFirst) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!isFirst){
            if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                snakBar = Snackbar.make(coordinatorLayout, "Please Turn On Location!", Snackbar.LENGTH_INDEFINITE);
                snakBar.show();
            }
        }
        lm.addGpsStatusListener(new android.location.GpsStatus.Listener()
        {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GPS_EVENT_STARTED: {
                        if(snakBar!=null){
                            snakBar.dismiss();
                        }
                    }

                    break;
                    case GPS_EVENT_STOPPED: {
                        snakBar = Snackbar.make(coordinatorLayout, "Please Turn On Location!", Snackbar.LENGTH_INDEFINITE);
                        snakBar.show();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationState(true);
                } else {
                    requestPermission();
                }
            }
        }
        updateLocationUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(MapsActivity.this);
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(markersList.size()!=2){
            map.addMarker(new MarkerOptions().position(latLng));
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            markersList.add(latLng);
        } else {
            Toast.makeText(this, "It has two markers selected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btn_calculate:{
                if(markersList.size()==2){
                    float [] resault = new float[1];
                    Location.distanceBetween(markersList.get(0).latitude,markersList.get(0).longitude,markersList.get(1).latitude,markersList.get(1).longitude,resault);
                    String resaultString = String.format("%.4f", resault[0]/1000);
                    tvResult.setText(resaultString+" Km");
                     map.addPolyline(new PolylineOptions()
                            .add(markersList.get(0), markersList.get(1))
                            .width(5)
                            .color(Color.MAGENTA));
                } else {
                    Toast.makeText(this, "Please select two markers", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.btn_reset:{
                if(markersList!=null) {
                    tvResult.setText("");
                    map.clear();
                    markersList.clear();
                }
                break;
            }
        }
    }
}
