package com.prashant.razorpay.payment_service.processor;


import com.prashant.razorpay.payment_service.processor.dto.PaymentProcessorRequest;
import com.prashant.razorpay.payment_service.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);

}
