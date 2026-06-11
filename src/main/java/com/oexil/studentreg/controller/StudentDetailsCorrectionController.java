package com.oexil.studentreg.controller;

import com.oexil.studentreg.dto.student.StudentDTO;
import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/public/student")
@RequiredArgsConstructor
public class StudentDetailsCorrectionController {

    private final StudentService studentService;

    // Search page
    @GetMapping("/student-search-view")
    public String searchStudentPage(Model model) {
        model.addAttribute("searchQuery", "");
        model.addAttribute("studentDTO", new StudentDTO());

        return "student/search";
    }

    // Search processing
    @PostMapping("/student-search-process")
    public String searchStudentById(@RequestParam("query") String query,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (query == null || query.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please enter your Student ID");
            return "redirect:/public/student/student-search-view";
        }

        try {
            StudentDTO student = studentService.getStudentByNic(query);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "No student found with ID: " + query);
                return "redirect:/public/student/student-search-view";
            }
            if (isNotYetAvailable(student)) {
                redirectAttributes.addFlashAttribute("error", "Still Not Available");
                return "redirect:/public/student/student-search-view";
            }
            model.addAttribute("student", student);
            return "student/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error occurred while searching: " + e.getMessage());
            return "redirect:/public/student/student-search-view";
        }
    }

    // Student view page
    @GetMapping("/student-view/{id}")
    public String viewStudent(@PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            StudentDTO student = studentService.getStudentById(id);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "Student not found");
                return "redirect:/public/student/student-search-view";
            }
            if (isNotYetAvailable(student)) {
                redirectAttributes.addFlashAttribute("error", "Still Not Available");
                return "redirect:/public/student/student-search-view";
            }
            model.addAttribute("student", student);
            return "student/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error retrieving student details: " + e.getMessage());
            return "redirect:/public/student/student-search-view";
        }
    }

    /**
     * A student is not yet available for public search when they are still on the
     * "Recently Added" list — i.e. PENDING status and not yet submitted for confirmation.
     */
    private boolean isNotYetAvailable(StudentDTO student) {
        return student.getConfirmationStatus() == ConfirmationStatus.PENDING
                && student.getConfirmationStatusChangeTime() == null;
    }

    // Confirm details
    @PostMapping("/confirm")
    public String confirmDetails(@RequestParam("studentId") Long studentId,
                                 @RequestParam(value = "confirmed", required = false) Boolean confirmed,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (confirmed == null || !confirmed) {
            redirectAttributes.addFlashAttribute("error", "Please check the box to confirm your details");
            return "redirect:/public/student/student-view/" + studentId;
        }

        try {
            studentService.confirmStudentDetails(studentId); // Implement this in service
            model.addAttribute("success", "Details confirmed successfully"); // Add success message to model
            StudentDTO student = studentService.getStudentById(studentId); // Reload student data
            model.addAttribute("student", student);
            return "student/view"; // Stay on the same page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error confirming details: " + e.getMessage());
            return "redirect:/public/student/student-view/" + studentId;
        }
    }

    // Report errors
    @PostMapping("/report-error")
    public String reportError(@RequestParam("studentId") Long studentId,
                              @RequestParam("corrections") String corrections,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (corrections == null || corrections.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please specify the changes needed");
            return "redirect:/public/student/student-view/" + studentId;
        }

        try {
            studentService.reportStudentCorrections(studentId, corrections); // Implement this in service
            model.addAttribute("success", "Corrections submitted successfully"); // Add success message to model
            StudentDTO student = studentService.getStudentById(studentId); // Reload student data
            model.addAttribute("student", student);
            return "student/view"; // Stay on the same page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting corrections: " + e.getMessage());
            return "redirect:/public/student/student-view/" + studentId;
        }
    }
}