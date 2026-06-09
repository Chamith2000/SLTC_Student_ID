package com.oexil.studentreg.service.impl;

import com.oexil.studentreg.dto.course.CourseDTO;
import com.oexil.studentreg.model.course.Course;
import com.oexil.studentreg.repository.CourseRepository;
import com.oexil.studentreg.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CourseDTO> getAllCourses(Model model) {
        List<Course> all = courseRepository.findAll();
        List<CourseDTO> dtos = modelMapper.map(all, new TypeToken<List<CourseDTO>>() {}.getType());
        model.addAttribute("courses", dtos);
        return dtos;
    }

    @Override
    public String createCourse(CourseDTO dto) {
        Course course = modelMapper.map(dto, Course.class);
        courseRepository.save(course);
        return "SUCCESS";
    }
}
