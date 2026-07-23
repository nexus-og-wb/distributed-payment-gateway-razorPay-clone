
package com.prashant.razorpay.payment.config;

import com.prashant.razorpay.common.enums.PaymentMethod;
import com.prashant.razorpay.payment.gateway.PaymentAdapter;
import com.prashant.razorpay.payment.gateway.adapter.CardPaymentAdapter;
import com.prashant.razorpay.payment.gateway.adapter.NetBankingAdapter;
import com.prashant.razorpay.payment.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
@RequiredArgsConstructor
@Configuration
public class PaymentAdapterConfig {

    private final NetBankingAdapter netBankingAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;


    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.NETBANKING, netBankingAdapter,
                PaymentMethod.UPI, upiPaymentAdapter
        );

    }


}