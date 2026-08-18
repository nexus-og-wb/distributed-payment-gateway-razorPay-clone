package com.prashant.razorpay.operations_service.repository;

import com.prashant.razorpay.operations_service.entity.SettlementPayment;
import com.prashant.razorpay.operations_service.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
}
