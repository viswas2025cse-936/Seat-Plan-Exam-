package com.example.Seat.Plan.repository;

import com.example.Seat.Plan.model.ExamSession;
import com.example.Seat.Plan.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByStatus(SessionStatus status);
}
