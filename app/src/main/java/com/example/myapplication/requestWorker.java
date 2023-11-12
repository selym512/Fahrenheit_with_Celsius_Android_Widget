package com.example.myapplication;


import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class requestWorker extends Worker {
    private FusedLocationProviderClient fusedLocationClient;
//    LocationRequest req;
    private Context context;
    private WorkManager WManager;
    double lat;
    double lon;
    OkHttpClient client = new OkHttpClient();

    public requestWorker( @NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
//        req = new LocationRequest.Builder().build();

    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("out", "we doin work now.");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
//        req = new LocationRequest.Builder();
        getCurrentLocation();
        return Result.success();
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
        catch (IOException e){
            Log.e("out", "IOerror", e);
        }
        return url;
    }

    private String sendLoc() throws IOException {
        OkHttpClient client = new OkHttpClient();
        String forcastURL;
        String[] loc = (tempStorage.loadTextFromInternalStorage(context, "coords")).split(",");
        String lat = loc[1];
        String lon = loc[0];
        Log.i("out", ("lat and lon: " + lat + ' ' + lon));
        String url = String.format(Locale.US, "https://api.weather.gov/points/%.6s,%.6s", lat, lon);
        Log.i("out", url);
        return run(url);
    }

    private void getCurrentLocation() {
        Log.i("out", "how many times is getCurrentLocation running");
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.i("out", "location permission true");
            } else {
                Log.i("out", "location permission false");
            }

//            fusedLocationClient.requestLocationUpdates();

            fusedLocationClient.getLastLocation().addOnSuccessListener(new Executor() {
                @Override
                public void execute(Runnable command) {
                    Log.i("out", "executing");
                    command.run();
                }
            }, location -> {
                if (location != null) {
                    Log.i("out", "location gotten within worker!");
                    String str1;
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.far__cel);
                    str1 = ("" + lon + "," + lat);
                    Log.i("out", str1);
                    tempStorage.saveTemperature(context, "coords", str1);
                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, R.layout.far__cel);
                    context.sendBroadcast(intent);
                    Log.i("out", "broadcasted?");
                    String url = String.format(Locale.US, "https://api.weather.gov/points/%.4f,%.4f", lat, lon);
                    try {
                        JSONObject jo = new JSONObject(sendLoc());
                        JSONObject properties = jo.getJSONObject("properties");
                        String forecast = properties.getString("forecastHourly");
                        Log.i("out", forecast);
                        String forecastResp = run(forecast);
                        JSONObject forecastJSON = new JSONObject(forecastResp);
                        properties = forecastJSON.getJSONObject("properties");
                        JSONArray periods = properties.getJSONArray("periods");
                        String temp = periods.getJSONObject(0).get("temperature").toString();
                        Log.i("out", "temp: " + temp);
                        tempStorage.saveTemperature(context, "temperature", temp);

                    }catch (IOException e) {
                        Log.e("out", "runtime exc", e);
                        throw new RuntimeException(e);
                    }
                    catch(Exception e){
                        Log.e("out", "exception", e);
                    }
                } else {
                    Log.i("out", "location == null");
                    String[] loc = tempStorage.loadTextFromInternalStorage(context, "coords").split(",");
                    String latitude = loc[1];
                    String longitude = loc[0];
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.far__cel);
                    String str1 = ("" + longitude + "," + latitude);
                    Log.i("out", str1);
                    String url = String.format(Locale.US, "https://api.weather.gov/points/%.4f,%.4f", latitude, longitude);
                    try {
                        JSONObject jo = new JSONObject(sendLoc());
                        JSONObject properties = jo.getJSONObject("properties");
                        String forecast = properties.getString("forecast");
                        Log.i("out", forecast);
                        String forecastResp = run(forecast);
                        JSONObject forecastJSON = new JSONObject(forecastResp);
                        properties = forecastJSON.getJSONObject("properties");
                        JSONArray periods = properties.getJSONArray("periods");
                        String temp = periods.getJSONObject(0).get("temperature").toString();
                        Log.i("out", "temp: " + temp);
                        tempStorage.saveTemperature(context, "temperature", temp);

                    }catch (IOException e) {
                        Log.e("out", "runtime exc", e);
                        throw new RuntimeException(e);
                    }
                    catch(Exception e){
                        Log.e("out", "exception", e);
                    }
                }
            }).addOnFailureListener((Exception e) -> {
                Log.e("out", "error", e);
            });
        }catch (SecurityException e){
            Log.e("out", "securityException" , e);

            throw new SecurityException(e);
        }
        catch(Exception e){
            Log.e("out", "except", e);
        }
    }
}
