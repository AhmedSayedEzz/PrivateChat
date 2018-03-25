package com.example.ahmedsayed.privatechat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Ahmed Sayed on 2018-03-14.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id_data");
        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_body=remoteMessage.getNotification().getBody();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(notification_title)
                .setContentText(notification_body);

        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        //set an id for the notification
        int mNotificationId= (int) System.currentTimeMillis();
        //get an instance of the notification Manger service
        NotificationManager mNotifMgr= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //buildes the notification and issues it
        mNotifMgr.notify(mNotificationId,mBuilder.build());


    }
}
