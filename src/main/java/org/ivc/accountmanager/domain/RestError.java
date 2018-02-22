package org.ivc.accountmanager.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error data transfer class.
 *
 * @author Roman Osipov
 */
public class RestError {

    //----------------------Constants------------------------------------------
    public static final String CLASS_NAME_PROPERTY_NAME = "exceptionClassName";
    public static final String MESSAGE_PROPERTY_NAME = "message";

    //----------------------Fields---------------------------------------------
    @JsonProperty(CLASS_NAME_PROPERTY_NAME)
    private final String exceptionClassName;

    @JsonProperty(MESSAGE_PROPERTY_NAME)
    private final String message;

    //----------------------Constructors---------------------------------------
    /**
     * Constructs error object.
     *
     * @param exceptionClassName the exception class name.
     * @param message the error message.
     */
    public RestError(String exceptionClassName, String message) {
        this.exceptionClassName = exceptionClassName;
        this.message = message;
    }

    //----------------------Getters&Setters------------------------------------
    /**
     * Returns the exception class name.
     *
     * @return the error code.
     */
    public String getExceptionClassName() {
        return exceptionClassName;
    }

    /**
     * Returns the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }
}
