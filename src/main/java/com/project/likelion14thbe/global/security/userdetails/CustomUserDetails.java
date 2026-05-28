package com.project.likelion14thbe.global.security.userdetails;

import com.project.likelion14thbe.domain.member.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final Role role;

    public CustomUserDetails(Long memberId, String email, String password, Role role) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // memberId 없이 생성하는 경우
    public CustomUserDetails(String email, String password, Role role) {
        this(null, email, password, role);
    }

    // 해당 User 의 권한을 return
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
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
        return true;
    }
}
