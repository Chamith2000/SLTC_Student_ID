package com.oexil.studentreg.service.impl;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.*;
import com.oexil.studentreg.dto.student.StudentDTO;
import com.oexil.studentreg.enums.CardStatus;
import com.oexil.studentreg.enums.CardType;
import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.model.Student;
import com.oexil.studentreg.model.StudentConfirmation;
import com.oexil.studentreg.model.StudentIdCard;
import com.oexil.studentreg.model.course.Batch;
import com.oexil.studentreg.model.course.Course;
import com.oexil.studentreg.repository.BatchRepository;
import com.oexil.studentreg.repository.CourseRepository;
import com.oexil.studentreg.repository.StudentConfirmationRepository;
import com.oexil.studentreg.repository.StudentIdCardRepository;
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
    private final StudentConfirmationRepository studentConfirmationRepository;
    private final StudentIdCardRepository studentIdCardRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;
    private final ImageUploadService imageUploadService;
    private final CurrentUser currentUser;
    private final TemplateEngine templateEngine;

    @Value("${archive.path}")
    private String archivePath;

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a fully-populated StudentDTO from the three separate entities.
     * Confirmation and card may be null (e.g. for a brand-new student).
     */
    private StudentDTO buildStudentDTO(Student student, StudentConfirmation confirmation, StudentIdCard card) {
        StudentDTO dto = modelMapper.map(student, StudentDTO.class);

        if (confirmation != null) {
            dto.setConfirmationStatus(confirmation.getConfirmationStatus());
            dto.setConfirmedActionTime(confirmation.getConfirmedActionTime());
            dto.setConfirmationStatusChangeTime(confirmation.getConfirmationStatusChangeTime());
            dto.setCorrections(confirmation.getCorrections());
            dto.setConfirmed(ConfirmationStatus.CONFIRMED == confirmation.getConfirmationStatus());
        }

        if (card != null) {
            dto.setCardId(card.getId());
            dto.setCardStatus(card.getCardStatus());
            dto.setIssuedDate(card.getIssuedDate());
            dto.setExpiryDate(card.getExpiryDate());
            dto.setPrintLabel(card.getPrintLabel());
            dto.setPdfGeneratedAt(card.getPdfGeneratedAt());
            dto.setPrintedAt(card.getPrintedAt());
            dto.setIssuedAt(card.getIssuedAt());
            dto.setCardType(card.getCardType());
            dto.setReprintReason(card.getReprintReason());
        }

        return dto;
    }

    private void attachQrCode(StudentDTO dto) {
        try {
            byte[] qr = QRCodeUtil.generateQRCode(dto.getRegNo());
            dto.setQr(qr);
            dto.setQrBase64(Base64.encodeBase64String(qr));
        } catch (Exception e) {
            dto.setQrBase64("");
        }
    }

    // -------------------------------------------------------------------------
    // Student CRUD
    // -------------------------------------------------------------------------

    @Override
    public void getAllStudent(Model model, int page, int size,
                             String search, Long courseId, Long batchId,
                             String cardStatus, String confirmationStatus) {
        Pageable pageable = PageRequest.of(page, size);

        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        CardStatus cardStatusParam = (cardStatus != null && !cardStatus.isBlank())
                ? CardStatus.valueOf(cardStatus) : null;
        ConfirmationStatus confirmationStatusParam = (confirmationStatus != null && !confirmationStatus.isBlank())
                ? ConfirmationStatus.valueOf(confirmationStatus) : null;

        Page<Object[]> results = studentRepository.findAllStudentsWithFilters(
                searchParam, courseId, batchId, cardStatusParam, confirmationStatusParam, pageable);

        Page<StudentDTO> dtos = results.map(row -> buildStudentDTO(
                (Student) row[0],
                (StudentConfirmation) row[1],
                (StudentIdCard) row[2]
        ));

        model.addAttribute("students", dtos);
    }

    @Override
    public String registerStudent(StudentDTO studentDTO) {
        boolean exists = studentRepository.existsByNicOrRegNoOrPhoneNumber(
                studentDTO.getNic(), studentDTO.getRegNo(), studentDTO.getPhoneNumber());
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
        student.setActionUser(currentUser.getUser());

        if (studentDTO.getImage() != null && !studentDTO.getImage().isEmpty()) {
            String[] fileWriteResults = imageUploadService.getResultsOfFileWrite(studentDTO.getImage(), studentDTO.getRegNo());
            Optional.ofNullable(fileWriteResults).ifPresent(results -> {
                student.setFilePath(results[1]);
                student.setFileName(results[2]);
            });
        }

        Student savedStudent = studentRepository.save(student);

        // Create the confirmation record — @PrePersist sets PENDING + timestamps
        StudentConfirmation confirmation = new StudentConfirmation();
        confirmation.setStudent(savedStudent);
        studentConfirmationRepository.save(confirmation);

        StudentIdCard card = new StudentIdCard();
        card.setStudent(savedStudent);
        card.setIssuedDate(studentDTO.getIssuedDate());
        card.setExpiryDate(studentDTO.getExpiryDate());
        card.setCardStatus(CardStatus.PRINT_PENDING);
        studentIdCardRepository.save(card);

        return "SUCCESS";
    }

    @Override
    public String updateStudent(StudentDTO studentDTO) {
        Student student = studentRepository.findById(studentDTO.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (studentRepository.existsByNicAndIdNot(studentDTO.getNic(), studentDTO.getId()))
            return "DUPLICATE_NIC";
        if (studentRepository.existsByRegNoAndIdNot(studentDTO.getRegNo(), studentDTO.getId()))
            return "DUPLICATE_REGNO";
        if (studentRepository.existsByPhoneNumberAndIdNot(studentDTO.getPhoneNumber(), studentDTO.getId()))
            return "DUPLICATE_PHONE";

        Course course = courseRepository.findById(studentDTO.getCourseId()).orElseThrow();
        Batch batch = batchRepository.findById(studentDTO.getBatchId()).orElseThrow();
        student.setCourse(course);
        student.setBatch(batch);
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setDisplayName(studentDTO.getDisplayName());
        student.setNic(studentDTO.getNic());
        student.setRegNo(studentDTO.getRegNo());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setUpdateDate(new Date());
        student.setActionUser(currentUser.getUser());

        if (studentDTO.getImage() != null && !studentDTO.getImage().isEmpty()) {
            String[] fileWriteResults = imageUploadService.getResultsOfFileWrite(studentDTO.getImage(), studentDTO.getRegNo());
            Optional.ofNullable(fileWriteResults).ifPresent(results -> {
                student.setFilePath(results[1]);
                student.setFileName(results[2]);
            });
        }

        studentRepository.save(student);

        // Update confirmation status
        StudentConfirmation confirmation = studentConfirmationRepository
                .findByStudentId(student.getId()).orElse(null);

        if (confirmation == null) {
            return "SUCCESS";
        }

// 1. Unconfirmed status eke thiyena welawaka (Meka oyage parana logic eka)
        if (confirmation.getConfirmationStatus() == ConfirmationStatus.UNCONFIRMED) {
            confirmation.setConfirmationStatus(ConfirmationStatus.PENDING);
            studentConfirmationRepository.save(confirmation);

            StudentIdCard card = studentIdCardRepository
                    .findByStudentIdAndIsActiveTrue(student.getId()).orElse(null);

            if (card == null) {
                StudentIdCard newCard = new StudentIdCard();
                newCard.setStudent(student);
                newCard.setIssuedDate(studentDTO.getIssuedDate());
                newCard.setExpiryDate(studentDTO.getExpiryDate());
                studentIdCardRepository.save(newCard);
            } else {
                card.setIssuedDate(studentDTO.getIssuedDate());
                card.setExpiryDate(studentDTO.getExpiryDate());
                studentIdCardRepository.save(card);
            }
            return "CONFIRMED";

        } else {

            StudentIdCard card = studentIdCardRepository
                    .findByStudentIdAndIsActiveTrue(student.getId()).orElse(null);

            if (card == null) {
                StudentIdCard newCard = new StudentIdCard();
                newCard.setStudent(student);
                newCard.setIssuedDate(studentDTO.getIssuedDate());
                newCard.setExpiryDate(studentDTO.getExpiryDate());
                studentIdCardRepository.save(newCard);
            } else {
                card.setIssuedDate(studentDTO.getIssuedDate());
                card.setExpiryDate(studentDTO.getExpiryDate());
                studentIdCardRepository.save(card);
            }
        }

        return "SUCCESS";
    }

    @Override
    @Transactional
    public String registerStudentsBatch(MultipartFile file, Date issuedDate, Date expiryDate) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            boolean isFirstRow = true;

            List<Student> studentList = new ArrayList<>();

            while (rows.hasNext()) {
                Row row = rows.next();

                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                String nic = getCellValue(row.getCell(4));
                String regNo = getCellValue(row.getCell(3));
                String phoneNumber = getCellValue(row.getCell(5));

                if (regNo.isEmpty()) continue;

                // Check for existing student with same Reg No
                boolean exists = studentRepository.existsByRegNo(regNo);
                if (exists) return "ERROR : Duplicate Student : Reg No." + regNo;

                String courseIdStr = getCellValue(row.getCell(6));
                String batchIdStr = getCellValue(row.getCell(7));

                if (courseIdStr.isEmpty()) return "ERROR : Missing Course ID for Reg No." + regNo;
                if (batchIdStr.isEmpty()) return "ERROR : Missing Batch ID for Reg No." + regNo;

                Long courseId = Long.parseLong(courseIdStr);
                Long batchId = Long.parseLong(batchIdStr);

                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Course ID: " + courseId));
                Batch batch = batchRepository.findById(batchId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Batch ID: " + batchId));

                Student student = new Student();
                student.setCourse(course);
                student.setBatch(batch);
                student.setFirstName(getCellValue(row.getCell(0)));
                student.setLastName(getCellValue(row.getCell(1)));
                student.setDisplayName(getCellValue(row.getCell(2)));
                student.setNic(nic);
                student.setRegNo(regNo);
                student.setPhoneNumber(phoneNumber);
                student.setCreateDate(new Date());
                student.setActionUser(currentUser.getUser());

                student.setFileName(regNo + ".jpg");
                student.setFilePath("files/" + regNo + ".jpg");

                studentList.add(student);
            }

            List<Student> savedStudents = studentRepository.saveAll(studentList);

            for (Student student : savedStudents) {
                StudentIdCard card = new StudentIdCard();
                card.setStudent(student);
                card.setIssuedDate(issuedDate);
                card.setExpiryDate(expiryDate);
                card.setCardStatus(CardStatus.PRINT_PENDING);
                studentIdCardRepository.save(card);
            }

            // Create a PENDING confirmation record for every saved student
            List<StudentConfirmation> confirmations = savedStudents.stream()
                    .map(student -> {
                        StudentConfirmation confirmation = new StudentConfirmation();
                        confirmation.setStudent(student);
                        return confirmation;
                    })
                    .collect(Collectors.toList());
            studentConfirmationRepository.saveAll(confirmations);

            return "SUCCESS";
        } catch (Exception exception) {
            exception.printStackTrace();
            return "ERROR";
        }
    }

    @Override
    public String deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (student.getFilePath() != null && !student.getFilePath().isEmpty())
            FileUtilizer.deleteFile(student.getFilePath());
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

    // -------------------------------------------------------------------------
    // Single-student lookups
    // -------------------------------------------------------------------------

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return null;

        StudentConfirmation confirmation = studentConfirmationRepository.findByStudentId(id).orElse(null);
        StudentIdCard card = studentIdCardRepository.findByStudentIdAndIsActiveTrue(id).orElse(null);

        StudentDTO dto = buildStudentDTO(student, confirmation, card);
        attachQrCode(dto);
        return dto;
    }

    @Override
    public StudentDTO getStudentByRegNo(String query) {
        Student student = studentRepository.findByRegNo(query).orElse(null);
        if (student == null) return null;

        StudentConfirmation confirmation = studentConfirmationRepository.findByStudentId(student.getId()).orElse(null);
        StudentIdCard card = studentIdCardRepository.findByStudentIdAndIsActiveTrue(student.getId()).orElse(null);

        StudentDTO dto = buildStudentDTO(student, confirmation, card);
        attachQrCode(dto);
        return dto;
    }

    @Override
    public StudentDTO getStudentByNic(String query) {
        Student student = studentRepository.findByNic(query).orElse(null);
        if (student == null) return null;

        StudentConfirmation confirmation = studentConfirmationRepository.findByStudentId(student.getId()).orElse(null);
        StudentIdCard card = studentIdCardRepository.findByStudentIdAndIsActiveTrue(student.getId()).orElse(null);

        StudentDTO dto = buildStudentDTO(student, confirmation, card);

        if (student.getFilePath() != null) {
            try {
                Path imagePath = Paths.get(archivePath, student.getFilePath().replace("/files/", ""));
                byte[] imageBytes = Files.readAllBytes(imagePath);
                String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                dto.setProfileImageBase64("data:image/png;base64," + base64Image);
            } catch (IOException e) {
                System.err.println("Error reading image: " + e.getMessage());
            }
        } else {
            dto.setProfileImageBase64("/images/student-id/default-avatar.jpg");
        }

        attachQrCode(dto);
        return dto;
    }

    // -------------------------------------------------------------------------
    // Confirmation workflow
    // -------------------------------------------------------------------------

    @Override
    public void confirmStudentDetails(Long studentId) {
        StudentConfirmation confirmation = studentConfirmationRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Confirmation record not found for student: " + studentId));

        confirmation.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        confirmation.setConfirmedActionTime(new Date());
        confirmation.setConfirmationStatusChangeTime(new Date());
        studentConfirmationRepository.save(confirmation);

        // Create a PRINT_PENDING card record if none exists yet
        boolean cardExists = studentIdCardRepository.findByStudentIdAndIsActiveTrue(studentId).isPresent();
        if (!cardExists) {
            Student student = studentRepository.findById(studentId).orElseThrow();
            StudentIdCard card = new StudentIdCard();
            card.setStudent(student);
            // @PrePersist sets PRINT_PENDING, INITIAL, isActive = true
            studentIdCardRepository.save(card);
        }
    }

    @Override
    public void reportStudentCorrections(Long studentId, String corrections) {
        StudentConfirmation confirmation = studentConfirmationRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Confirmation record not found for student: " + studentId));

        confirmation.setConfirmationStatus(ConfirmationStatus.UNCONFIRMED);
        confirmation.setCorrections(corrections);
        confirmation.setConfirmedActionTime(new Date());
        confirmation.setConfirmationStatusChangeTime(new Date());
        studentConfirmationRepository.save(confirmation);
    }

    // -------------------------------------------------------------------------
    // Paginated list queries
    // -------------------------------------------------------------------------

    @Override
    public void getAllConfirmedStudents(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rawPage = studentConfirmationRepository
                .findByConfirmationStatusWithCard(ConfirmationStatus.CONFIRMED, pageable);

        Page<StudentDTO> dtoPage = rawPage.map(row -> {
            StudentConfirmation confirmation = (StudentConfirmation) row[0];
            StudentIdCard card = (StudentIdCard) row[1];
            StudentDTO dto = buildStudentDTO(confirmation.getStudent(), confirmation, card);
            attachQrCode(dto);
            return dto;
        });

        model.addAttribute("students", dtoPage);
    }

    @Override
    public List<StudentDTO> getAllPendingStudents(Model model) {
        List<Object[]> results = studentConfirmationRepository
                .findAllSubmittedForConfirmationWithCard(ConfirmationStatus.PENDING, ConfirmationStatus.UNCONFIRMED);

        List<StudentDTO> dtos = results.stream()
                .map(row -> {
                    StudentConfirmation confirmation = (StudentConfirmation) row[0];
                    StudentIdCard card = (StudentIdCard) row[1];
                    StudentDTO dto = buildStudentDTO(confirmation.getStudent(), confirmation, card);
                    attachQrCode(dto);
                    return dto;
                })
                .collect(Collectors.toList());

        model.addAttribute("students", dtos);
        return dtos;
    }

    // -------------------------------------------------------------------------
    // Print workflow
    // -------------------------------------------------------------------------

    @Override
    public void getAllPendingToPrint(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rawPage = studentIdCardRepository
                .findByCardStatusWithConfirmation(CardStatus.PRINT_PENDING, pageable);

        Page<StudentDTO> dtoPage = rawPage.map(row -> {
            StudentIdCard card = (StudentIdCard) row[0];
            StudentConfirmation confirmation = (StudentConfirmation) row[1];
            StudentDTO dto = buildStudentDTO(card.getStudent(), confirmation, card);
            attachQrCode(dto);
            return dto;
        });

        model.addAttribute("students", dtoPage);
    }

    @Override
    public List<StudentDTO> getRecentlyAddedStudents(Model model) {
        List<Object[]> results = studentConfirmationRepository
                .findPendingNotSubmittedWithCard(ConfirmationStatus.PENDING);

        List<StudentDTO> dtos = results.stream()
                .map(row -> {
                    StudentConfirmation confirmation = (StudentConfirmation) row[0];
                    StudentIdCard card = (StudentIdCard) row[1];
                    return buildStudentDTO(confirmation.getStudent(), confirmation, card);
                })
                .collect(Collectors.toList());

        model.addAttribute("students", dtos);
        return dtos;
    }

    @Override
    @Transactional
    public String requestReprint(Long studentId, String requestReason) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        // Deactivate the current active card
        StudentIdCard existingCard = studentIdCardRepository
                .findByStudentIdAndIsActiveTrue(studentId).orElse(null);

        if (existingCard != null) {
            existingCard.setIsActive(false);
            studentIdCardRepository.save(existingCard);
        }

        // Create a new reprint card — @PrePersist sets PRINT_PENDING and isActive = true
        StudentIdCard reprintCard = new StudentIdCard();
        reprintCard.setStudent(student);
        reprintCard.setCardType(CardType.REPRINT);
        reprintCard.setRequestReason(requestReason);
        reprintCard.setPreviousCard(existingCard);
        studentIdCardRepository.save(reprintCard);

        return "SUCCESS";
    }

    @Override
    @Transactional
    public void markAsPrinted(List<Long> studentIds, String printLabel) {
        Date now = new Date();
        List<StudentIdCard> cards = studentIdCardRepository.findByStudentIdInAndIsActiveTrue(studentIds);
        for (StudentIdCard card : cards) {
            card.setCardStatus(CardStatus.PRINTED);
            card.setPrintedAt(now);
            card.setPrintLabel(printLabel);
        }
        studentIdCardRepository.saveAll(cards);
    }

    @Override
    @Transactional
    public void makeAvailableForConfirmation(List<Long> studentIds) {
        Date now = new Date();
        List<StudentConfirmation> confirmations = studentConfirmationRepository.findByStudentIdIn(studentIds);
        for (StudentConfirmation confirmation : confirmations) {
            if (confirmation.getConfirmationStatus() == ConfirmationStatus.PENDING) {
                confirmation.setConfirmationStatusChangeTime(now);
            }
        }
        studentConfirmationRepository.saveAll(confirmations);
    }

    @Override
    public void getAllPrintedStudents(Model model, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rawPage = studentIdCardRepository
                .findByCardStatusInWithConfirmation(List.of(CardStatus.PRINTED, CardStatus.ISSUED), pageable);

        Page<StudentDTO> dtoPage = rawPage.map(row -> {
            StudentIdCard card = (StudentIdCard) row[0];
            StudentConfirmation confirmation = (StudentConfirmation) row[1];
            StudentDTO dto = buildStudentDTO(card.getStudent(), confirmation, card);
            attachQrCode(dto);
            return dto;
        });

        model.addAttribute("students", dtoPage);
    }

    // java/com/oexil/studentreg/service/impl/StudentServiceImpl.java

    @Override
    public void getAllCorrectionStudents(Model model) {
        // Status eka UNCONFIRMED eka thiyena aya thama correction list ekata enne
        List<Object[]> results = studentConfirmationRepository
                .findAllSubmittedForConfirmationWithCard(ConfirmationStatus.PENDING, ConfirmationStatus.UNCONFIRMED);

        // Filter out only UNCONFIRMED students
        List<StudentDTO> dtos = results.stream()
                .map(row -> {
                    StudentConfirmation confirmation = (StudentConfirmation) row[0];
                    StudentIdCard card = (StudentIdCard) row[1];
                    return buildStudentDTO(confirmation.getStudent(), confirmation, card);
                })
                .filter(dto -> dto.getConfirmationStatus() == ConfirmationStatus.UNCONFIRMED)
                .collect(Collectors.toList());

        model.addAttribute("students", dtos);
    }

//    @Override
//    public void getAllPendingToPrintUnconfirmed(Model model, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Student> studentPage = studentRepository.findByConfirmedFalseAndConfirmationStatus(
//                ConfirmationStatus.UNCONFIRMED, pageable
//        );
//
//        Page<StudentDTO> studentDTOPage = studentPage.map(student -> {
//            StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);
//            try {
//                byte[] qrCode = QRCodeUtil.generateQRCode(studentDTO.getRegNo());
//                studentDTO.setQr(qrCode);
//                studentDTO.setQrBase64(Base64.encodeBase64String(qrCode));
//            } catch (Exception e) {
//                studentDTO.setQrBase64("");
//            }
//            return studentDTO;
//        });
//
//        model.addAttribute("students", studentDTOPage);
//    }

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
