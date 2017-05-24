package com.jdv.retail.taskplanner.packet;



import com.jdv.retail.taskplanner.exception.InvalidMessageDataLengthException;
import com.jdv.retail.taskplanner.Utils;

/**
 * Created by tfi on 31/03/2017.
 */

public class MessageCreator {


    public static Message createMessage(byte messageID, byte deviceID, byte sendToDeviceID, byte messageType, byte[] messageData) throws InvalidMessageDataLengthException {
        byte[] message = new byte[Message.MESSAGE_LEN];
        message[0] = messageID;
        message[1] = deviceID;
        message[2] = sendToDeviceID;
        message[3] = messageType;

        if (messageData.length != Message.MESSAGE_DATA_LEN) throw new InvalidMessageDataLengthException();

        int offset = message.length - messageData.length;
        System.arraycopy(messageData, 0, message, offset, messageData.length);

        return new Message(message);
    }

    public static Message createMessage(byte deviceID, byte sendToDeviceID, byte messageType, byte[] messageData) throws InvalidMessageDataLengthException{
        byte[] message = new byte[Message.MESSAGE_LEN];
        message[0] = Utils.createMessageIDByte();
        message[1] = deviceID;
        message[2] = sendToDeviceID;
        message[3] = messageType;

        if (messageData.length != Message.MESSAGE_DATA_LEN) throw new InvalidMessageDataLengthException();

        int offset = message.length - messageData.length;
        System.arraycopy(messageData, 0, message, offset, messageData.length);

        return new Message(message);
    }

    public static Message createMessage(byte deviceID, byte messageType, byte[] messageData) throws InvalidMessageDataLengthException{
        byte[] message = new byte[Message.MESSAGE_LEN];
        message[0] = Utils.createMessageIDByte();
        message[1] = deviceID;
        message[2] = Message.MESSAGE_ID_BROADCAST;
        message[3] = messageType;

        if (messageData.length != Message.MESSAGE_DATA_LEN) throw new InvalidMessageDataLengthException();

        int offset = message.length - messageData.length;
        System.arraycopy(messageData, 0, message, offset, messageData.length);

        return new Message(message);
    }

    public static Message createMessage(byte deviceID, byte messageType) throws InvalidMessageDataLengthException{
        byte[] message = new byte[Message.MESSAGE_LEN];
        message[0] = Utils.createMessageIDByte();
        message[1] = deviceID;
        message[2] = Message.MESSAGE_ID_BROADCAST;
        message[3] = messageType;

        return new Message(message);
    }
}
