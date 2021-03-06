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
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;
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
    private static final int REQUEST_WRITE = 1;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean permissions_granted = false;
    private BleAdvertiser bleAdvertiser;
    private byte[] deviceID = new byte[2];
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
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestWritePermission();
            }
        } else{
            permissions_granted = true;
        }

        bleAdvertiser = BleAdvertiser.getInstance();

        //if(!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
        //    Toast.makeText(context, "Multiple advertisement not supported", Toast.LENGTH_SHORT ).show();
        //}

        Button randomButton = (Button) findViewById(R.id.random_button);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAdvertiser.sendAdvertising(randomMessageData());
            }
        });

        Button pingButton = (Button) findViewById(R.id.ping_button);
        pingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            try {
                Message msg = MessageCreator.createMessage(
                        Constants.MESSAGE_SEQUENCE,
                        deviceID,
                        Constants.BASESTATION_ID,
                        Message.MESSAGE_TYPE_PING,
                        Message.getEmptyMessageData());
                bleAdvertiser.sendAdvertising(msg);
                Log.d(Constants.TAG, "Ping: " + msg);
            }
            catch (InvalidLengthException e){
                e.printStackTrace();
            }
            }
        });


        final SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY,
                Context.MODE_PRIVATE);
        deviceID = Utils.getDeviceID(context);

        final TextView textViewId = (TextView) findViewById(R.id.textViewID);
        textViewId.setText(Utils.bytesToHexString(deviceID).toUpperCase());

        textViewId.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deviceID = Utils.createRandomByteArray(2);
                Utils.setDeviceID(context, deviceID);
                textViewId.setText(Utils.bytesToHexString(deviceID).toUpperCase());
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

        //BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        //for(BluetoothDevice device : pairedDevices){
        //    unpairDevice(device);
        //}
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

    private void requestWritePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Write access so this application can log battery levels");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
                public void onDismiss(DialogInterface dialog){
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                }
            });
            builder.show();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
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
        if(requestCode == REQUEST_WRITE){
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i(Constants.TAG, "Write permission has now been granted");
            } else{
                Log.i(Constants.TAG, "Write permission was NOT granted");
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

    private Message randomMessageData() {
        int randomInt = createRandomIntInRange(3);
        byte[] emptyMessageData = Message.getEmptyMessageData();
        try {
            switch (randomInt) {
                case 0:
                    emptyMessageData[0] = 0x00;
                    emptyMessageData[1] = 0x01;
                    return MessageCreator.createMessage(Constants.MESSAGE_SEQUENCE, deviceID, Message.MESSAGE_TYPE_NOTI, emptyMessageData);
                case 1:
                    emptyMessageData[0] = 0x00;
                    emptyMessageData[1] = 0x02;
                    return MessageCreator.createMessage(Constants.MESSAGE_SEQUENCE, deviceID, Message.MESSAGE_TYPE_NOTI, emptyMessageData);
                case 2:
                    emptyMessageData[0] = 0x00;
                    emptyMessageData[1] = 0x03;
                    return MessageCreator.createMessage(Constants.MESSAGE_SEQUENCE, deviceID, Message.MESSAGE_TYPE_NOTI, emptyMessageData);
                case 3:
                    emptyMessageData[0] = 0x00;
                    emptyMessageData[1] = 0x04;
                    return MessageCreator.createMessage(Constants.MESSAGE_SEQUENCE, deviceID, Message.MESSAGE_TYPE_NOTI, emptyMessageData);
                default: //Should not come here
                    emptyMessageData[0] = 0x00;
                    emptyMessageData[1] = 0x01;
                    return MessageCreator.createMessage(Constants.MESSAGE_SEQUENCE, deviceID, Message.MESSAGE_TYPE_NOTI, emptyMessageData);
            }
            /*return MessageCreator.createMessage(
                    Utils.hexStringToByteArray("ffffff"),
                    Utils.hexStringToByteArray("fbbf"),
                    Utils.hexStringToByteArray("EC3C"),
                    Utils.hexStringToByteArray("72")[0],
                    Utils.hexStringToByteArray("01")[0],
                    Utils.hexStringToByteArray("0720b3620000"));*/

        } catch (InvalidLengthException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int createRandomIntInRange(int range){
        return new Random().nextInt(range);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
