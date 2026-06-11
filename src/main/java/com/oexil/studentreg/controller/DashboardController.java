package com.oexil.studentreg.controller;

import com.oexil.studentreg.enums.CardStatus;
import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.repository.StudentConfirmationRepository;
import com.oexil.studentreg.repository.StudentIdCardRepository;
import com.oexil.studentreg.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StudentRepository studentRepository;
    private final StudentConfirmationRepository studentConfirmationRepository;
    private final StudentIdCardRepository studentIdCardRepository;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {

        // --- Stat cards ---
        model.addAttribute("totalStudents", studentRepository.count());

        // Confirmation status breakdown
        Map<String, Long> confirmationCounts = new LinkedHashMap<>();
        confirmationCounts.put("PENDING", 0L);
        confirmationCounts.put("CONFIRMED", 0L);
        confirmationCounts.put("UNCONFIRMED", 0L);
        confirmationCounts.put("RECHECK", 0L);
        for (Object[] row : studentConfirmationRepository.countGroupedByStatus()) {
            confirmationCounts.put(((ConfirmationStatus) row[0]).name(), (Long) row[1]);
        }
        model.addAttribute("confirmationCounts", confirmationCounts);

        // Card status breakdown
        Map<String, Long> cardStatusCounts = new LinkedHashMap<>();
        cardStatusCounts.put("PRINT_PENDING", 0L);
        cardStatusCounts.put("PDF_GENERATED", 0L);
        cardStatusCounts.put("PRINTED", 0L);
        cardStatusCounts.put("ISSUED", 0L);
        for (Object[] row : studentIdCardRepository.countGroupedByStatus()) {
            cardStatusCounts.put(((CardStatus) row[0]).name(), (Long) row[1]);
        }
        model.addAttribute("cardStatusCounts", cardStatusCounts);

        // --- Chart data: Students by Course ---
        List<Object[]> courseRows = studentRepository.countByCourse();
        model.addAttribute("courseNames", courseRows.stream().map(r -> (String) r[0]).collect(Collectors.toList()));
        model.addAttribute("courseCounts", courseRows.stream().map(r -> (Long) r[1]).collect(Collectors.toList()));

        // --- Recent students table ---
        model.addAttribute("recentStudents", studentRepository.findTop10ByOrderByCreateDateDesc());

        return "dashboard/admin";
    }
}
