package com.jdv.retail.taskplanner.packet;

import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageLengthException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by tfi on 31/03/2017.
 */

public class Message {
    public static final byte TIME_TO_LIVE_HOPPING = 0x0F;

    public static final byte MESSAGE_TYPE_PING = (byte) 0x00;
    public static final byte MESSAGE_TYPE_DATA = (byte) 0x01;
    public static final byte MESSAGE_TYPE_POLL = (byte) 0x02;
    public static final byte MESSAGE_TYPE_CFIG = (byte) 0x03;
    public static final byte MESSAGE_TYPE_NOTI = (byte) 0x04;
    public static final byte MESSAGE_TYPE_UNKNOWN = (byte) 0xFF;

    public static final byte[] MESSAGE_ID_BROADCAST =  {0x00, 0x00};

    public static final int MESSAGE_TOTL_LEN = 24;
    public static final int MESSAGE_SEQU_LEN = 3;
    public static final int MESSAGE_SRCS_LEN = 2;
    public static final int MESSAGE_DEST_LEN = 2;
    public static final int MESSAGE_UNID_LEN = 1;
    public static final int MESSAGE_TYPE_LEN = 1;

    public static final int MESSAGE_DATA_OFS = (MESSAGE_SEQU_LEN +
                                                MESSAGE_SRCS_LEN +
                                                MESSAGE_DEST_LEN +
                                                MESSAGE_UNID_LEN +
                                                MESSAGE_TYPE_LEN); //Payload comes after Sequence, SourceID, DestinationID, MSG ID and MSG Type and before encryption key and ttl

    public static final int MESSAGE_EKEY_LEN = 8;
    public static final int MESSAGE_TTLH_LEN = 1;
    public static final int MESSAGE_DATA_LEN =
            MESSAGE_TOTL_LEN - (MESSAGE_SEQU_LEN +
                                MESSAGE_SRCS_LEN +
                                MESSAGE_DEST_LEN +
                                MESSAGE_UNID_LEN +
                                MESSAGE_TYPE_LEN +
                                MESSAGE_EKEY_LEN +
                                MESSAGE_TTLH_LEN); //6


    private static final byte[] EMPTY_MESSAGE = new byte[MESSAGE_TOTL_LEN];
    private static final byte[] EMPTY_MESSAGE_DATA = new byte[MESSAGE_DATA_LEN];

    private byte[] messageSequence = new byte[MESSAGE_SEQU_LEN];
    private byte[] sourceID = new byte[MESSAGE_SRCS_LEN];
    private byte[] destinationID = new byte[MESSAGE_DEST_LEN];
    private byte messageID;
    private byte messageType;
    private byte[] messageData = new byte[MESSAGE_DATA_LEN];
    private byte[] messageKey = new byte[MESSAGE_EKEY_LEN];
    private byte messageTTL;


    public Message(byte[] mSeq, byte[] sID, byte[] dID, byte mID, byte mType, byte[] mD, byte[] mKey, byte mTTL) throws InvalidLengthException {
        if (mSeq.length != MESSAGE_SEQU_LEN) throw new InvalidLengthException();
        if (sID.length != MESSAGE_SRCS_LEN) throw new InvalidLengthException();
        if (dID.length != MESSAGE_DEST_LEN) throw new InvalidLengthException();
        if (mD.length != MESSAGE_DATA_LEN) throw new InvalidLengthException();
        if (mKey.length != MESSAGE_EKEY_LEN) throw new InvalidLengthException();
        messageSequence = mSeq;
        sourceID = sID;
        destinationID = dID;
        messageID = mID;
        messageType = mType;
        messageData = mD;
        messageKey = mKey;
        messageTTL = mTTL;
    }

    public byte[] getMessageSequence() {
        return messageSequence;
    }

    public byte[] getSourceID() {
        return sourceID;
    }

    public byte[] getDestinationID() {
        return destinationID;
    }

    public byte getMessageID() {
        return messageID;
    }

    public byte getMessageType() {
        return messageType;
    }

    public byte[] getMessageData() {
        return messageData;
    }

    public byte[] getMessageKey() {
        return messageKey;
    }

    public byte getMessageTTL() {
        return messageTTL;
    }

    public byte[] getRawBytes(){
        byte[] rawMSG = EMPTY_MESSAGE;
        System.arraycopy(messageSequence, 0, rawMSG, 0, Message.MESSAGE_SEQU_LEN);
        System.arraycopy(sourceID, 0, rawMSG, MESSAGE_SEQU_LEN, Message.MESSAGE_SRCS_LEN);
        System.arraycopy(destinationID, 0, rawMSG, MESSAGE_SEQU_LEN + MESSAGE_SRCS_LEN, Message.MESSAGE_DEST_LEN);
        rawMSG[7] = messageID;
        rawMSG[8] = messageType;
        System.arraycopy(messageData, 0, rawMSG, MESSAGE_DATA_OFS, MESSAGE_DATA_LEN);
        System.arraycopy(messageKey, 0, rawMSG, MESSAGE_DATA_OFS + MESSAGE_DATA_LEN, MESSAGE_EKEY_LEN);
        rawMSG[MESSAGE_TOTL_LEN - 1] = messageTTL;

        return rawMSG;
    }

    public static byte[] getEmptyMessage() {
        return EMPTY_MESSAGE;
    }

    public static byte[] getEmptyMessageData() {
        return EMPTY_MESSAGE_DATA;
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("MsgSequence: ");
        sb.append(Utils.bytesToHexString(messageSequence));
        sb.append(" ");
        sb.append("SourceID: ");
        sb.append(Utils.bytesToHexString(sourceID));
        sb.append(" ");
        sb.append("DestinationID: ");
        sb.append(Utils.bytesToHexString(destinationID));
        sb.append(" ");
        sb.append("MsgID: ");
        sb.append(Utils.bytesToHexString(messageID));
        sb.append(" ");
        sb.append("MsgType: ");
        sb.append(Utils.bytesToHexString(messageType));
        sb.append(" ");
        sb.append("MsgData: ");
        for(byte b : messageData) {
            sb.append(String.format("%02x", b));
        }
        sb.append(" ");
        sb.append("MsgKey: ");
        sb.append(Utils.bytesToHexString(messageKey));
        sb.append(" ");
        sb.append("MsgTTL: ");
        sb.append(Utils.bytesToHexString(messageTTL));
        return sb.toString();
    }
}
