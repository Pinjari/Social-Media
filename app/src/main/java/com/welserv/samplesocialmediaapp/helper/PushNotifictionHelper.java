package com.welserv.samplesocialmediaapp.helper;

import android.os.AsyncTask;
import android.os.storage.StorageVolume;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class PushNotifictionHelper {
    public final static String AUTH_KEY_FCM = "AAAA4K3jrl4:APA91bHdSsPUPsqxfO_dmLTulu0C-uzaV4PDXsqrd7zJ79kpy6P04Ksh__oLNm3a_EAcRuxt-PKgfDHK8s4u4GEWIzHsnmQSjFtyR9KjwqJXoR5AuGw1J2ilt_HBXeMvxaEfX5cwA23W";
    public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

    public static void sendPushNotification(final String deviceToken, final String title, final String msg, final String postID, final String postUserId)
            {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                BufferedReader in = null;


                String authKey = AUTH_KEY_FCM;   // You FCM AUTH key
                String FMCurl = API_URL_FCM;

                URL url = null;
                try {
                    url = new URL(FMCurl);
                } catch (MalformedURLException e) {
                    Log.i("yoyoyo", "error: 1");
                    e.printStackTrace();
                }
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    Log.i("yoyoyo", "error: 2");
                    e.printStackTrace();
                }

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                try {
                    conn.setRequestMethod("POST");
                } catch (ProtocolException e) {
                    Log.i("yoyoyo", "error: 3");
                    e.printStackTrace();
                }
                conn.setRequestProperty("Authorization", "key=" + authKey);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();


                JSONObject info = new JSONObject();


                try {
                    json.put("to", deviceToken.trim());
                    info.put("body", msg); // Notification body
                    info.put("title", title);   // Notification title
                    info.put("postId", postID);   // Notification title
                    info.put("postUserId", postUserId);   // Notification title
                    json.put("notification", info);
                } catch (JSONException e) {
                    Log.i("yoyoyo", "error: 6");
                    e.printStackTrace();
                }


                OutputStreamWriter wr = null;
                try {
                    wr = new OutputStreamWriter(conn.getOutputStream());
                } catch (IOException e) {
                    Log.i("yoyoyo", "error: 8");
                    e.printStackTrace();
                }
                try {
                    wr.write(json.toString());
                } catch (IOException e) {
                    Log.i("yoyoyo", "error: 9");
                    e.printStackTrace();
                }
                try {
                    wr.flush();
                } catch (IOException e) {
                    Log.i("yoyoyo", "error: 10");
                    e.printStackTrace();
                }
                try {
                    conn.getInputStream();
                } catch (IOException e) {
                    Log.i("yoyoyo", "error: 11");
                    e.printStackTrace();
                }

                return null;
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

}
