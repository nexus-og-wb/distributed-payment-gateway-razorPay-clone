package com.prashant.razorpay.payment_service.dto.request;

import com.prashant.razorpay.common_lib.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(

        @NotNull(message = "order Id is required")
        UUID orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
