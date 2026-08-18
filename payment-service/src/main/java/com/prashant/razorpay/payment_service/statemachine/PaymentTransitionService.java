package com.prashant.razorpay.payment_service.statemachine;


import com.prashant.razorpay.common_lib.enums.PaymentActor;
import com.prashant.razorpay.common_lib.enums.PaymentEvent;
import com.prashant.razorpay.common_lib.enums.PaymentStatus;
import com.prashant.razorpay.payment_service.entity.Payment;
import com.prashant.razorpay.payment_service.entity.PaymentTransitionLog;
import com.prashant.razorpay.payment_service.repository.PaymentTransitionLogRepository;
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



        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .event(event)
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO: fetch merchant context to identify the actor
                .occurredAt(LocalDateTime.now())
                .build();
        payment.setStatus(next);

        paymentTransitionLogRepository.save(log);

        return next;

    }

}
