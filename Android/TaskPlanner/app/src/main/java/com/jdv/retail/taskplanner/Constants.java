package com.jdv.retail.taskplanner;

import android.os.ParcelUuid;

/**
 * Created by TFI on 20-3-2017.
 */

public class Constants {
    public static final String TAG = "TaskPlanner";
    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("0000FEF1-0000-1000-8000-00805F9B34FB");
    public static final int USED_MESSAGE_ID_BUFFER_SIZE = 1;
    public static final int MANUFACTURE_ID = 65535;
    public static final byte BASESTATION_ID = (byte) 0xFB;
    public static final String SHARED_PREFERENCE_KEY  = "com.jdv.retail.taskplanner.PREFERENCE_FILE_KEY";
    public static final String SAVED_DEVICE_ID_KEY = "com.jdv.retail.taskplanner.device_id";
    public static final String SAVED_IS_NOTIFY_KEY = "com.jdv.retail.taskplanner.is_notify";
    public static final String NOTIFICATION_DATA_KEY = "com.jdv.retail.taskplanner.notification_data";
    public static final boolean isPollOrListen = false;
    public static final boolean isDebug = true;
}
