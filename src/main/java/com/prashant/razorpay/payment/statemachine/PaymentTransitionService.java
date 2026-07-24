package com.prashant.razorpay.payment.statemachine;

import com.prashant.razorpay.common.enums.PaymentActor;
import com.prashant.razorpay.common.enums.PaymentEvent;
import com.prashant.razorpay.common.enums.PaymentStatus;
import com.prashant.razorpay.payment.entity.Payment;
import com.prashant.razorpay.payment.entity.PaymentTransitionLog;
import com.prashant.razorpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent event){

        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), event);

        payment.setStatus(next);

        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .event(event)
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO: fetch merchant context to identify the actor
                .occurredAt(LocalDateTime.now())
                .build();

        paymentTransitionLogRepository.save(log);

        return next;

    }

}
