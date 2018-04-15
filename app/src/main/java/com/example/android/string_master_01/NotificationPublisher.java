package com.example.android.string_master_01;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationPublisher extends BroadcastReceiver {

    private static final String TAG = "NotificationPublisher";

    public static String notificationInstance = "notification";
    public static String notificationId = "id";

    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = intent.getParcelableExtra(notificationInstance);
        int id = intent.getIntExtra(notificationId, 0);
        notificationManager.notify(id, notification);
    }
}
