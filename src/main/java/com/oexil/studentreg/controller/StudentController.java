package com.oexil.studentreg.controller;

import com.oexil.studentreg.dto.student.StudentDTO;
import com.oexil.studentreg.service.BatchService;
import com.oexil.studentreg.service.CourseService;
import com.oexil.studentreg.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final BatchService batchService;

    @GetMapping("/student-list")
    public String studentHome(Model model) {
        studentService.getAllStudent(model);
        return "student/list";
    }

    @GetMapping("/student-add")
    public String createStudent(Model model) {
        model.addAttribute("studentDTO", new StudentDTO());
        courseService.getAllCourses(model);
        batchService.getAllBatches(model);
        return "student/create";
    }

    @PostMapping("/student-register")
    public String registerStudent(@Valid @ModelAttribute("studentDTO") StudentDTO studentDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courseService.getAllCourses(model));
            model.addAttribute("batches", batchService.getAllBatches(model));
            model.addAttribute("error", "Validation errors occurred");
            return "student/create";
        }

        String result = studentService.registerStudent(studentDTO);

        if ("DUPLICATE".equals(result)) {
            model.addAttribute("error", "Student with this phone number or nic or reg no already exists");
            courseService.getAllCourses(model);
            batchService.getAllBatches(model);
            return "student/create";
        } else {
            redirectAttributes.addFlashAttribute("success", "Student registered successfully");
            return "redirect:/student/student-list";
        }
    }

    // Edit Student Page
    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model) {
        StudentDTO studentDTO = studentService.getStudentById(id);
        model.addAttribute("studentDTO", studentDTO);
        model.addAttribute("courses", courseService.getAllCourses(model));
        model.addAttribute("batches", batchService.getAllBatches(model));
        return "student/edit";
    }

    // Update Student
    @PostMapping("/update")
    public String updateStudent(@Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            model.addAttribute("error", "Error: Student update failed with " + errors);
            model.addAttribute("courses", courseService.getAllCourses(model));
            model.addAttribute("batches", batchService.getAllBatches(model));
            return "student/edit";
        }

        String result = studentService.updateStudent(studentDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Student updated successfully");
        }else if ("CONFIRMED".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Student updated successfully");
            return "redirect:/confirmation/pending-list";
        } else {
            redirectAttributes.addFlashAttribute("error", "Error : Student update failed with " + result);
        }
        return "redirect:/student/student-list";
    }

    // Delete Student
    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String result = studentService.deleteStudent(id);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Something went wrong");
        }
        return "redirect:/student/student-list";
    }

    @PostMapping("/upload-student-excel")
    public String uploadStudentExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select an Excel file to upload.");
            return "redirect:/student/student-list";
        }

        String result = studentService.registerStudentsBatch(file);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Students registered successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to process the file: " + result);
        }

        return "redirect:/student/student-list";
    }

    @GetMapping("/generate-id/{id}")
    public String generateIdCard(@PathVariable Long id, Model model) {
        Page<StudentDTO> studentDTOPage = new PageImpl<>(List.of(studentService.getStudentById(id)), PageRequest.of(0, 1), 1);
        model.addAttribute("students", studentDTOPage);

        return "student/id/id-card";
    }

    @GetMapping("/generate-all-ids")
    public String generateAllIdCards(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
//        studentService.getAllConfirmedStudents(model, page, size);
        studentService.getAllPendingToPrint(model, page, size);
        return "student/id/id-card";
    }

}