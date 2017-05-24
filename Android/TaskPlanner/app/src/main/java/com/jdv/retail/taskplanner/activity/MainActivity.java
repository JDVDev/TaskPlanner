package com.jdv.retail.taskplanner.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.notification.JSONParser;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;
import com.jdv.retail.taskplanner.R;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.bluetooth.BleAdvertiser;
import com.jdv.retail.taskplanner.bluetooth.BleCommunicationService;
import com.jdv.retail.taskplanner.bluetooth.BleDiscoveryService;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.Set;

public class MainActivity extends WearableActivity {
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean permissions_granted = false;
    private BleAdvertiser bleAdvertiser;
    private byte deviceID;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                permissions_granted = false;
                requestLocationPermission();
            } else{
                Log.i(Constants.TAG, "Location permission has already been granted. Starting scanning.");
                permissions_granted = true;
            }
        } else{
            permissions_granted = true;
        }

        bleAdvertiser = BleAdvertiser.getInstance();

        if(!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            Toast.makeText(context, "Multiple advertisement not supported", Toast.LENGTH_SHORT ).show();
        }

        Button randomButton = (Button) findViewById(R.id.random_button);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bleAdvertiser.sendAdvertising(new Message(randomMessageData()));
                } catch (InvalidMessageDataLengthException e){
                    e.printStackTrace();
                }
            }
        });

        Button pingButton = (Button) findViewById(R.id.ping_button);
        pingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            try {
                bleAdvertiser.sendAdvertising(MessageCreator.createMessage(
                        deviceID,
                        Constants.BASESTATION_ID,
                        Message.MESSAGE_TYPE_PING,
                        Message.getEmptyMessageData()));
            }
            catch (InvalidMessageDataLengthException e){
                e.printStackTrace();
            }
            }
        });


        final SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY,
                Context.MODE_PRIVATE);
        deviceID = (byte)sharedPref.getInt(Constants.SAVED_DEVICE_ID_KEY, 0);

        final TextView textViewId = (TextView) findViewById(R.id.textViewID);
        textViewId.setText(Utils.bytesToHexString(new byte[]{deviceID}));

        textViewId.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                deviceID = Utils.createRandomByte();
                editor.putInt(Constants.SAVED_DEVICE_ID_KEY, deviceID);
                editor.apply();
                textViewId.setText(Utils.bytesToHexString(new byte[]{deviceID}));
                return true;
            }
        });

        Switch notifySwitch = (Switch) findViewById(R.id.notifySwitch);
        notifySwitch.setChecked(Utils.isNotify(context));
        notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Constants.SAVED_IS_NOTIFY_KEY, isChecked);
                editor.apply();
            }
        });

        if(Constants.isPollOrListen){
            context.startService(new Intent(context, BleCommunicationService.class));
        }
        else {
            context.startService(new Intent(context, BleDiscoveryService.class));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Hopefully your alarm will have a lower frequency than this!
                Log.d(Constants.TAG, "Starting alarm");
                Utils.setAlarm(context);
            }
        }

        Log.d(Constants.TAG, "can play audio: " + canPlayAudio(context));

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for(BluetoothDevice device : pairedDevices){
            //unpairDevice(device);
        }
        if(!Constants.isDebug) {
            finish();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestLocationPermission(){
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
                public void onDismiss(DialogInterface dialog){
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == REQUEST_LOCATION){
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            // Location permission has been granted
                Log.i(Constants.TAG, "Location permission has now been granted. Scanning.....");
                permissions_granted = true;
                if(Constants.isPollOrListen){
                    context.startService(new Intent(context, BleCommunicationService.class));
                }
                else {
                    context.startService(new Intent(context, BleDiscoveryService.class));
                }
            } else{
                Log.i(Constants.TAG, "Location permission was NOT granted.");
            }
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void simpleToast(String message, int duration){
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public boolean canPlayAudio(Context context) {
        PackageManager packageManager = context.getPackageManager();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Check whether the device has a speaker.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check FEATURE_AUDIO_OUTPUT to guard against false positives.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
                return false;
            }

            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true;
                }
            }
        }
        return false;
    }

    private byte[] randomMessageData() {
        int randomInt = createRandomIntInRange(3);
        byte[] emptyMessageData = Message.getEmptyMessageData();
        try {
            switch (randomInt) {
                case 0:
                    emptyMessageData[0] = 0x01;
                    emptyMessageData[1] = 0x01;
                    return MessageCreator.createMessage(deviceID, Message.MESSAGE_TYPE_DATA, emptyMessageData).getRawBytes();
                case 1:
                    emptyMessageData[0] = 0x01;
                    emptyMessageData[1] = 0x02;
                    return MessageCreator.createMessage(deviceID, Message.MESSAGE_TYPE_DATA, emptyMessageData).getRawBytes();
                case 2:
                    emptyMessageData[0] = 0x02;
                    emptyMessageData[1] = 0x01;
                    return MessageCreator.createMessage(deviceID, Message.MESSAGE_TYPE_DATA, emptyMessageData).getRawBytes();
                case 3:
                    emptyMessageData[0] = 0x02;
                    emptyMessageData[1] = 0x02;
                    return MessageCreator.createMessage(deviceID, Message.MESSAGE_TYPE_DATA, emptyMessageData).getRawBytes();
                default:
                    emptyMessageData[0] = 0x01;
                    emptyMessageData[1] = 0x01;
                    return MessageCreator.createMessage(deviceID, Message.MESSAGE_TYPE_DATA, emptyMessageData).getRawBytes();
            }
        } catch (InvalidMessageDataLengthException e) {
            e.printStackTrace();
            return emptyMessageData;
        }
    }

    private int createRandomIntInRange(int range){
        return new Random().nextInt(range);
    }
}
