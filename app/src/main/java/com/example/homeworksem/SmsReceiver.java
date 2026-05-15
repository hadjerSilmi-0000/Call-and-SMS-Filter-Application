package com.example.homeworksem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        try {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages == null || messages.length == 0) return;

            String sender = messages[0].getDisplayOriginatingAddress();
            if (sender == null) return;

            Log.d("SmsReceiver", "SMS received from: " + sender);

            boolean blacklisted = FileHelper.isBlacklisted(context, sender);
            boolean whitelisted = FileHelper.isWhitelisted(context, sender);

            if (blacklisted) {
                Log.d("SmsReceiver", "Blacklisted sender, starting alert: " + sender);
                Intent serviceIntent = new Intent(context, MyService.class);
                serviceIntent.putExtra("SENDER_NUMBER", sender);
                context.startService(serviceIntent);
                try {
                    abortBroadcast();
                } catch (Exception e) {
                    Log.e("SmsReceiver", "abortBroadcast failed: " + e.getMessage());
                }
            } else if (!whitelisted) {
                Log.d("SmsReceiver", "Not whitelisted, blocking SMS: " + sender);
                try {
                    abortBroadcast();
                } catch (Exception e) {
                    Log.e("SmsReceiver", "abortBroadcast failed: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e("SmsReceiver", "Error: " + e.getMessage());
        }
    }
}