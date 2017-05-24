package com.jdv.retail.taskplanner.packet;

import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;

/**
 * Created by tfi on 31/03/2017.
 */

public class Message {
    public static final byte MESSAGE_TYPE_PING = (byte) 0x00;
    public static final byte MESSAGE_TYPE_DATA = (byte) 0x01;
    public static final byte MESSAGE_TYPE_POLL = (byte) 0x02;
    public static final byte MESSAGE_TYPE_CFIG = (byte) 0x03;
    public static final byte MESSAGE_TYPE_NOTI = (byte) 0x04;
    public static final byte MESSAGE_TYPE_UNKNOWN = (byte) 0xFF;

    public static final byte MESSAGE_ID_BROADCAST = (byte) 0x00;

    public static final int MESSAGE_LEN = 20;
    public static final int MESSAGE_DATA_LEN = 16;
    public static final int MESSAGE_DATA_OFFSET = 4;

    private static final byte[] EMPTY_MESSAGE = new byte[MESSAGE_LEN];
    private static final byte[] EMPTY_MESSAGE_DATA = new byte[MESSAGE_DATA_LEN];

    private byte messageID;
    private byte deviceID;
    private byte sendToDeviceID;
    private byte messageType;
    private byte[] messageData = new byte[MESSAGE_DATA_LEN];

    public Message(byte mID, byte dID, byte stID, byte mType, byte[] mD) throws InvalidMessageDataLengthException {
        if (mD.length != messageData.length) throw new InvalidMessageDataLengthException();
        messageID = mID;
        deviceID = dID;
        sendToDeviceID = stID;
        messageType = mType;
        messageData = mD;
    }

    public Message(byte[] rawContent) throws InvalidMessageDataLengthException{
        if (rawContent.length != MESSAGE_LEN) throw new InvalidMessageDataLengthException();
        messageID = rawContent[0];
        deviceID = rawContent[1];
        sendToDeviceID = rawContent[2];
        messageType = rawContent[3];

        int offset = rawContent.length - messageData.length;
        System.arraycopy(rawContent, offset , messageData, 0, messageData.length);
    }

    public byte getMessageID() {
        return messageID;
    }

    public byte getDeviceID() {
        return deviceID;
    }

    public byte getSendToDeviceID() {
        return sendToDeviceID;
    }

    public byte getMessageType() {
        return messageType;
    }

    public byte[] getMessageData() {
        return messageData;
    }

    public byte[] getRawBytes(){
        byte[] rawMSG = new byte[MESSAGE_LEN];
        rawMSG[0] = messageID;
        rawMSG[1] = deviceID;
        rawMSG[2] = sendToDeviceID;
        rawMSG[3] = messageType;
        System.arraycopy(messageData, 0, rawMSG, MESSAGE_DATA_OFFSET, MESSAGE_DATA_LEN);

        return rawMSG;
    }

    public static byte[] getEmptyMessage() {
        return EMPTY_MESSAGE;
    }

    public static byte[] getEmptyMessageData() {
        return EMPTY_MESSAGE_DATA;
    }

    public static int getMessageDataOffset() {
        return MESSAGE_DATA_OFFSET;
    }

    public static int getMessageDataLen() {
        return MESSAGE_DATA_LEN;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("MsgID:" + String.format("%02X", messageID));
        sb.append(" ");
        sb.append("DeviceID:" + String.format("%02X", deviceID));
        sb.append(" ");
        sb.append("SendToDeviceID:" + String.format("%02X", sendToDeviceID));
        sb.append(" ");
        sb.append("MsgType:" + String.format("%02X", messageType));
        sb.append(" ");
        sb.append("MsgData:");
        for(byte b : messageData) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
