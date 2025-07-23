package com.example.agriautomationhub;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class BackgroundInternetService extends Service {

    public static final String CHANNEL_ID = "BackgroundInternetServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
        checkNetworkInBackground();
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Internet Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Internet Service")
                .setContentText("Connecting to internet in background")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        startForeground(1, notification);
    }

    private void checkNetworkInBackground() {
        // Implement your network tasks here, e.g., pinging a server or sending data
        // You can use Retrofit, HttpUrlConnection, or any network library
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;  // Keeps the service running
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop any background tasks if necessary
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
