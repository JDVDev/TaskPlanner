package com.jdv.retail.taskplanner.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.Arrays;
import java.util.Date;
import java.util.logging.ConsoleHandler;

public class DemoTapperActivity extends WearableActivity implements
        DiscoveryResultToMessageHandler.OnConfigurationMessageReceived,
        DiscoveryResultToMessageHandler.OnDataMessageReceived {
    private BoxInsetLayout mContainerView;
    private Button buttonBlue;
    private Button buttonPurple;
    private Button buttonRed;
    private TextView mTextView;
    private TextView mTextCounterView;
    private TextView mTextJoinTeamView;
    private TextView mTextIDView;
    private Context context;
    private DiscoveryResultToMessageHandler discoveryResultToMessageHandler;
    private int tapCounter = 0;
    private boolean isInGame = false;
    private int teamColor = Color.WHITE;
    private boolean isBlueTeamFull = false;
    private boolean isPurpleTeamFull = false;
    private boolean isRedTeamFull = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_tapper);
        setAmbientEnabled();

        context = this;

        mTextIDView = (TextView) findViewById(R.id.textViewID);
        mTextIDView.setText(Utils.bytesToHexString(Utils.getDeviceID(context)).toUpperCase());

        discoveryResultToMessageHandler = DiscoveryResultToMessageHandler.getInstance();
        discoveryResultToMessageHandler.setOnConfigurationMessageReceivedListener(this);
        discoveryResultToMessageHandler.setOnDataMessageReceivedListener(this);

        buttonBlue = (Button) findViewById(R.id.buttonBlue);
        buttonPurple = (Button) findViewById(R.id.buttonPurple);
        buttonRed = (Button) findViewById(R.id.buttonRed);
        buttonBlue.setOnClickListener(new OnTeamButtonClicked());
        buttonPurple.setOnClickListener(new OnTeamButtonClicked());
        buttonRed.setOnClickListener(new OnTeamButtonClicked());

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.taptext);
        mTextCounterView = (TextView) findViewById(R.id.tapcountertext);
        mTextJoinTeamView = (TextView) findViewById(R.id.textViewJoinTeam);
    }

    @Override
    public void onConfigurationMessageReceived(Message msg) {
        if (msg.getMessageData()[0] == (byte) 0xFF) {
            discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
            discoveryResultToMessageHandler.removeOnDataMessageReceivedListener(this);
            finish();
        }
    }

    @Override
    public void onDataMessageReceived(Message msg) {
        if (msg.getMessageData()[0] == (byte) 0x07) {
            if (msg.getMessageData()[1] == (byte) 0x10) {
                setJoinedTeam(Color.BLUE);
            }
            if (msg.getMessageData()[1] == (byte) 0x11) {
                setJoinedTeam(Color.RED);
            }
            if (msg.getMessageData()[1] == (byte) 0x12) {
                setJoinedTeam(Color.GREEN);
            }
            if (msg.getMessageData()[1] == (byte) 0x20 && isInGame) {
                mContainerView.setOnClickListener(null);
                if (Arrays.equals(Arrays.copyOfRange(msg.getMessageData(), 2, 4), Utils.getDeviceID(context))) {
                    mTextView.setText("YOU \nWON!");
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 500, 100, 500, 100, 500, 100, 1000};
                    v.vibrate(pattern, -1);
                } else {
                    mTextView.setText("You \nLost");
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 1000, 100, 500, 100, 500, 100, 500};
                    v.vibrate(pattern, -1);
                }
            }
            if(msg.getMessageData()[1] == (byte)0x40){
                buttonBlue.setEnabled(!isBlueTeamFull);
                buttonRed.setEnabled(!isRedTeamFull);
                buttonPurple.setEnabled(!isPurpleTeamFull);
                if (msg.getMessageData()[2] == (byte) 0x10) {
                    buttonBlue.setText("Full");
                    buttonBlue.setEnabled(false);
                    isBlueTeamFull = true;
                }
                if (msg.getMessageData()[2] == (byte) 0x11) {
                    buttonRed.setText("Full");
                    buttonRed.setEnabled(false);
                    isRedTeamFull = true;
                }
                if (msg.getMessageData()[2] == (byte) 0x12) {
                    buttonPurple.setText("Full");
                    buttonPurple.setEnabled(false);
                    isPurpleTeamFull = true;
                }
            }
        }
    }

    private void setJoinedTeam(int color) {
        try {
            byte[] messageData = Message.getEmptyMessageData();
            messageData[0] = 0x07;
            messageData[1] = 0x50;
            Message message = MessageCreator.createMessage(
                    Constants.MESSAGE_SEQUENCE,
                    Utils.getDeviceID(context),
                    Constants.BASESTATION_ID,
                    Message.MESSAGE_TYPE_DATA,
                    messageData
            );
            BleAdvertiser.getInstance().sendAdvertising(message);
        } catch (InvalidLengthException e) {
            e.printStackTrace();
        }
        teamColor = color;
        mTextIDView.setTextColor(Color.WHITE);
        mTextView.setVisibility(View.VISIBLE);
        mTextJoinTeamView.setVisibility(View.GONE);
        buttonBlue.setVisibility(View.GONE);
        buttonPurple.setVisibility(View.GONE);
        buttonRed.setVisibility(View.GONE);
        mContainerView.setBackgroundColor(color);
        mContainerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    byte[] messageData = Message.getEmptyMessageData();
                    messageData[0] = 0x07;
                    messageData[1] = 0x01;
                    Message message = MessageCreator.createMessage(
                            Constants.MESSAGE_SEQUENCE,
                            Utils.getDeviceID(context),
                            Constants.BASESTATION_ID,
                            Message.MESSAGE_TYPE_DATA,
                            messageData
                    );
                    BleAdvertiser.getInstance().sendAdvertising(message);
                    tapCounter++;
                    mTextCounterView.setText(Integer.toString(tapCounter));
                } catch (InvalidLengthException e) {
                    e.printStackTrace();
                }
            }
        });
        isInGame = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
        discoveryResultToMessageHandler.removeOnDataMessageReceivedListener(this);
    }

    private class OnTeamButtonClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button buttonBlue = (Button) findViewById(R.id.buttonBlue);
            Button buttonPurple = (Button) findViewById(R.id.buttonPurple);
            Button buttonRed = (Button) findViewById(R.id.buttonRed);
            buttonBlue.setEnabled(false);
            buttonPurple.setEnabled(false);
            buttonRed.setEnabled(false);
            try {
                byte[] messageData = Message.getEmptyMessageData();
                messageData[0] = 0x07;
                if (v.getId() == R.id.buttonBlue) {
                    messageData[1] = 0x10;
                    buttonBlue.setText("Joining...");
                }
                else if(v.getId() == R.id.buttonRed) {
                    messageData[1] = 0x11;
                    buttonRed.setText("Joining...");
                }
                else if(v.getId() == R.id.buttonPurple) {
                    messageData[1] = 0x12;
                    buttonPurple.setText("Joining...");
                }
                Message message = MessageCreator.createMessage(
                        Constants.MESSAGE_SEQUENCE,
                        Utils.getDeviceID(context),
                        Constants.BASESTATION_ID,
                        Message.MESSAGE_TYPE_DATA,
                        messageData
                );
                BleAdvertiser.getInstance().sendAdvertising(message);
            } catch (InvalidLengthException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        //updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
            mContainerView.setBackgroundColor(teamColor);
        }
    }

}
