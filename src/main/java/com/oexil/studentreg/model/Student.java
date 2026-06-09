package com.oexil.studentreg.model;

import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.model.course.Batch;
import com.oexil.studentreg.model.course.Course;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "student")
public class Student implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "nic")
    private String nic;

    @Column(name = "reg_no")
    private String regNo;

    @Column(name = "student_uni_id")
    private String studentId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "issued_date")
    private Date issuedDate;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "confirmed")
    private Boolean confirmed;

    @Column(name = "confirmed_action_time")
    private Date confirmedActionTime;

    @Column(name = "confirmation status")
    @Enumerated(EnumType.STRING)
    private ConfirmationStatus confirmationStatus;

    @Column(name = "confirmed_status_change_time")
    private Date confirmationStatusChangeTime;

    @Column(name = "corrections")
    private String corrections;

    @Column(name = "printed")
    private Boolean printed;

    @Column(name = "printed_date")
    private Date printedDate;

    @Column(name = "print_label")
    private String printLabel;

    @ManyToOne
    @JoinColumn(name = "action_user")
    private User actionUser;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
