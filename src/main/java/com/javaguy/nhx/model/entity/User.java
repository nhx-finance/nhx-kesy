package com.javaguy.nhx.model.entity;

import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String country;
    private String province;
    private String timezone;

    private String mpesaNumber; // Added for M-Pesa payments
    
    @Builder.Default
    private Boolean termsAccepted = false;

    private String termsVersion;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus = KycStatus.INITIATED;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.INSTITUTIONAL_USER;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wallet> wallets = new ArrayList<>();
    
    // Removed Institution relationship.
}
