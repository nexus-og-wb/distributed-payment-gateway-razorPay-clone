package com.prashant.razorpay.payment.config;

import com.prashant.razorpay.common.enums.PaymentMethod;
import com.prashant.razorpay.payment.processor.PaymentProcessor;
import com.prashant.razorpay.payment.processor.strategy.CardPaymentProcessor;
import com.prashant.razorpay.payment.processor.strategy.NetBankingPaymentProcessor;
import com.prashant.razorpay.payment.processor.strategy.UpiPaymentProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.NETBANKING, new NetBankingPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor()
        );
    }
}
