package com.jdv.retail.taskplanner.exception;

/**
 * Created by tfi on 31/03/2017.
 */

public class InvalidLengthException extends Exception {
    //Parameterless Constructor
    public InvalidLengthException() {}

    //Constructor that accepts a message
    public InvalidLengthException(String message)
    {
        super(message);
    }
}
