package com.simple.android_push_replies_only.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Build;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import com.sendbird.android.SendBirdPushHandler;
import com.sendbird.android.SendBirdPushHelper;
import com.simple.android_push_replies_only.R;
import com.simple.android_push_replies_only.utils.RegisterSendbirdDeviceToken;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

public class MyFirebaseMessagingService extends SendBirdPushHandler {


    private static final String TAG = "ALWAYS PUSH" ;

    //ALWAYS PUSH
    @Override
    protected boolean alwaysReceiveMessage() { return true; }

    @Override
    public void onNewToken(String token) {
        RegisterSendbirdDeviceToken.sendToken(token);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(Context context, RemoteMessage remoteMessage) {

        //ALWAYS PUSH - Check if message already arrived via Web-socket
        Boolean arrivedWhenConnected = SendBirdPushHelper.isDuplicateMessage(remoteMessage);
        Log.d(TAG, "Message already arrived via SDK?: " + arrivedWhenConnected);

        String channelUrl = null;
        try {
            if (remoteMessage.getData().containsKey("sendbird")) {
                JSONObject sendBird = new JSONObject(remoteMessage.getData().get("sendbird"));
                JSONObject channel = (JSONObject) sendBird.get("channel");
                channelUrl = (String) channel.get("channel_url");
                //ALWAYS PUSH GET MessageId
                long messageId = sendBird.getLong("message_id");

                sendNotification(context, remoteMessage.getData().get("message"), channelUrl, messageId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     * @param messageId
     */
    public static void sendNotification(Context context, String messageBody, String channelUrl, long messageId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String CHANNEL_ID = "CHANNEL_ID";
        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) { return; }
        //ALWAYS PUSH ---> Build Push Notification context UI
        Log.d("ALWAYS PUSH:", "Build reply");
        RemoteInput remoteInput = new RemoteInput.Builder(NotificationReceiver.KEY_TEXT)
                .setLabel("Reply")
                .build();
        Intent replyIntent = new Intent(context, NotificationReceiver.class);
        replyIntent.putExtra(NotificationReceiver.KEY_MSG_ID, messageId);
        replyIntent.putExtra(NotificationReceiver.KEY_CHANNEL_URL, channelUrl);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
                (int) messageId,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_play,
                        "reply", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                //ALWAYS REPLY - Add Action
                .addAction(action);
        notificationBuilder.setContentText(messageBody);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
