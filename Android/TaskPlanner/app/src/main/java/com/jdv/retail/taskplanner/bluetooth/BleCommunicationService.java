package com.jdv.retail.taskplanner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;
import com.jdv.retail.taskplanner.notification.NotificationHandler;
import com.jdv.retail.taskplanner.packet.DiscoveryResultToMessageHandler;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;
import com.jdv.retail.taskplanner.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BleCommunicationService extends Service implements DiscoveryResultToMessageHandler.OnPreprocessedMessageReceived {

    private final static String TAG = "BleCommunicationService";
    private final static int DISCOVERY_INTERVAL = 5 * 1000;
    private final static int DISCOVERY_TIME = 2200;
    private BluetoothAdapter bluetoothAdapter;
    private BleAdvertiser bleAdvertiser;
    private BluetoothLeScanner mBluetoothLeScanner;
    private DiscoveryResultToMessageHandler discoveryResultToMessageHandler;
    private Context context;
    private Handler handler;
    private Runnable runnable;
    private List<ScanFilter> filters;
    private ScanSettings settings;
    private PowerManager.WakeLock wakeLock;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Future future;

    private int sendCounter = 0;
    private int receiveCounter = 0;

    public BleCommunicationService(){

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "BleCommunicationService created");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                Constants.TAG + TAG);
        if(!wakeLock.isHeld()) {
            Log.d(TAG, "Wakelock acquired");
            wakeLock.acquire();
        }
        context = this;
        handler = new Handler();
        runnable = new MyRunnable();
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // check bluetooth is available and on
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();// Isnt fast enough
            }
            bleAdvertiser = BleAdvertiser.getInstance();
            discoveryResultToMessageHandler = DiscoveryResultToMessageHandler.getInstance();
            mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, intentFilter);

            // Empty data & mask
            byte[] manuData = Message.getEmptyMessage();// Length isnt right
            byte[] manuMask = Message.getEmptyMessage();
            //manuData[5] = Utils.getSourceID(context);// Dunno why 5, but works ;)
            //manuMask[5] = 0x01;

            filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setManufacturerData(Constants.MANUFACTURE_ID, manuData, manuMask)
                    .build();
            filters.add(filter);
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
        }

        startForeground(NotificationHandler.NOTIFICATION_ID,
                new NotificationHandler(context).getDefaultNotification());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Starting discovery service for reason: " + intent);

        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, createRandomIntInRange(2500));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void startAdvertising(){
        try {
            Message msg = MessageCreator.createMessage(
                    Constants.MESSAGE_SEQUENCE,
                    Utils.getDeviceID(context),
                    Constants.BASESTATION_ID,
                    Message.MESSAGE_TYPE_POLL,
                    Message.getEmptyMessageData());
            bleAdvertiser.sendAdvertising(msg);
            Log.d(TAG, "StartAdvertising");
            Log.d(TAG, "Counter send: " + (++sendCounter) + " " + String.format("%02X",msg.getMessageID()));
        }
        catch (InvalidLengthException e){
            e.printStackTrace();
        }
    }
    private void startDiscovery(){
        Log.d(TAG, "StartDiscovery");
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    private void stopDiscovery() {
        Log.d(TAG, "stop Discovery");
        if(!future.isCancelled()){
            future.cancel(true);
        }
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    @Override
    public void onPreprocessedMessageReceived(Message msg) {
        Log.d(TAG, "Counter received: " + (++receiveCounter) + " " + String.format("%02X",msg.getMessageID()));
        Log.d(TAG, "Stopping discovery a for " + msg);
        bleAdvertiser.stopAdvertising();
        stopDiscovery();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "scan result: " + result);
            Log.d(TAG, "scanrecord : " + result.getScanRecord());
            Log.d(TAG, "scanrecord raw : " + Utils.bytesToHexString(result.getScanRecord().getBytes()));
            Log.d(TAG, "result getManufacturerSpecificData: " + result.getScanRecord().getManufacturerSpecificData());
            Log.d(TAG, "key at 0 int: " + result.getScanRecord().getManufacturerSpecificData().keyAt(0));
            Log.d(TAG, "value at 0 bytes: " + result.getScanRecord().getManufacturerSpecificData().valueAt(0));
            discoveryResultToMessageHandler.processDiscoveryResults(result, context);
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            //Toast.makeText(getApplicationContext(),
                    //"BLE Discovery onScanFailed: " + errorCode , Toast.LENGTH_LONG).show();
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        if (mBluetoothLeScanner != null) {
            stopDiscovery();
            mBluetoothLeScanner = null;
        }
        if(wakeLock.isHeld()) {
            Log.d(TAG, "Wakelock released");
            wakeLock.release();
        }
        stopForeground(true);
    }

    private int createRandomIntInRange(int range){
        return new Random().nextInt(range);
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run(){
            try {
                Log.d(TAG,"Runnable: Start everything");
                startDiscovery();
                startAdvertising();
                future = scheduleTaskExecutor.schedule(new StopDiscoveryRunnable(), DISCOVERY_TIME, TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                handler.postDelayed(runnable, DISCOVERY_INTERVAL);
                Log.d(TAG,"Runnable: Stopped discovery, Repost");
            }
        }
    }

    private class StopDiscoveryRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "Stop Discovery after:  " + DISCOVERY_TIME);
            stopDiscovery();
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.d(TAG, "Bluetooth state changed");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"Bluetooth off");
                        handler.removeCallbacks(runnable);
                        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()){
                            bluetoothAdapter.enable();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Bluetooth on");
                        handler.post(runnable);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"Turning Bluetooth on...");
                        break;
                }
            }
        }
    };
}
