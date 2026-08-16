package com.prashant.razorpay.merchant.repository;

import com.prashant.razorpay.common.enums.MerchantStatus;
import com.prashant.razorpay.merchant.entity.Merchant;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEmail(String email);

    List<UUID> findActiveMerchantIds();

    List<Merchant> findByStatus(MerchantStatus merchantStatus);

}
