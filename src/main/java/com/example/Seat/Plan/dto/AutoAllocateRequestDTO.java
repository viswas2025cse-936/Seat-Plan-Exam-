package com.example.Seat.Plan.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AutoAllocateRequestDTO {

    @NotNull(message = "Exam session ID is required")
    private Long examSessionId;

    // Optional override of halls to use (if null or empty, uses halls already attached to session)
    private List<Long> hallIds;

    // Optional override of students to allocate (if null or empty, uses students already attached to session)
    private List<Long> studentIds;

    public AutoAllocateRequestDTO() {
    }

    public AutoAllocateRequestDTO(Long examSessionId) {
        this.examSessionId = examSessionId;
    }

    public AutoAllocateRequestDTO(Long examSessionId, List<Long> hallIds, List<Long> studentIds) {
        this.examSessionId = examSessionId;
        this.hallIds = hallIds;
        this.studentIds = studentIds;
    }

    public Long getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(Long examSessionId) {
        this.examSessionId = examSessionId;
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
