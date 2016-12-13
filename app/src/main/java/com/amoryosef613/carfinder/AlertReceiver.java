package com.amoryosef613.carfinder;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by waqas on 08/12/2016.
 */
public class AlertReceiver extends BroadcastReceiver {


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent();
        Log.d("magic1","i am called");
        i.putExtra("timeup","true");
        i.setClassName("com.amoryosef613.carfinder", "com.amoryosef613.carfinder.Time");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        //TODO UnComment following line if you want to show Push Notifications on Timer up!
      // createNotification(context,"Car Finder","Your time is over! ");


    }


    public void createNotification(Context context,String msg,String msgText){
        Intent intent=new Intent(context,Time.class);

        PendingIntent notificIntent=PendingIntent.getActivity(context, 0,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(msg)
                .setTicker(msgText)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msgText))
                .setContentText(msgText);
        mBuilder.setContentIntent(notificIntent);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager=
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(101,mBuilder.build());


    }
}
