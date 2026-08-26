package com.prashant.razorpay.common_lib.dto;

import java.util.UUID;

public record PaymentSettlementView(
        UUID paymentId,
        int amountUnits,
        int RefundAmountUnits,
        String currency
) {
}
