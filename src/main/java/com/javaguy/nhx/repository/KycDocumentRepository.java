package com.javaguy.nhx.repository;

import com.javaguy.nhx.model.entity.KycDocument;
import com.javaguy.nhx.model.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {
    List<KycDocument> findByUserId(UUID userId);
    Page<KycDocument> findByUser_KycStatus(KycStatus status, Pageable pageable);
}
