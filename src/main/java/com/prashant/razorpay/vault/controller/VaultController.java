package com.prashant.razorpay.vault.controller;

import com.prashant.razorpay.vault.dto.request.TokenizeRequest;
import com.prashant.razorpay.vault.dto.response.TokenizeResponse;
import com.prashant.razorpay.vault.sevice.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    UUID merchantId = UUID.fromString("851bbb2d-90c5-4396-a4b0-ec94bfe25eb9"); // TODO: replace it with MerchantContext

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaultService.tokenize(request, merchantId));
    }
}
