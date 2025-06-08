package edu.finalproject.hotproperty.exceptions;

public class PropertyImageManagementException extends RuntimeException {
  public PropertyImageManagementException(String message) {
    super(message);
  }

  public PropertyImageManagementException(String message, Throwable cause) {
    super(message, cause);
  }
}
