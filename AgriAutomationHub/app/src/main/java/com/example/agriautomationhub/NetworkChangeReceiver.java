package com.example.agriautomationhub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.view.MenuItem;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final NetworkChangeListener listener;

    public NetworkChangeReceiver(NetworkChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                boolean isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                listener.onNetworkChange(isConnected);
            } else {
                boolean isConnected = false;
                Network[] networks = cm.getAllNetworks();
                for (Network network : networks) {
                    NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                    if (nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        isConnected = true;
                        break;
                    }
                }
                listener.onNetworkChange(isConnected);
            }
        }
    }

    public interface NetworkChangeListener {
        boolean onOptionsItemSelected(MenuItem item);

        void onNetworkChange(boolean isConnected);
    }
}
