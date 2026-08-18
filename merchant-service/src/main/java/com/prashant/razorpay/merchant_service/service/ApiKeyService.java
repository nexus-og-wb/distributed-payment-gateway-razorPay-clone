package com.prashant.razorpay.merchant_service.service;

import com.prashant.razorpay.merchant_service.dto.request.CreateApiKeyRequest;
import com.prashant.razorpay.merchant_service.dto.response.ApiKeyCreateResponse;
import com.prashant.razorpay.merchant_service.dto.response.ApiKeyResponse;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {


    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    @Nullable ApiKeyCreateResponse rotateKey(UUID merchantId, UUID keyId);

}
