package com.prashant.razorpay.merchant.dto.request;

import com.prashant.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(

        Environment environment
) {
}
