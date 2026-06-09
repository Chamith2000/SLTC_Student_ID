package com.oexil.studentreg.service.impl;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.*;
import com.oexil.studentreg.dto.student.StudentDTO;
import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.model.Student;
import com.oexil.studentreg.model.course.Batch;
import com.oexil.studentreg.model.course.Course;
import com.oexil.studentreg.repository.BatchRepository;
import com.oexil.studentreg.repository.CourseRepository;
import com.oexil.studentreg.repository.StudentRepository;
import com.oexil.studentreg.service.CurrentUser;
import com.oexil.studentreg.service.ImageUploadService;
import com.oexil.studentreg.service.StudentService;
import com.oexil.studentreg.utils.FileUtilizer;
import com.oexil.studentreg.utils.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;
    private final ImageUploadService imageUploadService;
    private final CurrentUser currentUser;
    private final TemplateEngine templateEngine;

    @Value("${archive.path}")
    private String archivePath;

    @Override
    public List<StudentDTO> getAllStudent(Model model) {
        List<Student> students = studentRepository.findAll();

        List<StudentDTO> dtos = students.stream()
                .map(student -> {
                    StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
                    studentDTO.setQr(QRCodeUtil.generateQRCode(studentDTO.getRegNo()));
                    studentDTO.setQrBase64(Base64.encodeBase64String(studentDTO.getQr()));
                    return studentDTO;
                })
                .collect(Collectors.toList());

        model.addAttribute("students", dtos);
        return dtos;
    }

    @Override
    public String registerStudent(StudentDTO studentDTO) {

        boolean exists = studentRepository.existsByNicOrRegNoOrPhoneNumber(studentDTO.getNic(), studentDTO.getRegNo(), studentDTO.getPhoneNumber());

        if (exists) return "DUPLICATE";

        Course course = courseRepository.findById(studentDTO.getCourseId()).orElseThrow();
        Batch batch = batchRepository.findById(studentDTO.getBatchId()).orElseThrow();
        Student student = new Student();
        student.setCourse(course);
        student.setBatch(batch);
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setDisplayName(studentDTO.getDisplayName());
        student.setNic(studentDTO.getNic());
        student.setRegNo(studentDTO.getRegNo());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setCreateDate(new Date());
        student.setConfirmationStatus(ConfirmationStatus.PENDING);
        student.setConfirmationStatusChangeTime(new Date());
        student.setActionUser(currentUser.getUser());

        String[] fileWriteResults = imageUploadService.getResultsOfFileWrite(studentDTO.getImage());
        Optional.ofNullable(fileWriteResults).ifPresent(results -> {
            student.setFilePath(results[1]);
            student.setFileName(results[2]);
        });

        studentRepository.save(student);
        return "SUCCESS";
    }

    @Override
    public String updateStudent(StudentDTO studentDTO) {
        Student student = studentRepository.findById(studentDTO.getId()).orElseThrow(() -> new RuntimeException("Student not found"));

        // Check for duplicate NIC, RegNo, or PhoneNumber
        boolean existsNic = studentRepository.existsByNicAndIdNot(studentDTO.getNic(), studentDTO.getId());
        boolean existsRegNo = studentRepository.existsByRegNoAndIdNot(studentDTO.getRegNo(), studentDTO.getId());
        boolean existsPhone = studentRepository.existsByPhoneNumberAndIdNot(studentDTO.getPhoneNumber(), studentDTO.getId());

        if (existsNic) {
            return "DUPLICATE_NIC";
        } else if (existsRegNo) {
            return "DUPLICATE_REGNO";
        } else if (existsPhone) {
            return "DUPLICATE_PHONE";
        }

        Course course = courseRepository.findById(studentDTO.getCourseId()).orElseThrow();
        Batch batch = batchRepository.findById(studentDTO.getBatchId()).orElseThrow();
        student.setCourse(course);
        student.setBatch(batch);

        // Update student details
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setDisplayName(studentDTO.getDisplayName());
        student.setNic(studentDTO.getNic());
        student.setRegNo(studentDTO.getRegNo());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setIssuedDate(studentDTO.getIssuedDate());
        student.setExpiryDate(studentDTO.getExpiryDate());
        student.setUpdateDate(new Date()); // Update timestamp

        if(student.getConfirmationStatus() == ConfirmationStatus.UNCONFIRMED) {
            student.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
            student.setConfirmed(true);
            student.setPrinted(false);
        }else{
            student.setConfirmationStatus(ConfirmationStatus.PENDING);
        }
        student.setConfirmationStatusChangeTime(new Date());
        student.setActionUser(currentUser.getUser());

        if (studentDTO.getImage() != null && !studentDTO.getImage().isEmpty()) {
            String[] fileWriteResults = imageUploadService.getResultsOfFileWrite(studentDTO.getImage());
            Optional.ofNullable(fileWriteResults).ifPresent(results -> {
                student.setFilePath(results[1]);
                student.setFileName(results[2]);
            });
        }

        studentRepository.save(student);
        return student.getConfirmationStatus() == ConfirmationStatus.CONFIRMED ? "CONFIRMED" : "SUCCESS";
    }

    @Override
    @Transactional
    public String registerStudentsBatch(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            boolean isFirstRow = true;

            List<Student> studentList = new ArrayList<>();

            while (rows.hasNext()) {
                Row row = rows.next();

                if (isFirstRow) { // Skip header row
                    isFirstRow = false;
                    continue;
                }

                String nic = getCellValue(row.getCell(4));
                String regNo = getCellValue(row.getCell(3));
                String phoneNumber = getCellValue(row.getCell(5));

                // Check for existing student with same NIC, Reg No, or Phone Number
//                boolean exists = studentRepository.existsByNicOrRegNoOrPhoneNumber(nic, regNo, phoneNumber);
//                if (exists) return "ERROR : Duplicate Student : " + "Reg No." + regNo;

                // Check for existing student with same Reg No Only....... SPLASHILY REQUIREMENT
                boolean exists = studentRepository.existsByRegNo(regNo);
                if (exists) return "ERROR : Duplicate Student : " + "Reg No." + regNo;

                Student student = new Student();
                Long courseId = Long.parseLong(getCellValue(row.getCell(6)));
                Long batchId = Long.parseLong(getCellValue(row.getCell(7)));

                Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Invalid Course ID: " + courseId));
                Batch batch = batchRepository.findById(batchId).orElseThrow(() -> new IllegalArgumentException("Invalid Batch ID: " + batchId));

                student.setCourse(course);
                student.setBatch(batch);
                student.setFirstName(getCellValue(row.getCell(0)));
                student.setLastName(getCellValue(row.getCell(1)));
                student.setDisplayName(getCellValue(row.getCell(2)));
                student.setNic(nic);
                student.setRegNo(regNo);
                student.setPhoneNumber(phoneNumber);
                student.setCreateDate(new Date());
                student.setConfirmationStatus(ConfirmationStatus.PENDING);
                student.setConfirmationStatusChangeTime(new Date());
                student.setActionUser(currentUser.getUser());

                studentList.add(student);
            }

            studentRepository.saveAll(studentList);
            return "SUCCESS";
        } catch (Exception exception) {
            exception.printStackTrace();
            return "ERROR";
        }
    }


    @Override
    public String deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        if (student.getFilePath() != null && !student.getFilePath().isEmpty()) FileUtilizer.deleteFile(student.getFilePath());
        studentRepository.delete(student);
        return "SUCCESS";
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return null;

        StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
        studentDTO.setQr(QRCodeUtil.generateQRCode(studentDTO.getRegNo()));
        studentDTO.setQrBase64(Base64.encodeBase64String(studentDTO.getQr()));

        return studentDTO;
    }

    @Override
    public StudentDTO getStudentByRegNo(String query) {
        Student student = studentRepository.findByRegNo(query).orElse(null);
        if (student == null) return null;

        StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
        studentDTO.setQr(QRCodeUtil.generateQRCode(studentDTO.getRegNo()));
        studentDTO.setQrBase64(Base64.encodeBase64String(studentDTO.getQr()));

        return studentDTO;
    }

    @Override
    public StudentDTO getStudentByNic(String query) {
        Student student = studentRepository.findByNic(query).orElse(null);
        if (student == null) return null;

        StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);

        if (student.getFilePath() != null) {
            try {
                Path imagePath = Paths.get(archivePath, student.getFilePath().replace("/files/", ""));
                byte[] imageBytes = Files.readAllBytes(imagePath);
                String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                studentDTO.setProfileImageBase64("data:image/png;base64," + base64Image);
            } catch (IOException e) {
                System.err.println("Error reading image: " + e.getMessage());
            }
        } else {
            studentDTO.setProfileImageBase64("/images/student-id/default-avatar.jpg");
        }

        studentDTO.setQr(QRCodeUtil.generateQRCode(studentDTO.getRegNo()));
        studentDTO.setQrBase64(Base64.encodeBase64String(studentDTO.getQr()));

        return studentDTO;
    }

    @Override
    public void confirmStudentDetails(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        student.setConfirmed(true);
        student.setPrinted(false);
        student.setConfirmedActionTime(new Date());
        student.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        student.setConfirmationStatusChangeTime(new Date());
        studentRepository.save(student);
    }

    @Override
    public void reportStudentCorrections(Long studentId, String corrections) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        student.setConfirmed(false);
        student.setCorrections(corrections);
        student.setConfirmedActionTime(new Date());
        student.setConfirmationStatus(ConfirmationStatus.UNCONFIRMED);
        student.setConfirmationStatusChangeTime(new Date());
        studentRepository.save(student);
    }

    @Override
    public void getAllConfirmedStudents(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> studentPage = studentRepository.findByConfirmedTrue(pageable);

        // Convert Page<Student> to Page<StudentDTO> with QR codes
        Page<StudentDTO> studentDTOPage = studentPage.map(student -> {
            StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
            try {
                byte[] qrCode = QRCodeUtil.generateQRCode(studentDTO.getRegNo());
                studentDTO.setQr(qrCode);
                studentDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                // Handle QR code generation failure (e.g., log error, set default QR)
                studentDTO.setQrBase64("");
            }
            return studentDTO;
        });

        model.addAttribute("students", studentDTOPage);
    }

    @Override
    public List<StudentDTO> getAllPendingStudents(Model model) {
        List<Student> students = studentRepository.findAllByConfirmedIsFalseAndConfirmationStatus(ConfirmationStatus.UNCONFIRMED);

        List<StudentDTO> dtos = students.stream()
                .map(student -> {
                    StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
                    studentDTO.setQr(QRCodeUtil.generateQRCode(studentDTO.getRegNo()));
                    studentDTO.setQrBase64(Base64.encodeBase64String(studentDTO.getQr()));
                    return studentDTO;
                })
                .collect(Collectors.toList());

        model.addAttribute("students", dtos);
        return dtos;
    }

    @Override
    public void getAllPrintedStudents(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> studentPage = studentRepository.findByPrintedTrue(pageable);

        // Convert Page<Student> to Page<StudentDTO> with QR codes
        Page<StudentDTO> studentDTOPage = studentPage.map(student -> {
            StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
            try {
                byte[] qrCode = QRCodeUtil.generateQRCode(studentDTO.getRegNo());
                studentDTO.setQr(qrCode);
                studentDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                // Handle QR code generation failure (e.g., log error, set default QR)
                studentDTO.setQrBase64("");
            }
            return studentDTO;
        });

        model.addAttribute("students", studentDTOPage);
    }

    @Override
    public void getAllPendingToPrint(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> studentPage = studentRepository.findByConfirmedTrueAndPrintedFalse(pageable);

        // Convert Page<Student> to Page<StudentDTO> with QR codes
        Page<StudentDTO> studentDTOPage = studentPage.map(student -> {
            StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
            try {
                byte[] qrCode = QRCodeUtil.generateQRCode(studentDTO.getRegNo());
                studentDTO.setQr(qrCode);
                studentDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                // Handle QR code generation failure (e.g., log error, set default QR)
                studentDTO.setQrBase64("");
            }
            return studentDTO;
        });

        model.addAttribute("students", studentDTOPage);
    }

    @Override
    public void getAllPendingToPrintUnconfirmed(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> studentPage = studentRepository.findByConfirmedFalseAndConfirmationStatus(
                ConfirmationStatus.UNCONFIRMED, pageable
        );

        Page<StudentDTO> studentDTOPage = studentPage.map(student -> {
            StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
            try {
                byte[] qrCode = QRCodeUtil.generateQRCode(studentDTO.getRegNo());
                studentDTO.setQr(qrCode);
                studentDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                studentDTO.setQrBase64("");
            }
            return studentDTO;
        });

        model.addAttribute("students", studentDTOPage);
    }

//    @Override
//    public void generateAllIdsPdf(HttpServletResponse response, int page, int size) throws Exception {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Student> studentPage = studentRepository.findByConfirmedTrueAndPrintedFalse(pageable);
//
//        List<StudentDTO> dtoList = studentPage.getContent().stream().map(student -> {
//            StudentDTO dto = modelMapper.map(student, StudentDTO.class);
//
//            // QR generate
//            byte[] qr = QRCodeUtil.generateQRCode(dto.getRegNo());
//            dto.setQr(qr);
//            dto.setQrBase64(Base64.encodeBase64String(qr));
//
//            if (student.getFilePath() != null && !student.getFilePath().isEmpty()) {
//                try {
//                    Path imgPath = Paths.get(archivePath,
//                            student.getFilePath().replace("/files/", ""));
//                    byte[] imgBytes = Files.readAllBytes(imgPath);
//                    dto.setProfileImageBase64("data:image/jpeg;base64,"
//                            + java.util.Base64.getEncoder().encodeToString(imgBytes));
//                } catch (Exception e) {
//                    dto.setProfileImageBase64(null);
//                }
//            }
//            return dto;
//        }).collect(Collectors.toList());
//
//        Context ctx = new Context();
//        ctx.setVariable("students", dtoList);
//        String htmlContent = templateEngine.process("student/print/print-all-ids-pdf", ctx);
//
//        // 4. Flying Saucer → PDF
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=\"student-ids.pdf\"");
//
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocumentFromString(htmlContent);
//        renderer.layout();
//        renderer.createPDF(response.getOutputStream());
//    }
}
