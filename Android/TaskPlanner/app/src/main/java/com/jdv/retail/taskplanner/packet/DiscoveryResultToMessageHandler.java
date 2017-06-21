package com.jdv.retail.taskplanner.packet;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.bluetooth.BleDiscoveryService;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;

import java.util.ArrayList;
import java.util.Arrays;
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
        byte[] messageBytes = null;
        try {
            messageBytes = result.getScanRecord().getServiceData(Constants.SERVICE_UUID);
        } catch (NullPointerException e){ //No Service data ignore message
            e.printStackTrace();
        }
        if(messageBytes != null) { //Ignore if no scanRecord or no service data
            //Log.d(TAG, "Procesmessage: " + Utils.bytesToHexString(messageBytes));
            processesMessageContent(messageBytes, context);
        }
    }

    private void processesMessageContent(byte[] content, Context context){
        Message msg;
        try {
            msg = MessageCreator.createMessage(content);
            byte[] tempData = msg.getMessageData();
            tempData[tempData.length - 1] = 0x00;// Reset receiver ID, remove code if non proof of concept code
            byte[] hash = msg.getMessageKey();
            byte[] newHash = MessageCreator.createMessageKey(
                    Constants.ENCRYPTION_KEY,
                    msg.getMessageSequence(),
                    msg.getSourceID(),
                    msg.getDestinationID(),
                    msg.getMessageID(),
                    msg.getMessageType(),
                    tempData);
            Log.d(TAG, "hash: " + Utils.bytesToHexString(hash) + " newHash: " + Utils.bytesToHexString(newHash));
            if(!Arrays.equals(hash, newHash)) return;//Check hash to see if it is for this network.
        }
        catch (InvalidLengthException e){
            Log.d(TAG, "Length invalid, discarding message");
            e.printStackTrace();
            return;
        }

        if(Arrays.equals(msg.getDestinationID(), Utils.getDeviceID(context)) || Arrays.equals(msg.getDestinationID(), Message.MESSAGE_ID_BROADCAST)){
            //Log.d(TAG, "Process content: " + Utils.bytesToHexString(content));

            if(!lastProcessedMessageID.contains(msg.getMessageID())) {
                Log.d(TAG, "lastProcessedMessageID !contains: " + msg.getMessageID());
                lastProcessedMessageID.add(msg.getMessageID());
                Log.d(TAG, "lastProcessedMessageID: " + lastProcessedMessageID);

                Log.d(TAG, "Process message: " + msg);
                for (OnPreprocessedMessageReceived callback : onPreprocessedMessageReceivedListeners){
                    callback.onPreprocessedMessageReceived(msg);
                }

                if (msg.getMessageType() == Message.MESSAGE_TYPE_PING) {
                    Toast.makeText(context,
                            "Pong " + BleDiscoveryService.getTotalPackets() +
                                    " " +
                                    Utils.bytesToHexString(msg.getMessageTTL()),
                            Toast.LENGTH_SHORT).show();
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
