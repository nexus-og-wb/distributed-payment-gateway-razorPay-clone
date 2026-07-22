package com.prashant.razorpay.common.exceptions;

public class BusinessRuleViolationException extends RuntimeException {
    private final String errorCode;
    public BusinessRuleViolationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
