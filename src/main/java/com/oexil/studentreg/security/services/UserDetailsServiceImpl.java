package com.oexil.studentreg.security.services;

import com.oexil.studentreg.model.User;
import com.oexil.studentreg.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndActiveIsTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with user: " + username));
        return UserDetailsImpl.build(user);
    }
}
