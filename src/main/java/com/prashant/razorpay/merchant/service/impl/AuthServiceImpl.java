package com.prashant.razorpay.merchant.service.impl;

import com.prashant.razorpay.common.enums.MerchantStatus;
import com.prashant.razorpay.common.enums.UserRole;
import com.prashant.razorpay.common.exceptions.DuplicateResourceException;
import com.prashant.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.prashant.razorpay.merchant.dto.response.MerchantResponse;
import com.prashant.razorpay.merchant.entity.AppUser;
import com.prashant.razorpay.merchant.entity.Merchant;
import com.prashant.razorpay.merchant.mapper.MerchantMapper;
import com.prashant.razorpay.merchant.repository.AppUserRepository;
import com.prashant.razorpay.merchant.repository.MerchantRepository;
import com.prashant.razorpay.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {

        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email already exists: "+request.email());
        }

        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(request.password())  //TODO: encrypt using Bcrypt
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);


        return merchantMapper.toResponse(merchant);
    }
}
