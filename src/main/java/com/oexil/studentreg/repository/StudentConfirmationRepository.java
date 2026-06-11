package com.oexil.studentreg.repository;

import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.model.StudentConfirmation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentConfirmationRepository extends JpaRepository<StudentConfirmation, Long> {

    Optional<StudentConfirmation> findByStudentId(Long studentId);

    @Query(value = "SELECT sc, sic FROM StudentConfirmation sc " +
                   "LEFT JOIN StudentIdCard sic ON sic.student = sc.student AND sic.isActive = true " +
                   "WHERE sc.confirmationStatus = :status",
           countQuery = "SELECT COUNT(sc) FROM StudentConfirmation sc WHERE sc.confirmationStatus = :status")
    Page<Object[]> findByConfirmationStatusWithCard(@Param("status") ConfirmationStatus status, Pageable pageable);

    @Query("SELECT sc, sic FROM StudentConfirmation sc " +
           "LEFT JOIN StudentIdCard sic ON sic.student = sc.student AND sic.isActive = true " +
           "WHERE (sc.confirmationStatus = :pending AND sc.confirmationStatusChangeTime IS NOT NULL) " +
           "OR sc.confirmationStatus = :unconfirmed")
    List<Object[]> findAllSubmittedForConfirmationWithCard(@Param("pending") ConfirmationStatus pending,
                                                           @Param("unconfirmed") ConfirmationStatus unconfirmed);

    @Query("SELECT sc, sic FROM StudentConfirmation sc " +
           "LEFT JOIN StudentIdCard sic ON sic.student = sc.student AND sic.isActive = true " +
           "WHERE sc.confirmationStatus = :pending AND sc.confirmationStatusChangeTime IS NULL " +
           "ORDER BY sc.createdAt DESC")
    List<Object[]> findPendingNotSubmittedWithCard(@Param("pending") ConfirmationStatus pending);

    List<StudentConfirmation> findByStudentIdIn(List<Long> studentIds);

    @Query("SELECT sc.confirmationStatus, COUNT(sc) FROM StudentConfirmation sc GROUP BY sc.confirmationStatus")
    List<Object[]> countGroupedByStatus();
}
