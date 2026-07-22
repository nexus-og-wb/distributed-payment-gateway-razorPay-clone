package com.prashant.razorpay.payment.gateway;

import com.prashant.razorpay.payment.gateway.dto.PaymentRequest;
import com.prashant.razorpay.payment.gateway.dto.PaymentResult;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);
}
