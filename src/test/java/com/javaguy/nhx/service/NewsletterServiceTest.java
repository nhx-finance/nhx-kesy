package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.model.entity.NewsletterSubscriber;
import com.javaguy.nhx.repository.NewsletterSubscriberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private NewsletterSubscriberRepository newsletterSubscriberRepository;

    @InjectMocks
    private NewsletterService newsletterService;

    @Test
    void subscribe_Success() {
        String email = "new@example.com";
        when(newsletterSubscriberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(newsletterSubscriberRepository.save(any(NewsletterSubscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> newsletterService.subscribe(email));

        verify(newsletterSubscriberRepository, times(1)).findByEmail(email);
        verify(newsletterSubscriberRepository, times(1)).save(any(NewsletterSubscriber.class));
    }

    @Test
    void subscribe_DuplicateEmail_ThrowsConflictException() {
        String email = "existing@example.com";
        when(newsletterSubscriberRepository.findByEmail(email)).thenReturn(Optional.of(new NewsletterSubscriber()));

        ConflictException thrown = assertThrows(ConflictException.class, () -> newsletterService.subscribe(email));
        assertEquals("Email is already subscribed to the newsletter.", thrown.getMessage());

        verify(newsletterSubscriberRepository, times(1)).findByEmail(email);
        verify(newsletterSubscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }
}
