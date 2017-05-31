package com.jdv.retail.taskplanner.exception;

/**
 * Created by tfi on 31/03/2017.
 */

public class InvalidMessageLengthException extends Exception {
    //Parameterless Constructor
    public InvalidMessageLengthException() {}

    //Constructor that accepts a message
    public InvalidMessageLengthException(String message)
    {
        super(message);
    }
}
