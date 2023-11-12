package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HTTPReqeustService {
    Context context;
    public HTTPReqeustService(Context context){
        this.context = context;
    }

    public interface HTTPResponseListener {
        void onError(String message);

        void onResponse(Object response);
    }


    public void sendOkayHTTP( OkHttpClient client, String url ,HTTPResponseListener hTTPResponseListener) throws Exception {
        Log.i("out", "sendOkayHTTP");
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("out", "IOException", e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.i("out", "on response");
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    // Get response headers
                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        Log.i("response", responseHeaders.name(i) + ": " + responseHeaders.value(i));

                    }

                    // Get response body
//                    Toast.makeText(context, responseHeaders.toString(), Toast.LENGTH_SHORT).show();
                    Log.i("response", responseBody.string());
//                    System.out.println(responseBody.string());
                }
                catch (IOException e) {
                    Log.e("out", "IOException in onResponse()", e);
                    hTTPResponseListener.onError(e.getMessage());
                }
            }
        }
        );

    }



}
