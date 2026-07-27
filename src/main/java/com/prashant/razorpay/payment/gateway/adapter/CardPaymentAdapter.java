package com.prashant.razorpay.payment.gateway.adapter;

import com.prashant.razorpay.payment.gateway.PaymentAdapter;
import com.prashant.razorpay.payment.gateway.dto.PaymentRequest;
import com.prashant.razorpay.payment.gateway.dto.PaymentResult;
import com.prashant.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.prashant.razorpay.vault.sevice.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest request){

        String token =(String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultService.charge(
                request.paymentId(), token, request.amount(), request.methodDetails()
        );
        return switch (response) {
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
