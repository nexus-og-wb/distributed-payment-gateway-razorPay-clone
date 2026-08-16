package com.prashant.razorpay.operations.settlement;

import com.prashant.razorpay.common.entity.Money;
import com.prashant.razorpay.operations.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface BankTransferProcessor {
    BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc);
}
