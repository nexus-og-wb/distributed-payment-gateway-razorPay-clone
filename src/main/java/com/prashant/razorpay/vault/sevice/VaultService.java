package com.prashant.razorpay.vault.sevice;

import com.prashant.razorpay.vault.dto.request.TokenizeRequest;
import com.prashant.razorpay.vault.dto.response.TokenizeResponse;

import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);
}
