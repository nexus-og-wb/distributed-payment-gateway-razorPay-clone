package com.prashant.razorpay.payment.processor.strategy;

import com.prashant.razorpay.payment.processor.PaymentProcessor;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        // Call the third-party API
        return null;
    }
}
