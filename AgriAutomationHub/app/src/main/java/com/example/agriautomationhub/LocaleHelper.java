package com.example.agriautomationhub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {

    public static void setLocale(Context context, String lang) {
        persist(context, lang);
        updateResources(context, lang);

        // Restart the app to apply changes
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();
    }


    private static void persist(Context context, String lang) {
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    private static Context updateResources(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        context.getResources().updateConfiguration(config, metrics);
        return context;
    }

    public static Context setLocale(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        return updateResources(context, language);
    }
}
