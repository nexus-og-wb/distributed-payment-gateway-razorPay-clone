package com.prashant.razorpay.merchant.cache;

import com.prashant.razorpay.merchant.entity.ApiKey;

import java.util.Optional;

public interface ApiKeyCache {

    Optional<ApiKeyCacheEntry> get(String keyId);

    void put(String keyId, ApiKeyCacheEntry entry);

    void evict(String keyId);

}
