package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.user.UserAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    ResponseEntity<?> authenticateUser(UserAuth login);
}
