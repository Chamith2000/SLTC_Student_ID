package com.oexil.studentreg.controller;

import com.oexil.studentreg.dto.course.CourseDTO;
import com.oexil.studentreg.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/course-list")
    @PreAuthorize("hasRole('ADMIN')")
    public String courseHome(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        courseService.getAllCourses(model);
        return "course/list";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCourse(@Valid @ModelAttribute("course") CourseDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            return "course/list";
        }

        String result = courseService.createCourse(dto);

        if ("DUPLICATE".equals(result))
            redirectAttributes.addFlashAttribute("error", "Course with this name already exists");

        redirectAttributes.addFlashAttribute("success", "Course created successfully");
        return "redirect:/course/course-list";
    }
}