package com.example.homeworksem;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MyService extends Service {

    MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("SENDER_NUMBER")) {
            String sender = intent.getStringExtra("SENDER_NUMBER");
            android.widget.Toast.makeText(this, "Blocked SMS from " + sender, android.widget.Toast.LENGTH_LONG).show();
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}