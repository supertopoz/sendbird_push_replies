package com.simple.android_push_replies_only.fcm;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessageParams;

import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String KEY_TEXT = "KEY_TEXT";
    public static final String KEY_MSG_ID = "KEY_MSG_ID";
    public static final String KEY_CHANNEL_URL = "KEY_CHANNEL_URL";
    private static final String USER_ID = "YOUR_USER_ID";

    //ALWAYS PUSH
    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {

            final PendingResult pendingResult = goAsync();

            final Thread replyThread = new Thread(() -> {
                Log.d("ALWAYS PUSH", "Prepare to log into Sendbird");
                final Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                Log.d("ALWAYS PUSH", remoteInput.getString(KEY_TEXT));
                if (null == remoteInput) return;
                final long messageId = intent.getLongExtra(KEY_MSG_ID, 0L);
                final String channelUrl = intent.getStringExtra(KEY_CHANNEL_URL);
                final String userInput = remoteInput.getString(KEY_TEXT);

                // Connect to Sendbird
                SendBird.connect(USER_ID, (user, connectionException) -> {
                    Log.d("ALWAYS PUSH", "Connected to Sendbird in Push context");
                    if (null != connectionException) {
                        onDone(pendingResult);
                    }
                    GroupChannel.getChannel(channelUrl, (groupChannel, e) -> {
                        onChannelFound(context, pendingResult, groupChannel, e, userInput, messageId);
                    });
                });
            });
            replyThread.start();
        }
    }

    private static void onChannelFound(final Context context,
                                       final PendingResult pendingResult,
                                       final GroupChannel groupChannel,
                                       final SendBirdException e,
                                       final String userInput,
                                       final long messageId) {
        if (null != e || null == groupChannel) {
            onDone(pendingResult);
            return;
        }
        final UserMessageParams params = new UserMessageParams();
        params.setMessage(userInput);
        params.setParentMessageId(messageId);

        // send message
        groupChannel.sendUserMessage(params, (userMessage, e1) -> {
            onMessageSent(context, pendingResult, messageId);
        });
    }

    private static void onMessageSent(final Context context,
                                      final PendingResult pendingResult,
                                      final long messageId) {

        final Notification repliedNotification = new NotificationCompat.Builder(context,
                "CHANNEL_ID")
                .setContentText("Reply sent")
                .build();

        NotificationManagerCompat.from(context).notify((int) messageId,
                repliedNotification);
        // disconnect
        onDone(pendingResult);
    }

    private static void onDone(final PendingResult pendingResult) {
        Log.d("ALWAYS PUSH", "Message sent but still connected to Sendbird");
        new Timer().schedule(
                new TimerTask(){
                    @Override
                    public void run(){
                        Log.d("ALWAYS PUSH", "Disconnected from Sendbird");
                        SendBird.disconnect(null);
                        //if you need some code to run when the delay expires
                    }
                }, 10000);
        pendingResult.finish();
    }
}
