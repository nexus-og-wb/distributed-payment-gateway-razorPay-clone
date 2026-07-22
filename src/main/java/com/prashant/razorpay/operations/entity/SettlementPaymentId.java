package com.prashant.razorpay.operations.entity;

import com.prashant.razorpay.common.entity.BaseEntity;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class SettlementPaymentId extends BaseEntity {

    private UUID settlementId;

    private UUID paymentId;
}
