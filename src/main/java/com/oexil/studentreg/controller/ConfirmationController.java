package com.oexil.studentreg.controller;

import com.oexil.studentreg.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping(value = "confirmation")
@RequiredArgsConstructor
public class ConfirmationController {

    private final StudentService studentService;

    @GetMapping("/confirmed-list")
    public String confirmedList(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        studentService.getAllConfirmedStudents(model, page, size);
        return "student/confirmation/confirmed-list";
    }

    @GetMapping("/pending-list")
    public String pendingList(Model model) {
        studentService.getAllPendingStudents(model);
        return "student/confirmation/pending-list";
    }

    @GetMapping("/correction-list")
    public String correctionList(Model model) {
        studentService.getAllCorrectionStudents(model);
        return "student/confirmation/correction-list";
    }

//    @GetMapping("/print-pending-ids")
//    public String printPendingIds(Model model,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  @RequestParam(defaultValue = "1000") int size) {
//        studentService.getAllPendingToPrintUnconfirmed(model, page, size);
//        return "student/id/id-card";
//    }
}