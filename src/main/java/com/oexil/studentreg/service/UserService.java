package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.user.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    List<UserDTO> searchUsers(String query);
}
