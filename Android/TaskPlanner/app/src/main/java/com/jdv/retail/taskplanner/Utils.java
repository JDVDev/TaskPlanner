package com.jdv.retail.taskplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.jdv.retail.taskplanner.bluetooth.BleDiscoveryService;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by tfi on 18/04/2017.
 */

public class Utils {

    public static byte[] getDeviceID(Context context){
        final SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY,
                Context.MODE_PRIVATE);
        return hexStringToByteArray(sharedPref.getString(Constants.SAVED_DEVICE_ID_KEY, "0000"));
    }

    public static void setDeviceID(Context context, byte[] deviceID){
        final SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.SAVED_DEVICE_ID_KEY, bytesToHexString(deviceID));
        editor.apply();
    }

    public static boolean isNotify(Context context){
        final SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY,
                Context.MODE_PRIVATE);
        return sharedPref.getBoolean(Constants.SAVED_IS_NOTIFY_KEY, false);
    }

    public static String bytesToHexString(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static String bytesToHexString(byte in) {
        byte[] convertByte = new byte[1];
        convertByte[0] = in;
        return bytesToHexString(convertByte);
    }

    public static String bytesToDecimalString(byte[] in) {
        return Long.toString(Long.parseLong(bytesToHexString(in), 16));
    }

    public static String bytesToDecimalString(byte in) {
        return Long.toString(Long.parseLong(bytesToHexString(in), 16));
    }

    public static String hexStringToDecimalString(String data){
        return Long.toString(Long.parseLong(data, 16));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void setAlarm(Context context){
        /*AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent actionIntent = new Intent(context, BleDiscoveryService.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, actionIntent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent actionIntent = new Intent(context, BleDiscoveryService.class);
            PendingIntent alarmIntent = PendingIntent.getService(context, 0, actionIntent, 0);
            alarmMgr.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
            Log.d(Constants.TAG, "Alarm set and starting");
        }
    }

    public static byte createMessageIDByte(){
        byte[] randomByte = new byte[1];
        while(randomByte[0] == (byte)0x00 || randomByte[0] == (byte)0xFF) {
            new Random().nextBytes(randomByte);
        }
        return randomByte[0];
    }

    public static byte createRandomByte(){
        byte[] randomByte = new byte[1];
        new Random().nextBytes(randomByte);
        return randomByte[0];
    }

    public static byte[] createRandomByteArray(int size){
        byte[] randomByte = new byte[size];
        new Random().nextBytes(randomByte);
        return randomByte;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

}
