package com.prashant.razorpay.payment.dto.response;

import com.prashant.razorpay.common.entity.Money;
import com.prashant.razorpay.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderResponse(

        UUID id,

        UUID merchantId,

        UUID  customerId,

        String receipt,

        Money amount,

        OrderStatus status,

        Integer attempts,

        Map<String, Object> notes,

        LocalDateTime expiresAt,

        LocalDateTime createdAt

) {
}
