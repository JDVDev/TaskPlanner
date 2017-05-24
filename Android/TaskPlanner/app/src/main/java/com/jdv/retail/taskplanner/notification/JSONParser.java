package com.jdv.retail.taskplanner.notification;

import android.content.Context;
import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.exception.NotificationNotFoundException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tfi on 16/05/2017.
 */

public class JSONParser {

    private static String FILENAME = "TestData.json";

    public static JSONObject loadJSONFromJSONString(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject loadJSONFromJSONInputStream(InputStream is) {
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return loadJSONFromJSONString(new String(buffer, "UTF-8"));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject loadJSONFromJSONAsset(Context context, String filename) {
        try {
            return loadJSONFromJSONInputStream(context.getAssets().open(filename));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<NotificationData> loadNotificationDataFromJSONAsset(Context context) {
        JSONObject jsonObject = loadJSONFromJSONAsset(context, FILENAME);
        ArrayList<NotificationData> notificationDatas = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("notification");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject notification = jsonArray.getJSONObject(i);
                byte[] notificationID = Utils.hexStringToByteArray(notification.getString("id"));
                String contentText = notification.getString("content");
                ArrayList<NotificationAction> actions = new ArrayList<>();
                JSONArray jsonActions = notification.getJSONArray("action");
                for(int j = 0; j < jsonActions.length(); j++){
                    JSONObject jsonActionObject = jsonActions.getJSONObject(j);
                    byte id = Utils.hexStringToByteArray(jsonActionObject.getString("id"))[0];
                    String actionContent = jsonActionObject.getString("content");
                    NotificationAction action = new NotificationAction(id,actionContent);
                    actions.add(action);
                }
                NotificationData notificationData = new NotificationData(notificationID,contentText,actions);
                notificationDatas.add(notificationData);
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return notificationDatas;
    }

    public static NotificationData loadNotificationDataFromJSONAssetByID(Context context, String ID) throws NotificationNotFoundException {
        JSONObject jsonObject = loadJSONFromJSONAsset(context, FILENAME);
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("notification");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject notification = jsonArray.getJSONObject(i);
                String notificationID = notification.getString("id");
                String contentText = notification.getString("content");
                ArrayList<NotificationAction> actions = new ArrayList<>();
                JSONArray jsonActions = notification.getJSONArray("action");
                for(int j = 0; j < jsonActions.length(); j++){
                    JSONObject jsonActionObject = jsonActions.getJSONObject(j);
                    byte id = Utils.hexStringToByteArray(jsonActionObject.getString("id"))[0];
                    String actionContent = jsonActionObject.getString("content");
                    NotificationAction action = new NotificationAction(id,actionContent);
                    actions.add(action);
                }
                if(notificationID.equals(ID)) {
                    Log.d(Constants.TAG, "ID lenght" + Utils.hexStringToByteArray(notificationID).length);
                    return new NotificationData(Utils.hexStringToByteArray(notificationID), contentText, actions);
                }
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        throw new NotificationNotFoundException();
    }
}
