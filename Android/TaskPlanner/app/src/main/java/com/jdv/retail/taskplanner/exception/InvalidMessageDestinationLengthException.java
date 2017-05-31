package com.jdv.retail.taskplanner.exception;

/**
 * Created by tfi on 31/03/2017.
 */

public class InvalidMessageDestinationLengthException extends Exception {
    //Parameterless Constructor
    public InvalidMessageDestinationLengthException() {}

    //Constructor that accepts a message
    public InvalidMessageDestinationLengthException(String message)
    {
        super(message);
    }
}
