package com.welserv.samplesocialmediaapp.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.welserv.samplesocialmediaapp.utils.AppSharedPref;
import com.welserv.samplesocialmediaapp.utils.CommonFun;


public class FirebaseMessagingServ extends FirebaseMessagingService {
    String TAG = "fcmService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                // scheduleJob();
            } else {
                // Handle message within 10 seconds
//                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            RemoteMessage.Notification noti = remoteMessage.getNotification();
            CommonFun.createNotification(noti.getBody(), this, noti.getTitle(), noti.getTitle());
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(String s) {
        AppSharedPref.setStringPreference(this, AppSharedPref.USER_TOKEN, s);
        Log.d(TAG, "onNewToken: " + s);
        super.onNewToken(s);
    }
}
