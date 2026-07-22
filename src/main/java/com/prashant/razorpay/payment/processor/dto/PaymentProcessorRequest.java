package com.prashant.razorpay.payment.processor.dto;

import com.prashant.razorpay.common.entity.Money;
import com.prashant.razorpay.common.enums.PaymentMethod;

import java.util.Map;

public record PaymentProcessorRequest(
        PaymentMethod method,
        Money amount,
        Map<String, Object> methodDetails
) {


}