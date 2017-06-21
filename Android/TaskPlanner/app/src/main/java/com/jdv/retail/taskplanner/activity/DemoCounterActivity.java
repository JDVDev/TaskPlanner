package com.jdv.retail.taskplanner.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.TextView;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.bluetooth.BleDiscoveryService;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;
import com.jdv.retail.taskplanner.packet.DiscoveryResultToMessageHandler;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;
import com.jdv.retail.taskplanner.R;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.bluetooth.BleAdvertiser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class DemoCounterActivity extends WearableActivity implements
        DiscoveryResultToMessageHandler.OnConfigurationMessageReceived,
        DiscoveryResultToMessageHandler.OnDataMessageReceived {

    private final static String TAG = "DemoCounterActivity";
    private Context context;
    private BoxInsetLayout mContainerView;
    private TextView mReceivedView;
    private TextView mSendView;
    private DiscoveryResultToMessageHandler discoveryResultToMessageHandler;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_counter);
        setAmbientEnabled();

        context = this;

        final TextView textViewId = (TextView) findViewById(R.id.textViewID);
        textViewId.setText(Utils.bytesToHexString(Utils.getDeviceID(context)).toUpperCase());

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mReceivedView = (TextView) findViewById(R.id.textViewReceiveCounter);
        mSendView = (TextView) findViewById(R.id.textViewSendCounter);

        discoveryResultToMessageHandler = DiscoveryResultToMessageHandler.getInstance();
        discoveryResultToMessageHandler.setOnConfigurationMessageReceivedListener(this);
        discoveryResultToMessageHandler.setOnDataMessageReceivedListener(this);

    }

    @Override
    public void onConfigurationMessageReceived(Message msg) {
        if(msg.getMessageData()[0] == (byte)0xFF){
            discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", true);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }

    @Override
    public void onDataMessageReceived(Message msg) {
        if (msg.getMessageData()[0] == 0x03) {
            Log.d(TAG, "Received demo counter :" + Utils.bytesToHexString(msg.getMessageData()[1]));
            mReceivedView.setText(Utils.bytesToDecimalString(msg.getMessageData()[1]));
            try {
                byte[] dataBytes = Message.getEmptyMessageData();
                dataBytes[0] = 0x03;
                dataBytes[1] = (byte) (msg.getMessageData()[1] + 0x01);

                int totalPackets = BleDiscoveryService.getTotalPackets();
                Log.d(TAG, "Total packets: " + totalPackets);
                dataBytes[2] = (byte) (totalPackets & 0xFF); //get lowest 8 bits
                dataBytes[3] = (byte) ((totalPackets >>> 8) & 0xFF); //shift 8 bits right without carry ang get lowest 8 bits

                dataBytes[dataBytes.length - 1] = msg.getMessageData()[msg.getMessageData().length - 1]; //ID of raspi for demo counter
                Message sendingMsg = MessageCreator.createMessage(
                        Constants.MESSAGE_SEQUENCE,
                        Utils.getDeviceID(context),
                        Constants.BASESTATION_ID,
                        (byte) (msg.getMessageID() + 0x01),
                        Message.MESSAGE_TYPE_DATA,
                        dataBytes);
                Log.d(TAG, "Sending demo msg " + sendingMsg);
                mSendView.setText(Utils.bytesToDecimalString(sendingMsg.getMessageData()[1]));
                BleAdvertiser.getInstance().sendAdvertising(sendingMsg);
            } catch (InvalidLengthException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        discoveryResultToMessageHandler.removeOnConfigurationMessageReceivedListener(this);
        discoveryResultToMessageHandler.removeOnDataMessageReceivedListener(this);
    }
}
