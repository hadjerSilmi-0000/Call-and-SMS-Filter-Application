package com.example.homeworksem;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;

public class CallReceiver extends BroadcastReceiver {

    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) return;

        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (incomingNumber == null || incomingNumber.isEmpty()) return;

        Log.d("CallReceiver", "Incoming call from: " + incomingNumber);

        boolean blacklisted = FileHelper.isBlacklisted(context, incomingNumber);
        boolean whitelisted = FileHelper.isWhitelisted(context, incomingNumber);

        if (blacklisted || !whitelisted) {
            Log.d("CallReceiver", "Blocking call from: " + incomingNumber);
            // Method 1: TelecomManager (Android 9+)
            if (Build.VERSION.SDK_INT >= 28) {
                try {
                    TelecomManager telecomManager = (TelecomManager)
                            context.getSystemService(Context.TELECOM_SERVICE);
                    telecomManager.endCall();
                } catch (Exception e) {
                    Log.e("CallReceiver", "TelecomManager failed: " + e.getMessage());
                }
            }
            // Method 2: Reflection fallback
            try {
                TelephonyManager telephonyManager = (TelephonyManager)
                        context.getSystemService(Context.TELEPHONY_SERVICE);
                java.lang.reflect.Method endCall = telephonyManager.getClass()
                        .getMethod("endCall");
                endCall.invoke(telephonyManager);
            } catch (Exception e) {
                Log.e("CallReceiver", "Reflection failed: " + e.getMessage());
            }
        }
    }
}