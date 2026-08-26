package com.prashant.razorpay.common_lib.dto;

public sealed interface PaymentProcessorResponse permits
        PaymentProcessorResponse.Pending,
        PaymentProcessorResponse.Success,
        PaymentProcessorResponse.Failure {

    record Pending(String processorReference) implements PaymentProcessorResponse{}

    record Success(String processorRef, String bankReference) implements PaymentProcessorResponse{}

    record Failure(String errorCode, String errorDescription) implements PaymentProcessorResponse{}
}
