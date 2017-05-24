package com.jdv.retail.taskplanner.notification;

import android.os.Parcel;
import android.os.Parcelable;

import com.jdv.retail.taskplanner.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tfi on 16/05/2017.
 */

public class NotificationData implements Parcelable {
    private byte[] notificationID = new byte[2];
    private String contentText;
    private ArrayList<NotificationAction> actions = new ArrayList<>();

    public NotificationData(byte[] notificationID, String contentText, ArrayList<NotificationAction> actions) {
        this.notificationID = notificationID;
        this.contentText = contentText;
        this.actions = actions;
    }

    public byte[] getNotificationID() {
        return notificationID;
    }

    public String getContentText() {
        return contentText;
    }

    public ArrayList<NotificationAction> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NotificationID: ");
        sb.append(Utils.bytesToHexString(notificationID));
        sb.append(" ");
        sb.append("ContentText: ");
        sb.append(contentText);
        sb.append(" ");
        sb.append("Actions: ");
        sb.append("\n");
        for (NotificationAction action : actions) {
            sb.append("Key: ");
            sb.append(Utils.bytesToHexString(action.getActionID()));
            sb.append(" ");
            sb.append("Data: ");
            sb.append(action.getActionText());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.notificationID);
        dest.writeString(this.contentText);
        dest.writeSerializable(this.actions);
    }

    protected NotificationData(Parcel in) {
        this.notificationID = in.createByteArray();
        this.contentText = in.readString();
        this.actions = (ArrayList<NotificationAction>) in.readSerializable();
    }

    public static final Parcelable.Creator<NotificationData> CREATOR = new Parcelable.Creator<NotificationData>() {
        @Override
        public NotificationData createFromParcel(Parcel source) {
            return new NotificationData(source);
        }

        @Override
        public NotificationData[] newArray(int size) {
            return new NotificationData[size];
        }
    };
}