package com.prashant.razorpay.merchant.dto.response;

import com.prashant.razorpay.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        com.prashant.razorpay.common.enums.BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
