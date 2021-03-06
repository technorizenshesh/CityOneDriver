package com.cityonedriver.fcm;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.cityonedriver.R;
import com.cityonedriver.SplashActivity;
import com.cityonedriver.shipping.activities.MyDeliveryShipActivity;
import com.cityonedriver.shipping.activities.ShipChatingActivity;
import com.cityonedriver.taxi.activities.TaxiHomeAct;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.SharedPref;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    Intent intent;
    SharedPref sharedPref;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // log the getting message from firebase
        // Log.d(TAG, "From: " + remoteMessage.getFrom());

        //  if remote message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            // Log.d(TAG, "Message data payload : " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            String jobType = data.get("type");

            /* Check the message contains data If needs to be processed by long running job
               so check if data needs to be processed by long running job */

            // Handle message within 10 seconds
            handleNow(data);

             /* if (jobType.equalsIgnoreCase(JobType.LONG.name())) {
                 // For long-running tasks (10 seconds or more) use WorkManager.
                 scheduleLongRunningJob();
            } else {} */

        }

        // if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
            // Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // make a own server request here using your http client
    }

    private void handleNow(Map<String, String> data) {
        sendNotification(data.get("title"), data.get("message"));
    }

    private void sendNotification(String title, String messageBody) {

        sharedPref = SharedPref.getInstance(this);

        if(sharedPref.getBooleanValue(AppConstant.IS_REGISTER)) {
            if(messageBody != null) {
                Log.e("hjasgfasfdsf","title = " + title);
                Log.e("hjasgfasfdsf","messageBody = " + messageBody);

                JSONObject jsonObject = null;
                String msg = "";
                String shipId = "";
                String status = "";
                String key = "";

                try {
                    jsonObject = new JSONObject(messageBody);
                    msg = jsonObject.getString("message");
                    shipId = jsonObject.getString("shipping_id");
                } catch (JSONException e) {}

                try {
                    status = jsonObject.getString("status");
                    key = jsonObject.getString("key");
                } catch (Exception e) {}

                if("shipping".equals(key)) {

                    if("Accept".equals(status)) {
                        Intent intent = new Intent("devship");
                        sendBroadcast(intent);
                    }
                    callShippingWhenNotifyClicked(status,shipId,msg);

                } else if("New Booking Request".equals(key)) {
                    if(status.equals("Pending")) {
                        title = "New Booking Request";
                        Intent intent1 = new Intent("Job_Status_Action");
                        Log.e("SendData=====", jsonObject.toString());
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.putExtra("object", jsonObject.toString());
                        sendBroadcast(intent1);
                        calltaxiStatusClicked(title,"New Taxi Booking Request",jsonObject.toString(),"");
                    } else if(status.equals("Cancel_by_user")) {
                        Intent intent1 = new Intent("cancel_by_user");
                        Log.e("SendData=====", jsonObject.toString());
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.putExtra("object", jsonObject.toString());
                        sendBroadcast(intent1);
                        calltaxiStatusClicked(title,"Trip cancelled by user",jsonObject.toString(),"Cancel_by_user");
                    }
                }

            }
        }

    }

    private void calltaxiStatusClicked(String title,String msg,String data,String status) {
        intent = new Intent(this, TaxiHomeAct.class);
        intent.putExtra("object", data);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = "1";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel human readable title
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Cloud Messaging Service",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(getNotificationId(), notificationBuilder.build());
    }

    private void callShippingWhenNotifyClicked(String status,String shipId,String msg) {

        if("Accept".equals(status)) {
            intent = new Intent(this, MyDeliveryShipActivity.class);
        } else if("chat".equals(status)) {
            intent = new Intent(this, ShipChatingActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = "1";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel human readable title
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Cloud Messaging Service",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(getNotificationId(), notificationBuilder.build());

    }

    private static int getNotificationId() {
        Random rnd = new Random();
        return 100 + rnd.nextInt(9000);
    }

    private boolean isAppOnForeground(Context context,String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = appPackageName;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                // Log.e("app",appPackageName);
                return true;
            }
        }
        return false;
    }

}

