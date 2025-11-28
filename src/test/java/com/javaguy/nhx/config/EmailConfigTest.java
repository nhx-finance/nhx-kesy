package com.javaguy.nhx.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EmailConfigTest {

    @InjectMocks
    private EmailConfig emailConfig;

    private String host = "smtp.test.com";
    private int port = 587;
    private String username = "test@test.com";
    private String password = "testPassword";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailConfig, "host", host);
        ReflectionTestUtils.setField(emailConfig, "port", port);
        ReflectionTestUtils.setField(emailConfig, "username", username);
        ReflectionTestUtils.setField(emailConfig, "password", password);
    }

    @Test
    void javaMailSender_ConfiguresCorrectly() {
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) emailConfig.javaMailSender();

        assertNotNull(mailSender);
        assertEquals(host, mailSender.getHost());
        assertEquals(port, mailSender.getPort());
        assertEquals(username, mailSender.getUsername());
        assertEquals(password, mailSender.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        assertNotNull(props);
        assertEquals("smtp", props.getProperty("mail.transport.protocol"));
        assertEquals("true", props.getProperty("mail.smtp.auth"));
        assertEquals("true", props.getProperty("mail.smtp.starttls.enable"));
        assertEquals("true", props.getProperty("mail.smtp.starttls.required"));
        assertEquals("false", props.getProperty("mail.debug"));
    }
}
