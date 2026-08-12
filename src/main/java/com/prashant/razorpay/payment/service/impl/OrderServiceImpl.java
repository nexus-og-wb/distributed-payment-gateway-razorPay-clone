package com.prashant.razorpay.payment.service.impl;

import com.prashant.razorpay.common.enums.EventAggregateType;
import com.prashant.razorpay.common.enums.OrderStatus;
import com.prashant.razorpay.common.exceptions.BusinessRuleViolationException;
import com.prashant.razorpay.common.exceptions.DuplicateResourceException;
import com.prashant.razorpay.common.exceptions.ResourceNotFoundException;
import com.prashant.razorpay.merchant.service.CustomerService;
import com.prashant.razorpay.payment.dto.request.CreateOrderRequest;
import com.prashant.razorpay.payment.dto.response.OrderResponse;
import com.prashant.razorpay.payment.dto.response.PaymentResponse;
import com.prashant.razorpay.payment.entity.OrderRecord;
import com.prashant.razorpay.payment.entity.Payment;
import com.prashant.razorpay.payment.mapper.OrderMapper;
import com.prashant.razorpay.payment.mapper.PaymentMapper;
import com.prashant.razorpay.payment.outbox.OutboxEventPublisher;
import com.prashant.razorpay.payment.repository.OrderRepository;
import com.prashant.razorpay.payment.repository.PaymentRepository;
import com.prashant.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final OrderMapper orderMapper;

    private final CustomerService customerService;

    private final OutboxEventPublisher eventPublisher;

    @Value("${razorpay.order.default-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already: " + request.receipt());
        }
        UUID customerId = null;

        if(request.customer() != null) {
            customerId = customerService.findOrCreate(merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone()
            );
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .merchantId(merchantId)
                .customerId(customerId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        eventPublisher.publish(
                EventAggregateType.ORDER,
                order.getId(),
                "ORDER_CREATED",
                Map.of("orderId", order.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency())
        );

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order" , orderId));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order" , orderId));
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID) {
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL", "Cannot cancel order with status: " + order.getOrderStatus().name());

        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        eventPublisher.publish(
                EventAggregateType.ORDER,
                order.getId(),
                "ORDER_CANCELLED",
                Map.of("orderId", order.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency())
        );
        return orderMapper.toResponse(order);

    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order" , orderId));

        List<Payment> paymentList = paymentRepository.findByOrder_Id(orderId);
//        return paymentList.stream()
//                .map(payment -> paymentMapper.toResponse(payment))
//                .collect(Collectors.toList());

        return paymentMapper.toResponseList(paymentList);
    }
}
