package com.example.Seat.Plan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_allocations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_session_seat", columnNames = {"session_id", "seat_id"}),
    @UniqueConstraint(name = "uk_session_student", columnNames = {"session_id", "student_id"})
}, indexes = {
    @Index(name = "idx_alloc_session_hall", columnList = "session_id, hall_id"),
    @Index(name = "idx_alloc_student", columnList = "student_id")
})
public class SeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDateTime allocationTime = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean hasConflict = false;

    public SeatAllocation() {
    }

    public SeatAllocation(ExamSession examSession, Hall hall, Seat seat, Student student) {
        this.examSession = examSession;
        this.hall = hall;
        this.seat = seat;
        this.student = student;
        this.allocationTime = LocalDateTime.now();
        this.hasConflict = false;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    public void setExamSession(ExamSession examSession) {
        this.examSession = examSession;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDateTime getAllocationTime() {
        return allocationTime;
    }

    public void setAllocationTime(LocalDateTime allocationTime) {
        this.allocationTime = allocationTime;
    }

    public Boolean getHasConflict() {
        return hasConflict;
    }

    public void setHasConflict(Boolean hasConflict) {
        this.hasConflict = hasConflict;
    }
}
