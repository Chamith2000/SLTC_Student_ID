package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.course.CourseDTO;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.validation.Valid;
import java.util.List;


@Service
public interface CourseService {

    List<CourseDTO> getAllCourses(Model model);

    String createCourse(@Valid CourseDTO dto);
}
