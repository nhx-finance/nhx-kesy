package com.javaguy.nhx.repository;

import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUser(User user);
    Optional<Wallet> findByUserAndWalletAddress(User user, String walletAddress);
    Optional<Wallet> findByWalletAddress(String walletAddress);
}
