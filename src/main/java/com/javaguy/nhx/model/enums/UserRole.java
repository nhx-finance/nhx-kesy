package com.javaguy.nhx.model.enums;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum UserRole {
    INSTITUTIONAL_USER(Set.of("ROLE_INSTITUTIONAL_USER")),
    ADMIN(Set.of("ROLE_ADMIN"));

    private final Set<String> permissions;

    UserRole(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return getPermissions().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
