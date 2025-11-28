package com.javaguy.nhx.security;

import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.UserRole;
import com.javaguy.nhx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UUID testUserId;
    private String testEmail;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(UserRole.INSTITUTIONAL_USER);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        UserPrincipal userPrincipal = customUserDetailsService.loadUserByUsername(testEmail);

        assertNotNull(userPrincipal);
        assertEquals(testUserId, userPrincipal.getId());
        assertEquals(testEmail, userPrincipal.getEmail());
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(testEmail));

        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void testLoadUserById_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserPrincipal userPrincipal = customUserDetailsService.loadUserById(testUserId);

        assertNotNull(userPrincipal);
        assertEquals(testUserId, userPrincipal.getId());
        assertEquals(testEmail, userPrincipal.getEmail());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testLoadUserById_UserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserById(testUserId));

        verify(userRepository).findById(testUserId);
    }

    @Test
    void testLoadUserByUsername_WithAdminRole() {
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        UserPrincipal userPrincipal = customUserDetailsService.loadUserByUsername(testEmail);

        assertNotNull(userPrincipal);
        assertTrue(userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserById_WithAdminRole() {
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserPrincipal userPrincipal = customUserDetailsService.loadUserById(testUserId);

        assertNotNull(userPrincipal);
        assertTrue(userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_EmptyEmail() {
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(""));
    }

    @Test
    void testLoadUserByUsername_NullEmail() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(null));
    }
}
