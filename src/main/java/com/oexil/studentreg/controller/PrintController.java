package com.oexil.studentreg.controller;

import com.oexil.studentreg.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping(value = "print")
@RequiredArgsConstructor
public class PrintController {

    private final StudentService studentService;

    @GetMapping("/printed-list")
    public String confirmedList(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        studentService.getAllPrintedStudents(model, page, size);
        return "student/print/printed-list";
    }

    @GetMapping("/pending-print-list")
    public String pendingList(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "100") int size) {
        studentService.getAllPendingToPrint(model, page, size);
        return "student/print/pending-list";
    }

    @PostMapping("/mark-as-printed")
    public String markAsPrinted(@RequestParam List<Long> studentIds,
                                @RequestParam String printLabel,
                                RedirectAttributes redirectAttributes) {
        studentService.markAsPrinted(studentIds, printLabel);
        redirectAttributes.addFlashAttribute("success",
                studentIds.size() + " student card(s) marked as Printed under batch \"" + printLabel + "\".");
        return "redirect:/print/pending-print-list";
    }

//    @GetMapping("/generate-all-ids-pdf")
//    public void generateAllIdsPdf(HttpServletResponse response,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  @RequestParam(defaultValue = "1000") int size) throws Exception {
//        studentService.generateAllIdsPdf(response, page, size);
//    }
}