package com.example.Seat.Plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class ExamSessionRequestDTO {

    @NotBlank(message = "Session name is required")
    private String sessionName;

    private String courseCode;

    @NotNull(message = "Exam date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate;

    private String startTime;
    private String endTime;

    private List<Long> hallIds;
    private List<Long> studentIds;

    public ExamSessionRequestDTO() {
    }

    public ExamSessionRequestDTO(String sessionName, String courseCode, LocalDate examDate, String startTime, String endTime, List<Long> hallIds, List<Long> studentIds) {
        this.sessionName = sessionName;
        this.courseCode = courseCode;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hallIds = hallIds;
        this.studentIds = studentIds;
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

    public List<Long> getHallIds() {
        return hallIds;
    }

    public void setHallIds(List<Long> hallIds) {
        this.hallIds = hallIds;
    }

    public List<Long> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Long> studentIds) {
        this.studentIds = studentIds;
    }
}
