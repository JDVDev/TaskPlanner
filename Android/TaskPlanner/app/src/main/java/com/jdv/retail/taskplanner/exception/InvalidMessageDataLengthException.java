package com.jdv.retail.taskplanner.exception;

/**
 * Created by tfi on 31/03/2017.
 */

public class InvalidMessageDataLengthException extends Exception {
    //Parameterless Constructor
    public InvalidMessageDataLengthException() {}

    //Constructor that accepts a message
    public InvalidMessageDataLengthException(String message)
    {
        super(message);
    }
}
