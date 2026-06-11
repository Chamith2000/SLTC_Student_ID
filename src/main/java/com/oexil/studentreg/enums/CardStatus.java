package com.oexil.studentreg.enums;

public enum CardStatus {
    PRINT_PENDING,  // Confirmation done, waiting to generate PDF
    PDF_GENERATED,  // PDF manually generated and ready to print
    PRINTED,        // Physical card printed
    ISSUED          // Card handed over to the student
}
