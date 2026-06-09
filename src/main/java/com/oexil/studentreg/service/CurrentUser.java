package com.oexil.studentreg.service;

import com.oexil.studentreg.model.User;
import org.springframework.stereotype.Service;

@Service
public interface CurrentUser {
    User getUser();

    User getUserForGlobalControllerAdvice();
}
