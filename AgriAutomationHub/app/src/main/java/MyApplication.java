package com.example.agriautomationhub;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Set log level here to ensure it's set before any Firebase usage
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
    }
}
