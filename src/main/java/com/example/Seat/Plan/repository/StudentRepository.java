package com.example.Seat.Plan.repository;

import com.example.Seat.Plan.model.Student;
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
    Optional<Student> findByRollNumberIgnoreCase(String rollNumber);
    boolean existsByRollNumberIgnoreCase(String rollNumber);
    boolean existsByRollNumberIgnoreCaseAndIdNot(String rollNumber, Long id);

    List<Student> findByClassNameIgnoreCase(String className);
    List<Student> findByClassNameIgnoreCaseAndSectionIgnoreCase(String className, String section);

    @Query("SELECT DISTINCT s.className FROM Student s ORDER BY s.className")
    List<String> findDistinctClasses();

    @Query("SELECT DISTINCT s.section FROM Student s ORDER BY s.section")
    List<String> findDistinctSections();

    @Query("SELECT s FROM Student s WHERE " +
           "(:search IS NULL OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.className) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.section) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Student> searchStudents(@Param("search") String search, Pageable pageable);
}
