package com.oexil.studentreg.model;

import com.oexil.studentreg.enums.CardStatus;
import com.oexil.studentreg.enums.CardType;
import com.oexil.studentreg.enums.ReprintReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "student_id_card")
public class StudentIdCard implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "card_status")
    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    @Column(name = "pdf_generated_at")
    private Date pdfGeneratedAt;

    @Column(name = "printed_at")
    private Date printedAt;

    @Column(name = "issued_at")
    private Date issuedAt;

    @Column(name = "print_label")
    private String printLabel;

    @Column(name = "issued_date")
    private Date issuedDate;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @Column(name = "card_type")
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    // Null when card_type = INITIAL
    @Column(name = "reprint_reason")
    @Enumerated(EnumType.STRING)
    private ReprintReason reprintReason;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    // Points to the card being replaced; null when card_type = INITIAL
    @ManyToOne
    @JoinColumn(name = "previous_card_id")
    private StudentIdCard previousCard;

    // False once this card is replaced by a newer one or revoked
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (cardStatus == null) {
            cardStatus = CardStatus.PRINT_PENDING;
        }
        if (cardType == null) {
            cardType = CardType.INITIAL;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
