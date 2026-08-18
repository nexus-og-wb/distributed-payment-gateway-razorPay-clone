package com.prashant.razorpay.vault_service.sevice;



import com.prashant.razorpay.common_lib.entity.Money;
import com.prashant.razorpay.vault_service.dto.request.TokenizeRequest;
import com.prashant.razorpay.vault_service.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);


    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);
}
