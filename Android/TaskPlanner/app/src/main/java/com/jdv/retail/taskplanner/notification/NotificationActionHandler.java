/*
Copyright 2016 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.jdv.retail.taskplanner.notification;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.bluetooth.BleAdvertiser;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;

/**
 * Asynchronously handles snooze and dismiss actions for reminder app (and active Notification).
 * Notification for for reminder app uses BigTextStyle.
 */
public class NotificationActionHandler extends IntentService {

    private static final String TAG = "NotificationAction";

    public static final String ACTION_ACCEPT =
            "com.jdv.retail.taskplanner.notification.handlers.action.ACCEPT";
    public static final String ACTION_DISMISS =
            "com.jdv.retail.taskplanner.notification.handlers.action.DISMISS";


    public NotificationActionHandler() {
        super("NotificationActionHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(): " + intent);
        if (intent != null) {
            final String action = intent.getAction();
            final byte[] contentData = intent.getByteArrayExtra(NotificationHandler.NOTIFICATION_CONTENT_DATA_KEY);
            Log.d(TAG, "ContentData: " + intent.getExtras());
            if (ACTION_ACCEPT.equals(action)) {
                handleActionAccept(contentData);
            }
            else if (ACTION_DISMISS.equals(action)) {
                handleActionDismiss(contentData);
            }
        }
    }

    /**
     * Handles action Snooze in the provided background thread.
     */
    private void handleActionAccept(byte[] contentData) {
        Log.d(TAG, "handleActionAccept()");
        sendAction((byte)0x01, contentData);
    }

    /**
     * Handles action Dismiss in the provided background thread.
     */
    private void handleActionDismiss(byte[] contentData) {
        Log.d(TAG, "handleActionDismiss()");
        sendAction((byte)0x02, contentData);
    }


    private void sendAction(byte action, byte[] ogMessageContent){
        try {
            byte[] messageData = Message.getEmptyMessageData();
            messageData[0] = 0x04;  //Set reaction
            messageData[1] = action; //Reaction
            messageData[2] = ogMessageContent[0]; //Set og question that was send to identify reaction to a specific question
            messageData[3] = ogMessageContent[1];
            Message message = MessageCreator.createMessage(
                    Constants.MESSAGE_SEQUENCE,
                    Utils.getDeviceID(getApplicationContext()),
                    Constants.BASESTATION_ID,
                    Message.MESSAGE_TYPE_DATA,
                    messageData);
            BleAdvertiser.getInstance().sendAdvertising(message);
            new NotificationHandler(getApplicationContext()).dismissNotification();
        }
        catch (InvalidLengthException e){
            e.printStackTrace();
        }
    }
}