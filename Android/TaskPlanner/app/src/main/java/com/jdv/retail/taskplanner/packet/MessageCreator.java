package com.jdv.retail.taskplanner.packet;



import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.exception.InvalidMessageDestinationLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageLengthException;
import com.jdv.retail.taskplanner.exception.InvalidMessageSourceLengthException;

/**
 * Created by tfi on 31/03/2017.
 */

public class MessageCreator {

    public static Message createMessage(byte[] rawBytes) throws InvalidMessageLengthException,
            InvalidMessageSourceLengthException,
            InvalidMessageDestinationLengthException,
            InvalidMessageDataLengthException{

        if (rawBytes.length != Message.MESSAGE_TOTL_LEN) throw new InvalidMessageLengthException();
        byte[] sourceID = new byte[Message.MESSAGE_SRCS_LEN];
        byte[] destinationID = new byte[Message.MESSAGE_DEST_LEN];
        byte messageID;
        byte messageType;
        byte[] messageData = new byte[Message.MESSAGE_DATA_LEN];

        System.arraycopy(rawBytes, 0, sourceID, 0, Message.MESSAGE_SRCS_LEN);
        System.arraycopy(rawBytes, Message.MESSAGE_SRCS_LEN, destinationID, 0, Message.MESSAGE_DEST_LEN);
        messageID = rawBytes[Message.MESSAGE_SRCS_LEN + Message.MESSAGE_DEST_LEN];
        messageType = rawBytes[Message.MESSAGE_SRCS_LEN + Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN];
        System.arraycopy(rawBytes, Message.MESSAGE_DATA_OFS, messageData, 0, Message.MESSAGE_DATA_LEN);

        Log.d(Constants.TAG, "getRaw" + Utils.bytesToHexString(new Message(sourceID, destinationID, messageID, messageType, messageData).getRawBytes()));

        return new Message(sourceID, destinationID, messageID, messageType, messageData);
    }

    public static Message createMessage(byte[] sourceID,
                                        byte[] destinationID,
                                        byte messageID,
                                        byte messageType,
                                        byte[] messageData) throws InvalidMessageSourceLengthException,
            InvalidMessageDestinationLengthException,
            InvalidMessageDataLengthException {

        if (messageData.length != Message.MESSAGE_DATA_LEN) throw new InvalidMessageDataLengthException();
        if (sourceID.length != Message.MESSAGE_SRCS_LEN) throw new InvalidMessageSourceLengthException();
        if (destinationID.length != Message.MESSAGE_DEST_LEN) throw new InvalidMessageDestinationLengthException();

        Log.d(Constants.TAG, "getRaw" + Utils.bytesToHexString(new Message(sourceID, destinationID, messageID, messageType, messageData).getRawBytes()));

        return new Message(sourceID, destinationID, messageID, messageType, messageData);
    }

    public static Message createMessage(byte[] sourceID,
                                        byte[] destinationID,
                                        byte messageType,
                                        byte[] messageData) throws InvalidMessageSourceLengthException,
            InvalidMessageDestinationLengthException,
            InvalidMessageDataLengthException {

        Message message;
        byte messageID = Utils.createMessageIDByte();
        message = createMessage(sourceID, destinationID, messageID, messageType, messageData);
        return message;
    }

    public static Message createMessage(byte[] sourceID,
                                        byte messageType,
                                        byte[] messageData) throws InvalidMessageSourceLengthException,
            InvalidMessageDestinationLengthException,
            InvalidMessageDataLengthException {

        Message message;
        message = createMessage(sourceID, Message.MESSAGE_ID_BROADCAST, messageType, messageData);
        return message;
    }

}
