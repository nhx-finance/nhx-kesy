package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.model.dto.request.NewsletterSubscriptionRequest;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.service.NewsletterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsletterController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NewsletterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsletterService newsletterService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void subscribeToNewsletter_Success() throws Exception {
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest();
        request.setEmail("test@example.com");

        doNothing().when(newsletterService).subscribe(anyString());

        mockMvc.perform(post("/api/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void subscribeToNewsletter_InvalidEmail() throws Exception {
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribeToNewsletter_EmptyEmail() throws Exception {
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest();
        request.setEmail("");

        mockMvc.perform(post("/api/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribeToNewsletter_DuplicateEmail() throws Exception {
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest();
        request.setEmail("duplicate@example.com");

        doThrow(new ConflictException("Email is already subscribed to the newsletter."))
                .when(newsletterService).subscribe(anyString());

        mockMvc.perform(post("/api/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
