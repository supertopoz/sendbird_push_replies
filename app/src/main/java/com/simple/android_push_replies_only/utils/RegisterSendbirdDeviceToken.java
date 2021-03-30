package com.simple.android_push_replies_only.utils;

import android.util.Log;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

public class RegisterSendbirdDeviceToken {

    public static void sendToken(String token) {

        SendBird.registerPushTokenForCurrentUser(token, new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus ptrs, SendBirdException e) {
                if (e != null) {
                    // Handle error.
                }
                Log.d("ALWAYS PUSH", "DEVICE TOKEN REGISTERED");
                if (ptrs == SendBird.PushTokenRegistrationStatus.PENDING) {
                    Log.d("ALWAYS PUSH", "TOKEN NOT YET REGISTERED");
                    // A token registration is pending.
                    // Retry the registration after a connection has been successfully established.
                }
            }
        });
    }

}
