package com.javaguy.nhx.repository;

import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.enums.MintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MintRepository extends JpaRepository<Mint, UUID> {
    Optional<Mint> findByPaymentReference(String paymentReference);

    // For AdminService
    Page<Mint> findByStatus(MintStatus status, Pageable pageable);
    Page<Mint> findByUserId(UUID userId, Pageable pageable);
    Optional<Mint> findByUserIdAndId(UUID userId, UUID id);
}
