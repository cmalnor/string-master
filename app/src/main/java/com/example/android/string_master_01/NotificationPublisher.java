package com.example.android.string_master_01;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION = "notification";
    public static String NOTIFICATION_ID = "id";

    private static final String TAG = "NotificationPublisher";

    public void onReceive(Context context, Intent intent){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);
    }
}
