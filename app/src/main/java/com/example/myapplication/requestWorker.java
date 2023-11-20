package com.example.myapplication;


import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class requestWorker extends Worker {

    private FusedLocationProviderClient fusedLocationClient;
    private Context context;
    private WorkManager WManager;
    double lat;
    double lon;
    OkHttpClient client = new OkHttpClient();

    public interface LocationRetrieved {
        void locationRetrieved(String[] location) throws IOException, JSONException;
        void locationLoaded(String[] location) throws IOException, JSONException;
    }
    public requestWorker( @NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("myles", "we doin work now.");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
        getCurrentLocation(new LocationRetrieved() {
            @Override
            public void locationRetrieved(String[] location) throws IOException, JSONException {
                Log.i("myles", "location was retrieved through the location services");
                String str1 = ("" + location[1] + "," + location[0]);
                tempStorage.saveString(context, "coords", str1);
                String firstResponse = runHttpRequest(String.format(Locale.US, "https://api.weather.gov/points/%.6s,%.6s", location[0], location[1]));
                String secondResponse = runHttpRequest(parseHourlyForecastURL(firstResponse));
                String tempFahrenheit = parseTemperature(secondResponse);
                tempStorage.saveString(context, "temperature", tempFahrenheit);
                Log.i("myles", "Fahrenheit: " + tempFahrenheit);

            }
            @Override
            public void locationLoaded(String[] location) throws IOException, JSONException {
                Log.i("myles", "location was retrieved through the local storage");
                String str1 = ("" + location[1] + "," + location[0]);
                String firstResponse = runHttpRequest(String.format(Locale.US, "https://api.weather.gov/points/%.6s,%.6s", location[0], location[1]));
                String secondResponse = runHttpRequest(parseHourlyForecastURL(firstResponse));
                String tempFahrenheit = parseTemperature(secondResponse);
                tempStorage.saveString(context, "temperature", tempFahrenheit);
                Log.i("myles", "Fahrenheit: " + tempFahrenheit);

            }
        });
        return Result.success();
    }

    String runHttpRequest(String url) throws IOException {
        Log.i("myles", url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            } catch (IOException e) {
                Log.e("myles", "IOerror", e);
            }
        return url;
    }
    String parseHourlyForecastURL(String firstResponseJSON) throws JSONException {
        JSONObject jo = new JSONObject(firstResponseJSON);
        JSONObject properties = jo.getJSONObject("properties");
        String forecast = properties.getString("forecastHourly");
        Log.i("myles", forecast);
        return forecast;
    }
    String parseTemperature(String secondResponse) throws JSONException {
        JSONObject forecastJSON = new JSONObject(secondResponse);
        JSONObject properties = forecastJSON.getJSONObject("properties");
        JSONArray periods = properties.getJSONArray("periods");
        String temp = periods.getJSONObject(0).get("temperature").toString();
        return temp;
    }


    private void getCurrentLocation(LocationRetrieved locationRetriever) {
        Log.i("myles", "how many times is getCurrentLocation running");
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.i("myles", "location permission true");
            } else {
                Log.i("myles", "location permission false");
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(new Executor() {
                @Override
                public void execute(Runnable command) {
                    Log.i("myles", "executing");
                    command.run();
                }
            }, location -> {
                if (location != null) {
                    try {
                        locationRetriever.locationRetrieved(new String[] {String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())});
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.i("myles", "location == null");
                    String[] loc = tempStorage.loadTextFromInternalStorage(context, "coords").split(",");
                }
            }).addOnFailureListener((Exception e) -> {
                Log.e("myles", "error", e);
            });
        } catch (SecurityException e){
            Log.e("myles", "securityException" , e);
            throw new SecurityException(e);
        }
        catch(Exception e){
            Log.e("myles", "except", e);
        }
    }

}
