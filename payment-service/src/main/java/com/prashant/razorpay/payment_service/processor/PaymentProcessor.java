package com.prashant.razorpay.payment_service.processor;


import com.prashant.razorpay.common_lib.dto.PaymentProcessorRequest;
import com.prashant.razorpay.common_lib.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);

}
