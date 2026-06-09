package com.oexil.studentreg.repository;

import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByNicOrRegNoOrPhoneNumber(String nic, String regNo, String phoneNumber);

    boolean existsByRegNo(String regNo);

    boolean existsByNicOrRegNoOrPhoneNumberAndIdNot(String nic, String regNo, String phoneNumber, Long id);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.id <> :id AND (s.nic = :nic OR s.regNo = :regNo OR s.phoneNumber = :phoneNumber)")
    boolean existsByUniqueFields(Long id, String nic, String regNo, String phoneNumber);

    boolean existsByNicAndIdNot(String nic, Long id);
    boolean existsByRegNoAndIdNot(String regNo, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    Optional<Student> findByRegNo(String no);

    Optional<Student> findByNic(String no);

    List<Student> findAllByConfirmedIsTrue();

    Page<Student> findByConfirmedTrue(Pageable pageable);

    Page<Student> findByPrintedTrue(Pageable pageable);

    Page<Student> findByConfirmedTrueAndPrintedFalse(Pageable pageable);

    List<Student> findAllByConfirmedIsFalseAndConfirmationStatus(ConfirmationStatus confirmationStatus);

    Page<Student> findByConfirmedFalseAndConfirmationStatus(ConfirmationStatus confirmationStatus, Pageable pageable);
}
