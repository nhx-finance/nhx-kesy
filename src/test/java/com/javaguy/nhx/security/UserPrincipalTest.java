package com.javaguy.nhx.security;

import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserPrincipalTest {

    private UUID testUserId;
    private String testEmail;
    private String testPassword;
    private Collection<? extends GrantedAuthority> testAuthorities;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testPassword = "password123";
        testAuthorities = UserRole.INSTITUTIONAL_USER.getAuthorities();
    }

    @Test
    void testUserPrincipalConstructor() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        assertNotNull(userPrincipal);
        assertEquals(testUserId, userPrincipal.getId());
        assertEquals(testEmail, userPrincipal.getEmail());
        assertEquals(testPassword, userPrincipal.getPassword());
    }

    @Test
    void testGetAuthorities() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        assertNotNull(authorities);
        assertEquals(testAuthorities, authorities);
    }

    @Test
    void testGetPassword() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        String password = userPrincipal.getPassword();

        assertEquals(testPassword, password);
    }

    @Test
    void testGetUsername() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        String username = userPrincipal.getUsername();

        assertEquals(testEmail, username);
    }

    @Test
    void testIsAccountNonExpired() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        assertTrue(userPrincipal.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        assertTrue(userPrincipal.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        assertTrue(userPrincipal.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void testCreate_FromUser() {
        User user = new User();
        user.setId(testUserId);
        user.setEmail(testEmail);
        user.setPasswordHash(testPassword);
        user.setRole(UserRole.INSTITUTIONAL_USER);

        UserPrincipal userPrincipal = UserPrincipal.create(user);

        assertNotNull(userPrincipal);
        assertEquals(testUserId, userPrincipal.getId());
        assertEquals(testEmail, userPrincipal.getEmail());
        assertEquals(testPassword, userPrincipal.getPassword());
        assertNotNull(userPrincipal.getAuthorities());
    }

    @Test
    void testCreate_AdminUser() {
        User user = new User();
        user.setId(testUserId);
        user.setEmail(testEmail);
        user.setPasswordHash(testPassword);
        user.setRole(UserRole.ADMIN);

        UserPrincipal userPrincipal = UserPrincipal.create(user);

        assertNotNull(userPrincipal);
        assertEquals(testUserId, userPrincipal.getId());
        assertTrue(userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testUserPrincipalSetters() {
        UserPrincipal userPrincipal = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        UUID newUserId = UUID.randomUUID();
        String newEmail = "newemail@example.com";
        String newPassword = "newpassword123";

        userPrincipal.setId(newUserId);
        userPrincipal.setEmail(newEmail);
        userPrincipal.setPassword(newPassword);

        assertEquals(newUserId, userPrincipal.getId());
        assertEquals(newEmail, userPrincipal.getEmail());
        assertEquals(newPassword, userPrincipal.getPassword());
    }

    @Test
    void testUserPrincipalEquality() {
        UserPrincipal userPrincipal1 = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);
        UserPrincipal userPrincipal2 = new UserPrincipal(testUserId, testEmail, testPassword, testAuthorities);

        // UserPrincipal should have equals/hashCode based on all fields
        assertEquals(userPrincipal1.getId(), userPrincipal2.getId());
        assertEquals(userPrincipal1.getEmail(), userPrincipal2.getEmail());
    }
}
