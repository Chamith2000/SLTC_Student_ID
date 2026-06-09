package com.oexil.studentreg.controller;

import com.oexil.studentreg.dto.user.UserAuth;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "auth")
public class AuthController {

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("userAuth", new UserAuth());
        return "auth/login";
    }
}
