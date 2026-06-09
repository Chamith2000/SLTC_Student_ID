package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.student.StudentDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public interface StudentService {

    List<StudentDTO> getAllStudent(Model model);

    String registerStudent(@Valid StudentDTO studentDTO);

    String registerStudentsBatch(MultipartFile file);

    StudentDTO getStudentById(Long id);

    String updateStudent(@Valid StudentDTO studentDTO);

    String deleteStudent(Long id);

    StudentDTO getStudentByRegNo(String query);

    StudentDTO getStudentByNic(String query);

    void confirmStudentDetails(Long studentId);

    void reportStudentCorrections(Long studentId, String corrections);

    void getAllConfirmedStudents(Model model, int page, int size);

    List<StudentDTO> getAllPendingStudents(Model model);

    void getAllPrintedStudents(Model model, int page, int size);

    void getAllPendingToPrint(Model model, int page, int size);

    void getAllPendingToPrintUnconfirmed(Model model, int page, int size);

//    void generateAllIdsPdf(HttpServletResponse response, int page, int size) throws Exception;
}