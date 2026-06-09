package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.course.BatchDTO;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.validation.Valid;
import java.util.List;

@Service
public interface BatchService {

    List<BatchDTO> getAllBatches(Model model);

    String createBatch(@Valid BatchDTO dto);
}
