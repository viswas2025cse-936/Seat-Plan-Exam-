package com.example.Seat.Plan.dto;

import com.example.Seat.Plan.model.SessionStatus;

import java.time.LocalDate;
import java.util.List;

public class ExamSessionResponseDTO {

    private Long id;
    private String sessionName;
    private String courseCode;
    private LocalDate examDate;
    private String startTime;
    private String endTime;
    private SessionStatus status;
    private int hallsCount;
    private int studentsCount;
    private int totalDeclaredCapacity;
    private long allocatedCount;
    private List<HallResponseDTO> halls;

    public ExamSessionResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public int getHallsCount() {
        return hallsCount;
    }

    public void setHallsCount(int hallsCount) {
        this.hallsCount = hallsCount;
    }

    public int getStudentsCount() {
        return studentsCount;
    }

    public void setStudentsCount(int studentsCount) {
        this.studentsCount = studentsCount;
    }

    public int getTotalDeclaredCapacity() {
        return totalDeclaredCapacity;
    }

    public void setTotalDeclaredCapacity(int totalDeclaredCapacity) {
        this.totalDeclaredCapacity = totalDeclaredCapacity;
    }

    public long getAllocatedCount() {
        return allocatedCount;
    }

    public void setAllocatedCount(long allocatedCount) {
        this.allocatedCount = allocatedCount;
    }

    public List<HallResponseDTO> getHalls() {
        return halls;
    }

    public void setHalls(List<HallResponseDTO> halls) {
        this.halls = halls;
    }
}
