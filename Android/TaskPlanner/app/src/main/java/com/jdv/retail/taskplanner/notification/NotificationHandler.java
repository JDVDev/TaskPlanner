package com.jdv.retail.taskplanner.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.exception.NotificationNotFoundException;
import com.jdv.retail.taskplanner.packet.DiscoveryResultToMessageHandler;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.R;
import com.jdv.retail.taskplanner.Utils;

import java.util.Arrays;

public class NotificationHandler implements DiscoveryResultToMessageHandler.OnNotificationMessageReceived {

    public final static String TAG = "NotificationHandler";
    public final static int NOTIFICATION_ID = 231;
    public final static String NOTIFICATION_CONTENT_DATA_KEY = "contentData";
    private NotificationManagerCompat notificationManagerCompat;
    private Context context;

    public NotificationHandler(Context context){
        this.context = context;
        notificationManagerCompat =
                NotificationManagerCompat.from(context);
    }

    public void notify(String contentText, byte[] contentData){
        Log.d(Constants.TAG, "Notify user: " + contentText);
        // Issue the notification with notification manager.
        notificationManagerCompat.notify(NOTIFICATION_ID, generateBigTextStyleNotification(context, contentText, contentData, true, true));
    }

    public void dismissNotification(){
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(
                NotificationHandler.NOTIFICATION_ID, generateBigTextStyleNotification(context, null, null, false, false));
    }

    public Notification getDefaultNotification(){
        return generateBigTextStyleNotification(context, null, null, false, false);
    }

    @Override
    public void onNotificationMessageReceived(Message msg) {
        String notificationID = Utils.bytesToHexString(Arrays.copyOfRange(msg.getMessageData(), 0, 2));
        try {
            NotificationData notificationData =
                    JSONParser.loadNotificationDataFromJSONAssetByID(context, notificationID);
            Intent intent = generateNewStyleNotification(notificationData);
            context.startActivity(intent);
        }
        catch (NotificationNotFoundException e){
            e.printStackTrace();
            // TODO: Action when notification is not found
            // Maybe device doesn't have the latest notifications available.
            // Check notifications version with server.
            // If latest version probably malformed data so ignore
            // Else tell user device needs to sync
        }
    }

    private void notifyUser(String content, byte[] contentData){
        if (Utils.isNotify(context)) {
            notify(content, contentData);
        }
    }


    private Intent generateNewStyleNotification(NotificationData notificationData){
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.NOTIFICATION_DATA_KEY, notificationData);
        return intent;
    }

    private  Notification generateBigTextStyleNotification(
            Context context,
            String contentText,
            byte[] contentData,
            boolean addActions,
            boolean vibrate) {

        Log.d(Constants.TAG, "generateBigTextStyleNotification()");

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get your data
        //      1. Build the BIG_TEXT_STYLE
        //      2. Set up main Intent for notification
        //      3. Create additional Actions for the Notification
        //      4. Build and issue the notification

        // 0. Get your data (everything unique per Notification)

        // 1. Build the BIG_TEXT_STYLE
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                // Overrides ContentText in the big form of the template
                if(contentText != null && !contentText.equals("")) {
                    bigTextStyle.bigText(contentText);
                }
                else {
                    bigTextStyle.bigText("No notification");
                }
                // Overrides ContentTitle in the big form of the template
                bigTextStyle.setBigContentTitle(context.getString(R.string.app_name));
                // Summary line after the detail section in the big form of the template
                // Note: To improve readability, don't overload the user with info. If Summary Text
                // doesn't add critical information, you should skip it.
                //.setSummaryText(contentText);


        // 2. Set up main Intent for notification
        /*Intent mainIntent = new Intent(context, BigTextMainActivity.class);

        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );*/


        // 4. Build and issue the notification

        // Because we want this to be a new notification (not updating a previous notification), we
        // create a new Builder. Later, we use the same global builder to get back the notification
        // we built here for the snooze action, that is, canceling the notification and relaunching
        // it several seconds later.

        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(context);


        notificationCompatBuilder
                // BIG_TEXT_STYLE sets title and content
                .setStyle(bigTextStyle)
                .setContentTitle(context.getString(R.string.app_name) + " (" + Utils.bytesToHexString(Utils.getDeviceID(context)).toUpperCase() + ")")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        context.getResources(), R.mipmap.ic_launcher))

                // Set primary color (important for Wear 2.0 Notifications)
                .setColor(context.getResources().getColor(R.color.card_default_background))

                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_HIGH)

                // Shows content on the lock-screen
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        // Sets vibration
        if(vibrate) {
            notificationCompatBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setVibrate(new long[]{0, 2000, 1000, 2000});
        }

        // Adds additional actions
        if(addActions && contentData != null) {
            // 3. Create additional Actions (Intents) for the Notification

            // In our case, we create two additional actions: a Snooze action and a Dismiss action.
            // Accept Action
            Intent acceptIntent = new Intent(context, NotificationActionHandler.class);
            acceptIntent.setAction(NotificationActionHandler.ACTION_ACCEPT);
            acceptIntent.putExtra(NOTIFICATION_CONTENT_DATA_KEY, contentData); //Pass content of message to be able specify on what message a reaction is
            PendingIntent acceptPendingIntent = PendingIntent.getService(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action acceptAction =
                    new NotificationCompat.Action.Builder(
                            R.drawable.ic_cc_checkmark,
                            "Accept",
                            acceptPendingIntent)
                            .build();

            // Dismiss Action
            Intent dismissIntent = new Intent(context, NotificationActionHandler.class);
            dismissIntent.setAction(NotificationActionHandler.ACTION_DISMISS);
            dismissIntent.putExtra(NOTIFICATION_CONTENT_DATA_KEY, contentData);

            PendingIntent dismissPendingIntent = PendingIntent.getService(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action dismissAction =
                    new NotificationCompat.Action.Builder(
                            R.drawable.ic_full_cancel,
                            "Dismiss",
                            dismissPendingIntent)
                            .build();

            notificationCompatBuilder.addAction(acceptAction);
            if(!"Give me a cookie".equals(contentText)) {
                notificationCompatBuilder.addAction(dismissAction);
            }
        }


        /* REPLICATE_NOTIFICATION_STYLE_CODE:
         * You can replicate Notification Style functionality on Wear 2.0 (24+) by not setting the
         * main content intent, that is, skipping the call setContentIntent(). However, you need to
         * still allow the user to open the native Wear app from the Notification itself, so you
         * add an action to launch the app.
         */
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enables launching app in Wear 2.0 while keeping the old Notification Style behavior.
            NotificationCompat.Action mainAction = new NotificationCompat.Action.Builder(
                    R.mipmap.ic_launcher,
                    "Open",
                    mainPendingIntent)
                    .build();

            notificationCompatBuilder.addAction(mainAction);

        } else {
            // Wear 1.+ still functions the same, so we set the main content intent.
            notificationCompatBuilder.setContentIntent(mainPendingIntent);
        }*/


        return notificationCompatBuilder.build();
    }
}
