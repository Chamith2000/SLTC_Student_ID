package com.oexil.studentreg.service.impl;

import com.oexil.studentreg.dto.course.BatchDTO;
import com.oexil.studentreg.dto.course.CourseDTO;
import com.oexil.studentreg.model.course.Batch;
import com.oexil.studentreg.repository.BatchRepository;
import com.oexil.studentreg.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BatchDTO> getAllBatches(Model model) {
        List<Batch> all = batchRepository.findAll();
        List<BatchDTO> dtos = modelMapper.map(all, new TypeToken<List<CourseDTO>>() {}.getType());
        model.addAttribute("batches", dtos);
        return dtos;
    }

    @Override
    public String createBatch(BatchDTO dto) {
        Batch batch = modelMapper.map(dto, Batch.class);
        batchRepository.save(batch);
        return "SUCCESS";
    }
}
