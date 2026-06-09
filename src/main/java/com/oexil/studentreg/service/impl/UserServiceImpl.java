package com.oexil.studentreg.service.impl;

import com.oexil.studentreg.dto.user.UserDTO;
import com.oexil.studentreg.model.User;
import com.oexil.studentreg.repository.UserRepository;
import com.oexil.studentreg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDTO> searchUsers(String query) {
        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query);
        return users.stream().map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getFirstName() + " " + user.getLastName());
            userDTO.setEmail(user.getEmail());
            return userDTO;
        }).collect(Collectors.toList());
    }
}
