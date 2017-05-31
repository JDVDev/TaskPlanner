package com.jdv.retail.taskplanner.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.packet.AdvertisingPacket;
import com.jdv.retail.taskplanner.packet.Message;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by TFI on 22-3-2017.
 */

public class BleAdvertiser{
    private static final String TAG = "BleAdvertiser";
    private static final int ADVERTISEMENT_TIME_TO_LIVE = 5;
    private static final long SEND_INTERVAL = 10;
    private final static int SEND_TIME = 500;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseSettings settings;
    private boolean isProcessing;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Future future;
    private ArrayList<AdvertisingPacket> advertisingPacketsList = new ArrayList<>();

    private static BleAdvertiser instance = null;

    public static BleAdvertiser getInstance() {
        if(instance == null) {
            instance = new BleAdvertiser();
        }
        return instance;
    }

    private BleAdvertiser(){
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(false)
                .build();
        Log.d(TAG, "Advetiser created");
    }

    public void sendAdvertising(Message message){
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(Constants.SERVICE_UUID)
                .addServiceData(Constants.SERVICE_UUID, message.getRawBytes())
                .build();
        stopAdvertising();
        //advertiser.startAdvertising(settings, data, advertisingCallback);
        Log.d(TAG, "Send advertising: " + message);
        future = scheduleTaskExecutor.schedule(new StopAdvertisingRunnable(), SEND_TIME, TimeUnit.MILLISECONDS);


        /* Fix later
        addToDataBuffer(data);
        if(!isProcessing){
            Log.d(TAG, "!iSProcessing");
            processAdvertising();
        }*/
    }

    private void addToDataBuffer(AdvertiseData data){
        Log.d(TAG, "add to buffer" + data);
        AdvertisingPacket advertisingPacket = new AdvertisingPacket(data, data.getManufacturerSpecificData().keyAt(0), ADVERTISEMENT_TIME_TO_LIVE, 0);
        advertisingPacketsList.add(advertisingPacket);
    }

    private void processAdvertising(){
        Log.d(TAG, "Start processing");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!advertisingPacketsList.isEmpty()){
                    setProcessing(true);
                    Log.d(TAG, "Buffer not empty");
                    AdvertisingPacket packet = advertisingPacketsList.get(0);
                    if(packet.getAndDecremtTimeToLive() >= 0){
                        Log.d(TAG, "Packet ttl >= go advertise" + packet.getAdvertiseData());
                        advertiser.startAdvertising(settings, packet.getAdvertiseData(), advertisingCallback);
                        try{
                            Thread.sleep(SEND_INTERVAL);
                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        advertiser.stopAdvertising(advertisingCallback);
                        Log.d(TAG, "stop advertise");
                    }
                    else {
                        Log.d(TAG, "Packet ttl == 0 remove from list");
                        advertisingPacketsList.remove(packet);
                    }
                }
                stopAdvertising();
            }
        }).start();
    }

    public void stopAdvertising(){
        Log.d(TAG, "Stop Advertising");
        if(future != null && !future.isCancelled()){
            future.cancel(true);
        }
        advertiser.stopAdvertising(advertisingCallback);
        setProcessing(false);
    }

    private void setProcessing(boolean processing){
        isProcessing = processing;
    }

    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };

    private class StopAdvertisingRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "Stop Advertising after:  " + SEND_TIME);
            advertiser.stopAdvertising(advertisingCallback);
        }
    }
}
