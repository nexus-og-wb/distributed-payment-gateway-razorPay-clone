package com.prashant.razorpay.payment.service.impl;

import com.prashant.razorpay.common.enums.OrderStatus;
import com.prashant.razorpay.common.enums.PaymentStatus;
import com.prashant.razorpay.common.exceptions.BusinessRuleViolationException;
import com.prashant.razorpay.common.exceptions.ResourceNotFoundException;
import com.prashant.razorpay.payment.dto.request.PaymentInitRequest;
import com.prashant.razorpay.payment.dto.response.PaymentResponse;
import com.prashant.razorpay.payment.entity.OrderRecord;
import com.prashant.razorpay.payment.entity.Payment;
import com.prashant.razorpay.payment.gateway.PaymentGatewayRouter;
import com.prashant.razorpay.payment.gateway.dto.PaymentRequest;
import com.prashant.razorpay.payment.gateway.dto.PaymentResult;
import com.prashant.razorpay.payment.mapper.PaymentMapper;
import com.prashant.razorpay.payment.repository.OrderRepository;
import com.prashant.razorpay.payment.repository.PaymentRepository;
import com.prashant.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {

        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order", request.orderId()));

        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "Order cannot accept payment in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);
        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(
                payment.getId(),
                request.orderId(),
                merchantId,
                order.getAmount(),
                request.method(),
                request.methodDetails()

        );

        PaymentResult result = paymentGatewayRouter.initiate(paymentRequest);

        switch(result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success -> {

            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }
}
