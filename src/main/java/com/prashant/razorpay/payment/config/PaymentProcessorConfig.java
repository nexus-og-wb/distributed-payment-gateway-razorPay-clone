package com.prashant.razorpay.payment.config;

import com.prashant.razorpay.common.enums.PaymentMethod;
import com.prashant.razorpay.payment.processor.PaymentProcessor;
import com.prashant.razorpay.payment.processor.strategy.CardPaymentProcessor;
import com.prashant.razorpay.payment.processor.strategy.NetBankingPaymentProcessor;
import com.prashant.razorpay.payment.processor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NETBANKING, netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
