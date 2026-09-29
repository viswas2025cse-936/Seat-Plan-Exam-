package com.example.Seat.Plan.repository;

import com.example.Seat.Plan.model.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {

    List<SeatAllocation> findByExamSessionId(Long examSessionId);

    List<SeatAllocation> findByExamSessionIdAndHallId(Long examSessionId, Long hallId);

    Optional<SeatAllocation> findByExamSessionIdAndSeatId(Long examSessionId, Long seatId);

    Optional<SeatAllocation> findByExamSessionIdAndStudentId(Long examSessionId, Long studentId);

    @Query("SELECT sa FROM SeatAllocation sa " +
           "JOIN FETCH sa.examSession es " +
           "JOIN FETCH sa.hall h " +
           "JOIN FETCH sa.seat s " +
           "JOIN FETCH sa.student st " +
           "WHERE LOWER(st.rollNumber) = LOWER(:rollNumber)")
    List<SeatAllocation> findAllocationsByStudentRollNumber(@Param("rollNumber") String rollNumber);

    long countByExamSessionIdAndHallId(Long examSessionId, Long hallId);

    long countByExamSessionId(Long examSessionId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SeatAllocation sa WHERE sa.examSession.id = :examSessionId")
    void deleteByExamSessionId(@Param("examSessionId") Long examSessionId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SeatAllocation sa WHERE sa.examSession.id = :examSessionId AND sa.hall.id = :hallId")
    void deleteByExamSessionIdAndHallId(@Param("examSessionId") Long examSessionId, @Param("hallId") Long hallId);
}
