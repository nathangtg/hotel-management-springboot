package com.nathangtg.hotel_management.security;

import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        
        // Create authorities based on user role
        Collection<? extends GrantedAuthority> authorities;
        if (user.getRole() != null) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        } else {
            authorities = Collections.emptyList();
        }
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }
}