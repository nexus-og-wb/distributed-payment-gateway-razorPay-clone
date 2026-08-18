package com.prashant.razorpay.operations.repository;

import com.prashant.razorpay.operations.entity.SettlementPayment;
import com.prashant.razorpay.operations.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
}
