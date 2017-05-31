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
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.jdv.retail.taskplanner.activity.DemoCounterActivity;
import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.activity.DemoSnakeActivity;
import com.jdv.retail.taskplanner.activity.DemoTapperActivity;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;
import com.jdv.retail.taskplanner.notification.NotificationHandler;
import com.jdv.retail.taskplanner.packet.DiscoveryResultToMessageHandler;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.packet.MessageCreator;

import java.util.ArrayList;
import java.util.List;

public class BleDiscoveryService extends Service implements
        DiscoveryResultToMessageHandler.OnPreprocessedMessageReceived,
        DiscoveryResultToMessageHandler.OnConfigurationMessageReceived {

    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter bluetoothAdapter;
    private final static String TAG = "DiscoveryService";
    private DiscoveryResultToMessageHandler discoveryResultToMessageHandler;
    private PowerManager.WakeLock wakeLock;
    private Context context;
    private NotificationHandler notificationHandler;

    private int receiveCounter = 0;

    private static byte lasMsgID = 0x00; //For demo
    public static int totalPackets = 0; //For demo
    public static int lastTotalPackets = 0;

    public BleDiscoveryService(){}

    @Override
    public void onCreate(){
        context = this;
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                Constants.TAG + "DiscoveryWakelock");
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // check bluetooth is available and on
        if (bluetoothAdapter != null) {
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, intentFilter);
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();// Isnt fast enough do anyways
            }
            discoveryResultToMessageHandler = DiscoveryResultToMessageHandler.getInstance();
            discoveryResultToMessageHandler.setOnPreprocessedMessageReceivedListener(this);
            discoveryResultToMessageHandler.setOnConfigurationMessageReceivedListener(this);
            notificationHandler = new NotificationHandler(context);
            discoveryResultToMessageHandler.setOnNotificationMessageReceivedListener(notificationHandler);
            mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        }

        startForeground(NotificationHandler.NOTIFICATION_ID,
                new NotificationHandler(context).getDefaultNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Utils.setAlarm(context);
        }
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Starting discovery service for reason: " + intent);

        stopDiscovery();
        startDiscovery();

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

    public void startDiscovery(){
        Log.d(TAG, "StartDiscovery");
        if(!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(Constants.SERVICE_UUID)
                .build();
        filters.add(filter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    public void stopDiscovery() {
        Log.d(TAG, "stop Discovery");
        mBluetoothLeScanner.stopScan(mScanCallback);
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            /*Log.d(TAG, "scan result: " + result);
            Log.d(TAG, "scanrecord : " + result.getScanRecord());
            Log.d(TAG, "scanrecord raw : " + Utils.bytesToHexString(result.getScanRecord().getBytes()));
            Log.d(TAG, "result getManufacturerSpecificData: " + result.getScanRecord().getManufacturerSpecificData());*/
            /*Log.d(TAG, "key at 0 int: " + result.getScanRecord().getManufacturerSpecificData().keyAt(0));
            Log.d(TAG, "value at 0 bytes: " + result.getScanRecord().getManufacturerSpecificData().valueAt(0));*/
            discoveryResultToMessageHandler.processDiscoveryResults(result, context);


            //FOR DEMO
            byte[] demoData = result.getScanRecord().getServiceData(Constants.SERVICE_UUID);
            if(demoData != null) {
                Message msg;
                try {
                    msg = MessageCreator.createMessage(demoData);
                }
                catch (InvalidMessageLengthException |
                        InvalidMessageSourceLengthException |
                        InvalidMessageDestinationLengthException |
                        InvalidMessageDataLengthException e){
                    Log.d(TAG, "Length invalid, discarding message");
                    e.printStackTrace();
                    return;
                }
                if(msg.getMessageID() == lasMsgID){
                    totalPackets++;
                    Log.d(TAG, "Total packets received: " + totalPackets);
                }
            }
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            Toast.makeText(getApplicationContext(),
                    "BLE Discovery onScanFailed: " + errorCode , Toast.LENGTH_LONG).show();
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "Ondestroy");
        stopForeground(true);
        if (mBluetoothLeScanner != null) {
            stopDiscovery();
            mBluetoothLeScanner = null;
        }
        discoveryResultToMessageHandler.setOnPreprocessedMessageReceivedListener(this);
        discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
        discoveryResultToMessageHandler.removeOnNotificationMessageReceivedListener(notificationHandler);
    }

    @Override
    public void onPreprocessedMessageReceived(Message msg) {
        lastTotalPackets = totalPackets;
        totalPackets = 0;
        lasMsgID = msg.getMessageID();
        Log.d(TAG, "Counter received: " + (++receiveCounter) + " " + String.format("%02X",msg.getMessageID()));
    }

    @Override
    public void onConfigurationMessageReceived(Message msg) {
        if(msg.getMessageData()[0] == 0x00){
            Intent intent = new Intent(context, DemoCounterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        if(msg.getMessageData()[0] == 0x01){
            Intent intent = new Intent(context, DemoTapperActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        if(msg.getMessageData()[0] == 0x02){
            Intent intent = new Intent(context, DemoSnakeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
                        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()){
                            bluetoothAdapter.enable();
                        }
                        stopDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Bluetooth on");
                        startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    public static int getTotalPackets() {
        return lastTotalPackets;
    }
}
