package com.oexil.studentreg.controller;

import com.oexil.studentreg.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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

//    @GetMapping("/generate-all-ids-pdf")
//    public void generateAllIdsPdf(HttpServletResponse response,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  @RequestParam(defaultValue = "1000") int size) throws Exception {
//        studentService.generateAllIdsPdf(response, page, size);
//    }
}