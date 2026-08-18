package com.prashant.razorpay.merchant.service;

import com.prashant.razorpay.merchant.dto.request.LoginRequest;
import com.prashant.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.prashant.razorpay.merchant.dto.response.LoginResponse;
import com.prashant.razorpay.merchant.dto.response.MerchantResponse;
import jakarta.validation.Valid;

public interface AuthService {

    MerchantResponse signup(MerchantSignupRequest request);

    LoginResponse login (LoginRequest request);
}
