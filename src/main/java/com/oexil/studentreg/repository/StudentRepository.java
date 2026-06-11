package com.oexil.studentreg.repository;

import com.oexil.studentreg.enums.CardStatus;
import com.oexil.studentreg.enums.ConfirmationStatus;
import com.oexil.studentreg.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByNicOrRegNoOrPhoneNumber(String nic, String regNo, String phoneNumber);

    boolean existsByRegNo(String regNo);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.id <> :id AND (s.nic = :nic OR s.regNo = :regNo OR s.phoneNumber = :phoneNumber)")
    boolean existsByUniqueFields(Long id, String nic, String regNo, String phoneNumber);

    boolean existsByNicAndIdNot(String nic, Long id);

    boolean existsByRegNoAndIdNot(String regNo, Long id);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    Optional<Student> findByRegNo(String regNo);

    Optional<Student> findByNic(String nic);

    @Query("SELECT s, sc, sic FROM Student s " +
           "LEFT JOIN StudentConfirmation sc ON sc.student = s " +
           "LEFT JOIN StudentIdCard sic ON sic.student = s AND sic.isActive = true")
    List<Object[]> findAllStudentsWithDetails();

    @Query(value = "SELECT s, sc, sic FROM Student s " +
                   "LEFT JOIN StudentConfirmation sc ON sc.student = s " +
                   "LEFT JOIN StudentIdCard sic ON sic.student = s AND sic.isActive = true " +
                   "WHERE (:search IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
                   "    OR LOWER(s.lastName)  LIKE LOWER(CONCAT('%',:search,'%')) " +
                   "    OR LOWER(s.regNo)     LIKE LOWER(CONCAT('%',:search,'%')) " +
                   "    OR LOWER(s.nic)       LIKE LOWER(CONCAT('%',:search,'%'))) " +
                   "AND (:courseId IS NULL OR s.course.id = :courseId) " +
                   "AND (:batchId IS NULL OR s.batch.id = :batchId) " +
                   "AND (:cardStatus IS NULL OR sic.cardStatus = :cardStatus) " +
                   "AND (:confirmationStatus IS NULL OR sc.confirmationStatus = :confirmationStatus)",
           countQuery = "SELECT COUNT(s) FROM Student s " +
                        "LEFT JOIN StudentConfirmation sc ON sc.student = s " +
                        "LEFT JOIN StudentIdCard sic ON sic.student = s AND sic.isActive = true " +
                        "WHERE (:search IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
                        "    OR LOWER(s.lastName)  LIKE LOWER(CONCAT('%',:search,'%')) " +
                        "    OR LOWER(s.regNo)     LIKE LOWER(CONCAT('%',:search,'%')) " +
                        "    OR LOWER(s.nic)       LIKE LOWER(CONCAT('%',:search,'%'))) " +
                        "AND (:courseId IS NULL OR s.course.id = :courseId) " +
                        "AND (:batchId IS NULL OR s.batch.id = :batchId) " +
                        "AND (:cardStatus IS NULL OR sic.cardStatus = :cardStatus) " +
                        "AND (:confirmationStatus IS NULL OR sc.confirmationStatus = :confirmationStatus)")
    Page<Object[]> findAllStudentsWithFilters(@Param("search") String search,
                                              @Param("courseId") Long courseId,
                                              @Param("batchId") Long batchId,
                                              @Param("cardStatus") CardStatus cardStatus,
                                              @Param("confirmationStatus") ConfirmationStatus confirmationStatus,
                                              Pageable pageable);

    @Query("SELECT s.course.name, COUNT(s) FROM Student s WHERE s.course IS NOT NULL GROUP BY s.course.name ORDER BY COUNT(s) DESC")
    List<Object[]> countByCourse();

    List<Student> findTop10ByOrderByCreateDateDesc();

//    Page<Student> findByConfirmedFalseAndConfirmationStatus(ConfirmationStatus confirmationStatus, Pageable pageable);
}
