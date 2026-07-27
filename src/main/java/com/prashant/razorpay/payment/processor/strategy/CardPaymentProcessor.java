package com.prashant.razorpay.payment.processor.strategy;

import com.prashant.razorpay.common.util.RandomizerUtil;
import com.prashant.razorpay.payment.processor.PaymentProcessor;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINED = "400000000000002";
    public static final String PAN_CARD_EXPIRED = "400000000000069";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request){

        String pan = request.pan();

        if(PAN_CARD_DECLINED.equals(pan)){
            log.warn("Card Declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined by the bank");
        }

        if(PAN_CARD_EXPIRED.equals(pan)){
            log.warn("Pan card has expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired");
        }

        String processorRef = "CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);

    }
}
