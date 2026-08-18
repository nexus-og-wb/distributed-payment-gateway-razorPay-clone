package com.prashant.razorpay.common.exceptions;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String errorCode;
    public DuplicateResourceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
