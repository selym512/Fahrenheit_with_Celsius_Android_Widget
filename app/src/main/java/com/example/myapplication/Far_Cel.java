package com.example.myapplication;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Implementation of App Widget functionality.
 */
public class Far_Cel extends AppWidgetProvider {

    private WorkManager WManager;

//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//
//        CharSequence widgetText = context.getString(R.string.temperature);
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.far__cel);
////        views.setTextViewText(R.id.appwidget_text, widgetText);
//
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//
//    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        super.onUpdate(context, appWidgetManager, appWidgetIds);
        WManager = WorkManager.getInstance(context);

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(requestWorker.class, 15, TimeUnit.MINUTES)
                        // Constraints
                        .build();
        WManager.enqueueUniquePeriodicWork("tempReq", ExistingPeriodicWorkPolicy.UPDATE, saveRequest);
        Log.i("myles", "UPDATED");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId);
            Log.i("myles", "loading");
//            String loaded = tempStorage.loadTextFromInternalStorage(context, "coords");
            String temp = tempStorage.loadTextFromInternalStorage(context, "temperature");
            int farenInt = Integer.parseInt(temp);
            int celciusInt =  (farenInt - 32) * 5/9;
            String temperatureOutput = "" + farenInt + "°F " + celciusInt + "°C";
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.far__cel);
            views.setTextViewText(R.id.appwidget_text, temperatureOutput);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.i("myles", "THIS WAS LOADED: " + temperatureOutput);

        }
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
//        Log.i("myles", tempStorage.loadTextFromInternalStorage(context));
//        Log.i("myles", "enabled");
//        alarmService = new AlarmService(context);
//        alarmService.start();
//        views = new RemoteViews(context.getPackageName(), R.layout.far__cel);


    }
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
//        alarmService.stop();

    }

}