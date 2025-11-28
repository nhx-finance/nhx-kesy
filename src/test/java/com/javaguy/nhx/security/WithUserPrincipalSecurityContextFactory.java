package com.javaguy.nhx.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WithUserPrincipalSecurityContextFactory implements WithSecurityContextFactory<WithUserPrincipal> {
    @Override
    public SecurityContext createSecurityContext(WithUserPrincipal annotation) {
        String email = annotation.username().isEmpty() ? annotation.email() : annotation.username();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : annotation.roles()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                email,
                "password",
                authorities);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principal,
                "password",
                authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return context;
    }
}
