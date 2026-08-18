package com.prashant.razorpay.payment_service.gateway;



import com.prashant.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.prashant.razorpay.payment_service.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
