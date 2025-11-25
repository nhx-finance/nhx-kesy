package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.NewsletterSubscriptionRequest;
import com.javaguy.nhx.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribeToNewsletter(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        newsletterService.subscribe(request.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
