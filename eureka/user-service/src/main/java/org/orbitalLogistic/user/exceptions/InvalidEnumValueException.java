package org.orbitalLogistic.user.exceptions;

import java.util.Arrays;

public class InvalidEnumValueException extends RuntimeException{
    private final String fieldName;
    private final String invalidValue;
    private final String[] acceptedValues;

    public InvalidEnumValueException(String fieldName, String invalidValue, String[] acceptedValues) {
        super(String.format("Invalid value '%s' for field '%s'. Accepted values: %s", 
              invalidValue, fieldName, Arrays.toString(acceptedValues)));
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.acceptedValues = acceptedValues;
    }

    public String getFieldName() { return fieldName; }
    public String getInvalidValue() { return invalidValue; }
    public String[] getAcceptedValues() { return acceptedValues; }
}
