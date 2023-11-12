package com.example.myapplication;

import android.Manifest;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private WorkManager WManager;
    double lat;
    double lon;


    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
//                    getCurrentLocation();
                    WManager = WorkManager.getInstance(getApplication());

                    PeriodicWorkRequest saveRequest =
                            new PeriodicWorkRequest.Builder(requestWorker.class, 15, TimeUnit.MINUTES)
                                    // Constraints
                                    .build();
                    WManager.enqueueUniquePeriodicWork("tempReq", ExistingPeriodicWorkPolicy.UPDATE, saveRequest);
                } else {
                    Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        Log.i("out", "how many times is onCreate running");
    }

    private void getCurrentLocation() {
        Log.i("out", "how many times is getCurrentLocation running");
        try{
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Log.i("out", "is this the one that works");
                TextView str = findViewById(R.id.temp);
                String str1 = getString(R.string.temperature);

                lat = location.getLatitude();
                lon = location.getLongitude();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.far__cel);
                str.setText("Lon: " + lon + " Lat: " + lat);
                str1 = (lon + "," + lat);
                tempStorage.saveTemperature(this, str1, "coords");
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, R.layout.far__cel);
//                sendBroadcast(intent);
//                Log.i("out", "broadcasted?");
                OkHttpClient client = new OkHttpClient();
                String forcastURL;
//                String forecastJson;
                Toast.makeText(this, String.valueOf(lat).substring(0,6) + String.valueOf(lon).substring(0,6), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, String.valueOf(lon), Toast.LENGTH_SHORT).show();
                String url = String.format(Locale.US, "https://api.weather.gov/points/%.4f,%.4f", lat, lon);

                HTTPReqeustService httpService = new HTTPReqeustService(this);
                try {
                    httpService.sendOkayHTTP(client, url, new HTTPReqeustService.HTTPResponseListener() {
                        @Override
                        public void onError(String message) {
    //                        throw new Exception(e);
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onResponse(Object response) {
                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("out", "Runtime Exception" , e);
                    throw new RuntimeException(e);
                }

                //need to get forecastURL


//
//                //retrieved forecast url and now requesting that
//                try {
//                    forecastJson =  httpService.sendOkayHTTP(client, forcastURL, new HTTPReqeustService.HTTPResponseListener() {
//                        @Override
//                        public void onError(String message) {
//                            //                        throw new Exception(e);
//                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onResponse(Object response) {
//                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
//
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e("out", "Runtime Exception" , e);
//                    throw new RuntimeException(e);
//                }
            }
        });}
        catch (SecurityException e){
            Log.e("out", "securityException" , e);
            throw new SecurityException(e);
        }
    }



}