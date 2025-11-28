package com.javaguy.nhx.service;

import com.javaguy.nhx.model.entity.NewsletterSubscriber;
import com.javaguy.nhx.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.javaguy.nhx.exception.custom.ConflictException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    public void subscribe(String email) {
        if (newsletterSubscriberRepository.findByEmail(email).isPresent()) {
            log.warn("Attempted to subscribe with existing email: {}", email);
            throw new ConflictException("Email is already subscribed to the newsletter.");
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        newsletterSubscriberRepository.save(subscriber);
        log.info("Email subscribed to newsletter: {}", email);
    }
}
