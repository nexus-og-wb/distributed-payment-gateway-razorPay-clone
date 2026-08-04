package com.prashant.razorpay.merchant.cache;

import com.prashant.razorpay.common.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry(
        String keyId,
        String keySecretHash,
        String previousKeySecretKeyHash,
        LocalDateTime gracePeriodExpiredAt,
        UUID merchantId,
        Environment environment,
        boolean enabled
) {

    public boolean isInGracePeriod() {
        return gracePeriodExpiredAt != null && LocalDateTime.now().isBefore(gracePeriodExpiredAt);
    }

}
