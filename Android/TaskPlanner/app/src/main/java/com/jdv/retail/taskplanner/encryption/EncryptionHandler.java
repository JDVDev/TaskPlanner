package com.jdv.retail.taskplanner.encryption;

import android.util.Log;

import com.jdv.retail.taskplanner.Constants;
import com.jdv.retail.taskplanner.Utils;
import com.jdv.retail.taskplanner.packet.Message;
import com.jdv.retail.taskplanner.packet.MessageCreator;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by tfi on 27/06/2017.
 */

public class EncryptionHandler {

    public static Message encrypt(Message msg, String password) {
        byte[] encrypted;
        try {
            byte[] seq = msg.getMessageSequence();
            byte[] src = msg.getSourceID();
            byte uid = msg.getMessageID();
            byte typ = msg.getMessageType();
            byte[] des = msg.getDestinationID();
            byte[] dat = msg.getMessageData();

            byte[] iv = new byte[16];
            iv[0] = seq[0];
            iv[1] = seq[1];
            iv[2] = seq[2];

            iv[4] = src[0];
            iv[5] = src[1];

            byte[] appended = new byte[Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN + Message.MESSAGE_DATA_LEN];
            System.arraycopy(des, 0, appended, 0, Message.MESSAGE_DEST_LEN);
            appended[Message.MESSAGE_DEST_LEN] = uid;
            appended[Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN] = typ;
            System.arraycopy(dat, 0, appended, Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN, Message.MESSAGE_DATA_LEN);

            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
            c.init(Cipher.ENCRYPT_MODE, key, ivspec);
            encrypted = c.doFinal(appended);

            byte[] newMsg = msg.getRawBytes();
            System.arraycopy(encrypted, 0, newMsg,
                    Message.MESSAGE_DATA_OFS - (Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN),
                    Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN + Message.MESSAGE_DATA_LEN);
            return MessageCreator.createMessage(newMsg);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static Message decrypt(Message msg, String password) {
        byte[] decrypted;
        try {
            byte[] seq = msg.getMessageSequence();
            byte[] src = msg.getSourceID();
            byte uid = msg.getMessageID();
            byte typ = msg.getMessageType();
            byte[] des = msg.getDestinationID();
            byte[] dat = msg.getMessageData();

            byte[] iv = new byte[16];
            iv[0] = seq[0];
            iv[1] = seq[1];
            iv[2] = seq[2];

            iv[4] = src[0];
            iv[5] = src[1];

            byte[] appended = new byte[Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN + Message.MESSAGE_DATA_LEN];
            System.arraycopy(des, 0, appended, 0, Message.MESSAGE_DEST_LEN);
            appended[Message.MESSAGE_DEST_LEN] = uid;
            appended[Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN] = typ;
            System.arraycopy(dat, 0, appended, Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN, Message.MESSAGE_DATA_LEN);

            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
            c.init(Cipher.DECRYPT_MODE, key, ivspec);
            decrypted = c.doFinal(appended);

            byte[] newMsg = msg.getRawBytes();
            System.arraycopy(decrypted, 0, newMsg,
                    Message.MESSAGE_DATA_OFS - (Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN),
                    Message.MESSAGE_DEST_LEN + Message.MESSAGE_UNID_LEN + Message.MESSAGE_TYPE_LEN + Message.MESSAGE_DATA_LEN);
            return MessageCreator.createMessage(newMsg);
        }
        catch (Exception e) {
            return null;
        }
    }
}
