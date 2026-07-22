package com.prashant.razorpay.merchant.service;

import com.prashant.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.prashant.razorpay.merchant.dto.response.MerchantResponse;

public interface AuthService {

    MerchantResponse signup(MerchantSignupRequest request);
}
