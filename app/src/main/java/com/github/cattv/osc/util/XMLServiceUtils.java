package com.github.cattv.osc.util;


import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.github.cattv.osc.base.App;
import com.github.cattv.osc.callback.RequestCallback;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class XMLServiceUtils {
    private static final String TAG = "XMLServiceUtils";
    public static final String VERSION_URL_PATH = "https://raw.githubusercontent.com/supermeguo/BoxRes/main/Myuse/getUpdate.txt";
    public static Handler handler = new Handler();

    public static void getXMLData(String requestURL, RequestCallback requestCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(VERSION_URL_PATH);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(10000);
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder sb = new StringBuilder();
                        String jsonLine = "";
                        while (true) {
                            jsonLine = reader.readLine();
                            if (TextUtils.isEmpty(jsonLine)) {
                                break;
                            }
                            sb.append(jsonLine);

                        }
                        Log.i(TAG, "result=" + sb);
                        if (requestCallback != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    requestCallback.onSuccess(sb.toString());
                                }
                            });

                        }
                        reader.close();
                    } else {
                        if (requestCallback != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    requestCallback.onError("error code =" + code);
                                }
                            });


                        }
                    }
                } catch (Exception e) {
                    if (requestCallback != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                requestCallback.onError("Exception = " + e.toString());
                            }
                        });
                    }
                }
            }
        }).start();
    }
}
