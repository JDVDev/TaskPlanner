package com.jdv.retail.taskplanner.packet;



import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.exception.InvalidLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by tfi on 31/03/2017.
 */

public class MessageCreator {

    public static Message createMessage(byte[] rawBytes) throws InvalidLengthException{

        if (rawBytes.length != Message.MESSAGE_TOTL_LEN) throw new InvalidLengthException();
        byte[] messageSequence = new byte[Message.MESSAGE_SEQU_LEN];
        byte[] sourceID = new byte[Message.MESSAGE_SRCS_LEN];
        byte[] destinationID = new byte[Message.MESSAGE_DEST_LEN];
        byte messageID;
        byte messageType;
        byte[] messageData = new byte[Message.MESSAGE_DATA_LEN];
        byte[] messageKey = new byte[Message.MESSAGE_EKEY_LEN];
        byte messageTTL;

        System.arraycopy(rawBytes, 0, messageSequence, 0, Message.MESSAGE_SEQU_LEN);
        System.arraycopy(rawBytes, Message.MESSAGE_SEQU_LEN, sourceID, 0, Message.MESSAGE_SRCS_LEN);
        System.arraycopy(rawBytes, Message.MESSAGE_SEQU_LEN + Message.MESSAGE_SRCS_LEN, destinationID, 0, Message.MESSAGE_DEST_LEN);
        messageID = rawBytes[Message.MESSAGE_SEQU_LEN + Message.MESSAGE_SRCS_LEN + Message.MESSAGE_DEST_LEN];
        messageType = rawBytes[Message.MESSAGE_SEQU_LEN + Message.MESSAGE_SRCS_LEN + Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN];
        System.arraycopy(rawBytes, Message.MESSAGE_DATA_OFS, messageData, 0, Message.MESSAGE_DATA_LEN);
        System.arraycopy(rawBytes, Message.MESSAGE_DATA_OFS + Message.MESSAGE_DATA_LEN, messageKey, 0, Message.MESSAGE_EKEY_LEN);
        messageTTL = rawBytes[rawBytes.length - 1];

        Log.d(Constants.TAG, "getRaw " + Utils.bytesToHexString(new Message(messageSequence, sourceID, destinationID, messageID, messageType, messageData, messageKey, messageTTL).getRawBytes()));
        return new Message(messageSequence, sourceID, destinationID, messageID, messageType, messageData, messageKey, messageTTL);
    }

    public static Message createMessage(byte[] messageSequence,
                                        byte[] sourceID,
                                        byte[] destinationID,
                                        byte messageID,
                                        byte messageType,
                                        byte[] messageData) throws InvalidLengthException {

        if (messageSequence.length != Message.MESSAGE_SEQU_LEN) throw new InvalidLengthException();
        if (sourceID.length != Message.MESSAGE_SRCS_LEN) throw new InvalidLengthException();
        if (destinationID.length != Message.MESSAGE_DEST_LEN) throw new InvalidLengthException();
        if (messageData.length != Message.MESSAGE_DATA_LEN) throw new InvalidLengthException();

        byte[] messageKey = createMessageKey(Constants.ENCRYPTION_KEY, messageSequence, sourceID, destinationID, messageID, messageType, messageData);
        Log.d(Constants.TAG, "getRaw " + Utils.bytesToHexString(new Message(messageSequence, sourceID, destinationID, messageID, messageType, messageData, messageKey, Message.TIME_TO_LIVE_HOPPING).getRawBytes()));

        return new Message(messageSequence, sourceID, destinationID, messageID, messageType, messageData, messageKey, Message.TIME_TO_LIVE_HOPPING);
    }

    public static Message createMessage(byte[] messageSequence,
                                        byte[] sourceID,
                                        byte[] destinationID,
                                        byte messageType,
                                        byte[] messageData) throws InvalidLengthException {

        Message message;
        byte messageID = Utils.createMessageIDByte();
        message = createMessage(messageSequence, sourceID, destinationID, messageID, messageType, messageData);
        return message;
    }

    public static Message createMessage(byte[] messageSequence,
                                        byte[] sourceID,
                                        byte messageType,
                                        byte[] messageData) throws InvalidLengthException {

        Message message;
        message = createMessage(messageSequence, sourceID, Message.MESSAGE_ID_BROADCAST, messageType, messageData);
        return message;
    }

    public static byte[] createMessageKey(String key, byte[] seq, byte[] src, byte[] dest, byte msgID, byte msgType, byte[] data) {
        byte[] offset = new byte[8];
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(offset);
            outputStream.write(seq);
            outputStream.write(src);
            outputStream.write(dest);
            outputStream.write(msgID);
            outputStream.write(msgType);
            outputStream.write(data);
            byte appended[] = outputStream.toByteArray();
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hmac = sha256_HMAC.doFinal(appended);
            byte[] result = new byte[8];
            for (int i = 0; i < result.length; i++){
                result[i] = hmac[(hmac.length - 1) - i];
            }
            Log.d(Constants.TAG, "Data " + Utils.bytesToHexString(appended));
            Log.d(Constants.TAG, "Key og " + Utils.bytesToHexString(hmac));
            Log.d(Constants.TAG, "Key result " + Utils.bytesToHexString(result));
            return result;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | IOException e){
            e.printStackTrace();
        }
        return null;
    }

}
