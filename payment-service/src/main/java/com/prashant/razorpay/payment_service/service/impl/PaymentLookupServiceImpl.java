package com.prashant.razorpay.payment_service.service.impl;

import com.prashant.razorpay.common_lib.enums.PaymentStatus;
import com.prashant.razorpay.payment_service.api.PaymentLookupService;
import com.prashant.razorpay.payment_service.entity.Payment;
import com.prashant.razorpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> findUnsettledCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);
    }
}
