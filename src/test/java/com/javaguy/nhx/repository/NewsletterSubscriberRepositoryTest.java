package com.javaguy.nhx.repository;

import com.javaguy.nhx.model.entity.NewsletterSubscriber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NewsletterSubscriberRepositoryTest {

    @Autowired
    private NewsletterSubscriberRepository newsletterSubscriberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_ExistingEmail_ReturnsSubscriber() {
        // Given
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail("test@example.com");
        entityManager.persistAndFlush(subscriber);

        // When
        Optional<NewsletterSubscriber> foundSubscriber = newsletterSubscriberRepository.findByEmail("test@example.com");

        // Then
        assertTrue(foundSubscriber.isPresent());
        assertEquals("test@example.com", foundSubscriber.get().getEmail());
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<NewsletterSubscriber> foundSubscriber = newsletterSubscriberRepository
                .findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundSubscriber.isPresent());
    }

    @Test
    void save_NewSubscriber_PersistsSuccessfully() {
        // Given
        NewsletterSubscriber newSubscriber = new NewsletterSubscriber();
        newSubscriber.setEmail("new@example.com");

        // When
        NewsletterSubscriber savedSubscriber = newsletterSubscriberRepository.save(newSubscriber);
        entityManager.flush(); // Ensure it's persisted to the DB
        entityManager.clear(); // Clear cache to fetch fresh from DB

        // Then
        assertNotNull(savedSubscriber.getId());
        assertEquals("new@example.com", savedSubscriber.getEmail());
        assertNotNull(savedSubscriber.getSubscribedAt());

        Optional<NewsletterSubscriber> found = newsletterSubscriberRepository.findByEmail("new@example.com");
        assertTrue(found.isPresent());
        assertEquals(savedSubscriber.getId(), found.get().getId());
    }
}
