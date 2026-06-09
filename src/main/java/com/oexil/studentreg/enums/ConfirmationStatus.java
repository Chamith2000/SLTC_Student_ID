package com.oexil.studentreg.enums;

public enum ConfirmationStatus {
    PENDING,    // Data submitted, awaiting verification
    CONFIRMED,  // Data verified as correct
    UNCONFIRMED, // Data is incorrect or incomplete
    RECHECK,    // Data needs re-verification
}
