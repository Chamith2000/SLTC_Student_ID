package com.oexil.studentreg.service.impl;

import com.oexil.studentreg.dto.student.StudentDTO;
import com.oexil.studentreg.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    @Override
    public byte[] generateStudentIDCard(StudentDTO student) {
        return null;
    }
}
