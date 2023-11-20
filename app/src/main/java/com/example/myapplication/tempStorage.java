package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class  tempStorage {

    public static void saveString(Context context, String filename, String temperature) {
//        File file = new File(this.getFilesDir().toURI() + File.separator + "temp");
//        Log.i("myles", " new File doesn't fail");
        try {
//            if (!file.exists()) {
//                Log.i("myles", "!file.exists() doesn't fail");
//                file.createNewFile();
//                Log.i("myles", "!file.exists() doesn't fail");
            Log.i("myles", "save Text running");
            FileOutputStream fos = context.openFileOutput(filename + ".txt", Context.MODE_PRIVATE);
            fos.write(temperature.getBytes());
            fos.close();
        }
//                FileOutputStream outputStream = new FileOutputStream(file);
//                outputStream.write(temperature.getBytes());
//                outputStream.close();
        catch (IOException e){
            Log.e("myles", "IO Exception", e);
        }
    }

    public static String loadTextFromInternalStorage(Context context, String filename) {
        try {
            Log.i("myles", "load Text running");
            FileInputStream fis = context.openFileInput(filename + ".txt");
            byte[] buffer = new byte[1024];
            int bytesRead = fis.read(buffer);
            String text = new String(buffer, 0, bytesRead);
            fis.close();
            return text;
        } catch (IOException e) {
            Log.e("myles", "IO Exception", e);
            return null;
        }
    }

}
