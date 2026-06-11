package com.oexil.studentreg.repository;

import com.oexil.studentreg.enums.CardStatus;
import com.oexil.studentreg.model.StudentIdCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentIdCardRepository extends JpaRepository<StudentIdCard, Long> {

    Optional<StudentIdCard> findByStudentIdAndIsActiveTrue(Long studentId);

    List<StudentIdCard> findByStudentIdInAndIsActiveTrue(List<Long> studentIds);

    @Query(value = "SELECT sic, sc FROM StudentIdCard sic " +
                   "LEFT JOIN StudentConfirmation sc ON sc.student = sic.student " +
                   "WHERE sic.cardStatus = :status AND sic.isActive = true",
           countQuery = "SELECT COUNT(sic) FROM StudentIdCard sic WHERE sic.cardStatus = :status AND sic.isActive = true")
    Page<Object[]> findByCardStatusWithConfirmation(@Param("status") CardStatus status, Pageable pageable);

    @Query(value = "SELECT sic, sc FROM StudentIdCard sic " +
                   "LEFT JOIN StudentConfirmation sc ON sc.student = sic.student " +
                   "WHERE sic.cardStatus IN :statuses AND sic.isActive = true",
           countQuery = "SELECT COUNT(sic) FROM StudentIdCard sic WHERE sic.cardStatus IN :statuses AND sic.isActive = true")
    Page<Object[]> findByCardStatusInWithConfirmation(@Param("statuses") List<CardStatus> statuses, Pageable pageable);

    @Query("SELECT sic.cardStatus, COUNT(sic) FROM StudentIdCard sic WHERE sic.isActive = true GROUP BY sic.cardStatus")
    List<Object[]> countGroupedByStatus();
}
