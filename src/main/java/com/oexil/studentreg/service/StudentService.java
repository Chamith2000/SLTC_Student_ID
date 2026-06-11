package com.oexil.studentreg.service;

import com.oexil.studentreg.dto.student.StudentDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;


@Service
public interface StudentService {

    void getAllStudent(Model model, int page, int size,
                      String search, Long courseId, Long batchId,
                      String cardStatus, String confirmationStatus);

    String registerStudent(@Valid StudentDTO studentDTO);

    String registerStudentsBatch(MultipartFile file, Date issuedDate, Date expiryDate);

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

    List<StudentDTO> getRecentlyAddedStudents(Model model);

    void makeAvailableForConfirmation(List<Long> studentIds);

    void markAsPrinted(List<Long> studentIds, String printLabel);

    String requestReprint(Long studentId, String requestReason);

    void getAllCorrectionStudents(Model model);

//    public void getAllPendingToPrintUnconfirmed(Model model, int page, int size);
}