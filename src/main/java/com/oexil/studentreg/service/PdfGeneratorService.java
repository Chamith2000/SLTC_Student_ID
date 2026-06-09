package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.student.StudentDTO;
import org.springframework.stereotype.Service;


@Service
public interface PdfGeneratorService {

    byte[] generateStudentIDCard(StudentDTO student);
}
