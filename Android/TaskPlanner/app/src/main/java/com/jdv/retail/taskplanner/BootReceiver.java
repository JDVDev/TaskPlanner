package com.jdv.retail.taskplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.jdv.retail.taskplanner.bluetooth.BleCommunicationService;
import com.jdv.retail.taskplanner.bluetooth.BleDiscoveryService;

/**
 * Created by TFI on 24-3-2017.
 */

public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.TAG, "BootReceiver start BleDiscoveryService");
        if(Constants.isPollOrListen){
            context.startService(new Intent(context, BleCommunicationService.class));
        }
        else {
            context.startService(new Intent(context, BleDiscoveryService.class));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Utils.setAlarm(context);
            }
        }
    }
}
