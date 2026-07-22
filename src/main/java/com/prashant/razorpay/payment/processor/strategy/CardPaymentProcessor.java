package com.prashant.razorpay.payment.processor.strategy;

import com.prashant.razorpay.payment.processor.PaymentProcessor;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorResponse;

public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request){

        return null;
    }
}
