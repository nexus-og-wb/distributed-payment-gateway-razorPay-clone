package com.prashant.razorpay.vault.sevice;

import com.prashant.razorpay.common.entity.Money;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.prashant.razorpay.vault.dto.request.TokenizeRequest;
import com.prashant.razorpay.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);


    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);
}
