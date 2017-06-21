package com.jdv.retail.taskplanner.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.R;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.bluetooth.BleAdvertiser;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;
import com.jdv.retail.taskplanner.packet.DiscoveryResultToMessageHandler;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;

public class DemoSnakeActivity extends WearableActivity implements
        DiscoveryResultToMessageHandler.OnConfigurationMessageReceived {
    private BoxInsetLayout mContainerView;
    private ImageView arrowUp;
    private ImageView arrowDown;
    private ImageView arrowRight;
    private ImageView arrowLeft;
    private Context context;
    private DiscoveryResultToMessageHandler discoveryResultToMessageHandler;
    private int tapCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_snake);
        setAmbientEnabled();

        context = this;

        discoveryResultToMessageHandler = DiscoveryResultToMessageHandler.getInstance();
        discoveryResultToMessageHandler.setOnConfigurationMessageReceivedListener(this);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        arrowUp = (ImageView) findViewById(R.id.imageButtonUp);
        arrowDown = (ImageView) findViewById(R.id.imageButtonDown);
        arrowRight = (ImageView) findViewById(R.id.imageButtonRight);
        arrowLeft = (ImageView) findViewById(R.id.imageButtonLeft);
        arrowUp.setOnClickListener(new arrowClick());
        arrowDown.setOnClickListener(new arrowClick());
        arrowRight.setOnClickListener(new arrowClick());
        arrowLeft.setOnClickListener(new arrowClick());
    }

    private class arrowClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            byte action;
            switch (v.getId()){
                case R.id.imageButtonUp:
                    action = 0x01;
                    break;
                case R.id.imageButtonDown:
                    action = 0x02;
                    break;
                case R.id.imageButtonRight:
                    action = 0x03;
                    break;
                case R.id.imageButtonLeft:
                    action = 0x04;
                    break;
                default:
                    action = 0x00;
            }
            try {
                byte[] messageData = Message.getEmptyMessageData();
                messageData[0] = 0x06;
                messageData[1] = action;
                Message message = MessageCreator.createMessage(
                        Constants.MESSAGE_SEQUENCE,
                        Utils.getDeviceID(context),
                        Constants.BASESTATION_ID,
                        Message.MESSAGE_TYPE_DATA,
                        messageData
                );
                BleAdvertiser.getInstance().sendAdvertising(message);
            }
            catch (InvalidLengthException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationMessageReceived(Message msg) {
        if(msg.getMessageData()[0] == (byte)0xFF){
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
