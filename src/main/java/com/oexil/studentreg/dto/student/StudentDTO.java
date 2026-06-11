package com.oexil.studentreg.dto.student;

import com.oexil.studentreg.constants.Constants;
import com.oexil.studentreg.enums.CardStatus;
import com.oexil.studentreg.enums.CardType;
import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.enums.ReprintReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
@Accessors(chain = true)
public class StudentDTO {

    private Long id;

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    @NotBlank(message = "Display Name is required")
    private String displayName;

    @NotBlank(message = "Reg No is required")
    private String regNo;

    @NotBlank(message = "NIC is required")
    private String nic;

    private String email;

    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull(message = "Course is required")
    private Long courseId;
    private String courseName;

    @NotNull(message = "Batch is required")
    private Long batchId;
    private String batchName;

    private Date createDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date issuedDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;

    private MultipartFile image;

    @Setter(AccessLevel.NONE)
    private String filePath;
    private String fileName;

    private byte[] qr;
    private String qrBase64;

    private String profileImageBase64;

    private Boolean confirmed;
    private ConfirmationStatus confirmationStatus;
    private Date confirmedActionTime;
    private Date confirmationStatusChangeTime;
    private String corrections;

    // ID card fields
    private Long cardId;
    private CardStatus cardStatus;
    private String printLabel;
    private Date pdfGeneratedAt;
    private Date printedAt;
    private Date issuedAt;
    private CardType cardType;
    private ReprintReason reprintReason;

    public void setFilePath(String filePath) {
        this.filePath = (filePath != null) ? Constants.IMAGE_URL + filePath : null;  // or provide a default path
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
