package com.jdv.retail.taskplanner.packet;

import android.bluetooth.le.AdvertiseData;

/**
 * Created by TFI on 24-3-2017.
 */

public class AdvertisingPacket{
    private AdvertiseData advertiseData;
    private int packetID;
    private int timeToLive;
    private int priority;

    public AdvertisingPacket(AdvertiseData advd, int pID, int ttl, int prio){
        advertiseData = advd;
        packetID = pID;
        timeToLive = ttl;
        priority = prio;
    }

    public AdvertiseData getAdvertiseData(){
        return advertiseData;
    }

    public int getPacketID(){
        return packetID;
    }

    public int getAndDecremtTimeToLive(){
        return timeToLive--;
    }

    public int getTimeToLive(){
        return timeToLive;
    }

    public int getPriority(){
        return priority;
    }
}
