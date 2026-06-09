package com.oexil.studentreg.controller;

import com.oexil.studentreg.dto.user.UserDTO;
import com.oexil.studentreg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public List<UserDTO> searchUsers(@RequestParam("query") String query) {
        return userService.searchUsers(query);
    }
}