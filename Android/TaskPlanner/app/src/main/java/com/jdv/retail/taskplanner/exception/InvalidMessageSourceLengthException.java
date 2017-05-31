package com.jdv.retail.taskplanner.exception;

/**
 * Created by tfi on 31/03/2017.
 */

public class InvalidMessageSourceLengthException extends Exception {
    //Parameterless Constructor
    public InvalidMessageSourceLengthException() {}

    //Constructor that accepts a message
    public InvalidMessageSourceLengthException(String message)
    {
        super(message);
    }
}
