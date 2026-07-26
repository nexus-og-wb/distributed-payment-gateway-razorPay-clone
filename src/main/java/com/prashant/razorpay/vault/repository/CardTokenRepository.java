package com.prashant.razorpay.vault.repository;

import com.prashant.razorpay.vault.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardTokenRepository extends JpaRepository<CardToken, UUID> {
}
