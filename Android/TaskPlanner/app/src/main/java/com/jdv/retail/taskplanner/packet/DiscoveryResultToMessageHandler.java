package com.jdv.retail.taskplanner.packet;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TFI on 24-3-2017.
 */

public class DiscoveryResultToMessageHandler{
    private final static String TAG = "DRToMessageHandler";
    private ArrayList<Byte>
            lastProcessedMessageID = new ArrayList<>();
    private CopyOnWriteArrayList<OnPreprocessedMessageReceived>
            onPreprocessedMessageReceivedListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnConfigurationMessageReceived>
            onConfigurationMessageReceivedListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnNotificationMessageReceived>
            onNotificationMessageReceivedListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnDataMessageReceived>
            onDataMessageReceivedListeners = new CopyOnWriteArrayList<>();

    private static DiscoveryResultToMessageHandler instance = null;

    public static DiscoveryResultToMessageHandler getInstance() {
        if(instance == null) {
            instance = new DiscoveryResultToMessageHandler();
        }
        return instance;
    }

    public interface OnPreprocessedMessageReceived {
        void onPreprocessedMessageReceived(Message msg);
    }

    public interface OnConfigurationMessageReceived{
        void onConfigurationMessageReceived(Message msg);
    }

    public interface OnNotificationMessageReceived{
        void onNotificationMessageReceived(Message msg);
    }

    public interface OnDataMessageReceived{
        void onDataMessageReceived(Message msg);
    }

    private DiscoveryResultToMessageHandler(){
    }

    public void processDiscoveryResults(ScanResult result, Context context){
        //Log.d(TAG, "Proces: " + result);
        byte[] messageData = null;
        try {
            messageData = result.getScanRecord().getServiceData(Constants.SERVICE_UUID);
        } catch (NullPointerException e){ //No Service data ignore message
            e.printStackTrace();
        }
        //byte messageId = messageData[0];
        /*Log.d(TAG, "Key: " + messageId);
        Log.d(TAG, "Data: " + bytesToHex(messageData));
        Log.d(TAG, "Procesmessage: " + bytesToHex(messageData));*/
        if(messageData != null) { //Ignore if no scanRecord or no service data
            processesMessageContent(messageData, context);
        }
    }

    private void processesMessageContent(byte[] content, Context context){
        Message msg;
        try {
            msg = new Message(content);
        }
        catch (InvalidMessageDataLengthException e){
            Log.d(TAG, "Length invalid, discarding message");
            e.printStackTrace();
            return;
        }

        if(msg.getSendToDeviceID() == Utils.getDeviceID(context) || msg.getSendToDeviceID() == Message.MESSAGE_ID_BROADCAST) {
            //Log.d(TAG, "Process content: " + bytesToHex(content));

            if(!lastProcessedMessageID.contains(msg.getMessageID())) {
                Log.d(TAG, "lastProcessedMessageID !contains: " + msg.getMessageID());
                lastProcessedMessageID.add(msg.getMessageID());
                Log.d(TAG, "lastProcessedMessageID: " + lastProcessedMessageID);

                Log.d(TAG, "Process message: " + msg);
                for (OnPreprocessedMessageReceived callback : onPreprocessedMessageReceivedListeners){
                    callback.onPreprocessedMessageReceived(msg);
                }

                if (msg.getMessageType() == Message.MESSAGE_TYPE_PING) {
                    Log.d(TAG, "Content: " + Utils.bytesToHexString(msg.getMessageData()));
                    Toast.makeText(context, "Pong", Toast.LENGTH_SHORT).show();
                }
                if(msg.getMessageType() == Message.MESSAGE_TYPE_CFIG){
                    for(OnConfigurationMessageReceived listener : onConfigurationMessageReceivedListeners){
                        listener.onConfigurationMessageReceived(msg);
                    }
                }
                if (msg.getMessageType() == Message.MESSAGE_TYPE_NOTI) {
                    for(OnNotificationMessageReceived listener : onNotificationMessageReceivedListeners){
                        listener.onNotificationMessageReceived(msg);
                    }
                }
                if (msg.getMessageType() == Message.MESSAGE_TYPE_DATA) {
                    for(OnDataMessageReceived listener : onDataMessageReceivedListeners){
                        listener.onDataMessageReceived(msg);
                    }
                }
            }
            if(lastProcessedMessageID.size() > Constants.USED_MESSAGE_ID_BUFFER_SIZE) {
                lastProcessedMessageID.remove(0);
            }
        }
        else {
            //Log.d(TAG, "not for me dont process");
        }
    }

    public boolean setOnPreprocessedMessageReceivedListener(OnPreprocessedMessageReceived listener) {
        if(listener != null) {
            onPreprocessedMessageReceivedListeners.addIfAbsent(listener);
            return true;
        }
        return false;
    }

    public boolean removeOnPreprocessedMessageReceivedListener(OnPreprocessedMessageReceived listener) {
        if(listener != null && onPreprocessedMessageReceivedListeners.contains(listener)) {
            onPreprocessedMessageReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public boolean setOnConfigurationMessageReceivedListener(OnConfigurationMessageReceived listener) {
        if(listener != null) {
            onConfigurationMessageReceivedListeners.addIfAbsent(listener);
            return true;
        }
        return false;
    }

    public boolean removeOnConfigurationMessageReceivedListener(OnConfigurationMessageReceived listener) {
        if(listener != null && onConfigurationMessageReceivedListeners.contains(listener)) {
            onConfigurationMessageReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public boolean setOnNotificationMessageReceivedListener(OnNotificationMessageReceived listener) {
        if(listener != null) {
            onNotificationMessageReceivedListeners.addIfAbsent(listener);
            return true;
        }
        return false;
    }

    public boolean removeOnNotificationMessageReceivedListener(OnNotificationMessageReceived listener) {
        if(listener != null && onNotificationMessageReceivedListeners.contains(listener)) {
            onNotificationMessageReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public boolean setOnDataMessageReceivedListener(OnDataMessageReceived listener) {
        if(listener != null) {
            onDataMessageReceivedListeners.addIfAbsent(listener);
            return true;
        }
        return false;
    }

    public boolean removeOnDataMessageReceivedListener(OnDataMessageReceived listener) {
        if(listener != null && onDataMessageReceivedListeners.contains(listener)) {
            onDataMessageReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }
}
