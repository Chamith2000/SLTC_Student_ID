package com.oexil.studentreg.controller;

import com.oexil.studentreg.dto.course.BatchDTO;
import com.oexil.studentreg.dto.course.CourseDTO;
import com.oexil.studentreg.service.BatchService;
import com.oexil.studentreg.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping("/batch-list")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchHome(Model model) {
        model.addAttribute("batchDTO", new BatchDTO());
        batchService.getAllBatches(model);
        return "batch/list";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createBatch(@Valid @ModelAttribute("batch") BatchDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            return "batch/list";
        }

        String result = batchService.createBatch(dto);

        if ("DUPLICATE".equals(result))
            redirectAttributes.addFlashAttribute("error", "Batch with this name already exists");

        redirectAttributes.addFlashAttribute("success", "Batch created successfully");
        return "redirect:/batch/batch-list";
    }
}
