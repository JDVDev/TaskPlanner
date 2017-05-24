package com.jdv.retail.taskplanner.notification;

import com.jdv.retail.taskplanner.Utils;

import java.io.Serializable;

/**
 * Created by tfi on 17/05/2017.
 */

public class NotificationAction implements Serializable{
    private byte actionID;
    private String actionText;

    public NotificationAction(byte id, String at){
        actionID = id;
        actionText = at;
    }

    public byte getActionID() {
        return actionID;
    }

    public String getActionText() {
        return actionText;
    }

    @Override
    public String toString() {
        return "ActionID: " +
                Utils.bytesToHexString(actionID) +
                " " +
                "ActionText: " +
                actionText;
    }
}
