package com.lcjian.spunsugar.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.lcjian.spunsugar.MD5Utils;

@Component
public class BasicAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String username = authentication.getName();
        if (StringUtils.isEmpty(username)) {
            throw new BadCredentialsException("Empty username");
        }

        String password = (String) authentication.getCredentials();
        if (StringUtils.isEmpty(password)) {
            throw new BadCredentialsException("Empty password");
        }
        if (password.equals(MD5Utils.getMD532(username + "spun_sugar"))) {
            throw new BadCredentialsException("Error password");
        }
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}