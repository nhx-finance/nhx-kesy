package com.javaguy.nhx.model.entity;

import com.javaguy.nhx.model.enums.MintStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Mint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountKes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MintStatus status;

    @Column(nullable = false)
    private LocalDate dateInitiated;

    private LocalDate restrictionEndDate;

    private String paymentReference;

    private String treasuryTransactionId;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean settled = false;
}
