package com.simple.android_push_replies_only.main;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sendbird.android.SendBird;
import com.simple.android_push_replies_only.R;
import com.simple.android_push_replies_only.utils.RegisterSendbirdDeviceToken;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "ALWAYS PUSH";
    private static final String APP_ID = "YOUR_APP_ID";
    private static final String USER_ID = "YOUR_USER_ID"; //ALSO UPDATE IN NotificationReceiver

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SendBird.init(APP_ID, getApplicationContext());
        login(USER_ID);

    }

    private static void login(String userId) {
        Log.d("ALWAYS PUSH", "Logging into Sendbird");
        SendBird.connect(userId, (user, connectionException) -> {
            if (connectionException != null) {
                Log.d("ALWAYS PUSH", "Failed to log into Sendbird");
            } else {
                Log.d("ALWAYS PUSH", "Logged into Sendbird as: " + user.getNickname());
                registerDeviceToken();
            }
        });
    }

    private static void registerDeviceToken() {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        RegisterSendbirdDeviceToken.sendToken(token);
                    }
                });
    }

}