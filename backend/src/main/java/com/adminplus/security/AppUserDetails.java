package com.adminplus.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.ArrayList;

@Getter
public class AppUserDetails implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final String nickname;
    private final String email;
    private final String phone;
    private final String avatar;
    private final Integer status;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(String id, String username, String password, String nickname,
                           String email, String phone, String avatar, Integer status,
                           List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.status = status;

        List<GrantedAuthority> authorityList = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            authorityList.addAll(roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        if (permissions != null && !permissions.isEmpty()) {
            authorityList.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        if (authorityList.isEmpty()) {
            authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        this.authorities = authorityList;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    public static AppUserDetails fromJwt(Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        String username = jwt.getSubject();

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object scopeClaim = jwt.getClaim("scope");
        if (scopeClaim instanceof String) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + scopeClaim));
        } else if (scopeClaim instanceof Collection) {
            ((Collection<?>) scopeClaim).forEach(scope ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + scope))
            );
        }

        return new AppUserDetails(
            userId,
            username,
            null,
            null,
            null,
            null,
            null,
            1,
            null,
            null
        );
    }
}
