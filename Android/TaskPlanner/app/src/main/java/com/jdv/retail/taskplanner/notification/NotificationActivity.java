package com.jdv.retail.taskplanner.notification;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.R;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.bluetooth.BleAdvertiser;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.listadapter.NotificationActionRecycleAdapter;
import com.jdv.retail.taskplanner.notification.DismissActionTimerActivity;
import com.jdv.retail.taskplanner.notification.NotificationAction;
import com.jdv.retail.taskplanner.notification.NotificationData;
import com.jdv.retail.taskplanner.packet.DiscoveryResultToMessageHandler;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationActivity extends WearableActivity implements
        NotificationActionRecycleAdapter.OnItemClickedCallback,
        DiscoveryResultToMessageHandler.OnConfigurationMessageReceived{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private static final int REQUESTCODE = 528;

    private Context context;

    private BoxInsetLayout mContainerView;
    private TextView mContentTextView;
    private TextView mClockView;
    private TextView mTextIDView;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NotificationActionRecycleAdapter listAdapter;

    private NotificationData notificationData;
    private ArrayList<NotificationAction> notificationActions = new ArrayList<>();

    private Message prePreparedMessage;

    private DiscoveryResultToMessageHandler discoveryResultToMessageHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        setAmbientEnabled();

        context = this;

        mTextIDView = (TextView) findViewById(R.id.textViewID);
        mTextIDView.setText(Utils.bytesToHexString(Utils.getDeviceID(context)).toUpperCase());

        notificationData = getIntent().getParcelableExtra(Constants.NOTIFICATION_DATA_KEY);
        notificationActions = notificationData.getActions();

        discoveryResultToMessageHandler = DiscoveryResultToMessageHandler.getInstance();
        discoveryResultToMessageHandler.setOnConfigurationMessageReceivedListener(this);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mContentTextView = (TextView) findViewById(R.id.contentText);
        mClockView = (TextView) findViewById(R.id.clock);
        mTextIDView = (TextView) findViewById(R.id.textViewID);

        mContentTextView.setText(notificationData.getContentText());

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_launcher_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        listAdapter = new NotificationActionRecycleAdapter(mRecyclerView, notificationActions, this);
        mRecyclerView.setAdapter(listAdapter);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 2000, 1000, 2000};
        v.vibrate(pattern, -1);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mClockView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        }
        else {
            mContainerView.setBackground(null);
            mClockView.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public void onListItemClicked(int position) {
        NotificationAction action = notificationActions.get(position);
        Log.d(Constants.TAG, "Item clicked: " +  action);
        Intent intent = new Intent(context, DismissActionTimerActivity.class);
        intent.putExtra(DismissActionTimerActivity.TITLE_KEY, "Send action");
        intent.putExtra(DismissActionTimerActivity.CONTENT_KEY, action.getActionText());
        try {
            byte[] messageData = Message.getEmptyMessageData();
            messageData[0] = notificationData.getNotificationID()[0];
            messageData[1] = notificationData.getNotificationID()[1];
            messageData[2] = action.getActionID();
            prePreparedMessage = MessageCreator.createMessage(
                    Utils.getDeviceID(context),
                    Constants.BASESTATION_ID,
                    Message.MESSAGE_TYPE_NOTI,
                    messageData
            );
        } catch (InvalidMessageDataLengthException e) {
            e.printStackTrace();
        }
        startActivityForResult(intent, REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                finish();
                Intent intent = new Intent(this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        "Action send");
                startActivity(intent);
                if(prePreparedMessage != null) {
                    sendChosenAction(prePreparedMessage);
                }
            }
            if(resultCode == RESULT_CANCELED) {
                Intent intent = new Intent(this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.FAILURE_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        "Action not send");
                startActivity(intent);
            }
        }
    }

    private void sendChosenAction(Message message){
        BleAdvertiser.getInstance().sendAdvertising(message);
    }

    @Override
    public void onConfigurationMessageReceived(Message msg) {
        if (msg.getMessageData()[0] == (byte) 0xFF) {
            discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
    }
}
