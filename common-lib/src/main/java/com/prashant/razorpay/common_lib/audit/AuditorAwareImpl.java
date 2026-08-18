package com.prashant.razorpay.common_lib.audit;

import com.prashant.razorpay.common_lib.context.MerchantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final MerchantContext merchantContext;

    @Override
    public Optional<String> getCurrentAuditor() {

        try{
            String keyId = merchantContext.getKeyId();

            if(keyId != null && !keyId.isBlank()) {
                return Optional.of(keyId);
            }

            if(merchantContext.getMerchantId() != null) {
                return Optional.of("merchantId: "+merchantContext.getMerchantId());
            }
        } catch (Exception ignore){

        }

        return Optional.of("SYSTEM");
    }
}
