package com.oexil.studentreg.config;

import com.oexil.studentreg.dto.user.UserForResponsePrivet;
import com.oexil.studentreg.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CurrentUser currentUser;
    private final ModelMapper modelMapper;

    @ModelAttribute("loggedInUser")
    public UserForResponsePrivet addLoggedInUserToModel() {
        if (currentUser.getUserForGlobalControllerAdvice() == null)
            return null;

        return modelMapper.map(currentUser.getUserForGlobalControllerAdvice(), UserForResponsePrivet.class);
    }

}
