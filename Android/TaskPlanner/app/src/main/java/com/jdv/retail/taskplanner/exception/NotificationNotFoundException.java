package com.jdv.retail.taskplanner.exception;

/**
 * Created by tfi on 31/03/2017.
 */

public class NotificationNotFoundException extends Exception {
    //Parameterless Constructor
    public NotificationNotFoundException() {}

    //Constructor that accepts a message
    public NotificationNotFoundException(String message)
    {
        super(message);
    }
}
