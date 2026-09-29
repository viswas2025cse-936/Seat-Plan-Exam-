package com.example.Seat.Plan.dto;

import java.util.List;

public class AutoAllocateResponseDTO {

    private Long examSessionId;
    private String sessionName;
    private int totalStudentsToAllocate;
    private int totalAllocated;
    private int hallsUsedCount;
    private int unavoidableConflictsCount;
    private String message;
    private List<String> hallSummary;

    public AutoAllocateResponseDTO() {
    }

    public AutoAllocateResponseDTO(Long examSessionId, String sessionName, int totalStudentsToAllocate,
                                   int totalAllocated, int hallsUsedCount, int unavoidableConflictsCount,
                                   String message, List<String> hallSummary) {
        this.examSessionId = examSessionId;
        this.sessionName = sessionName;
        this.totalStudentsToAllocate = totalStudentsToAllocate;
        this.totalAllocated = totalAllocated;
        this.hallsUsedCount = hallsUsedCount;
        this.unavoidableConflictsCount = unavoidableConflictsCount;
        this.message = message;
        this.hallSummary = hallSummary;
    }

    public Long getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(Long examSessionId) {
        this.examSessionId = examSessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public int getTotalStudentsToAllocate() {
        return totalStudentsToAllocate;
    }

    public void setTotalStudentsToAllocate(int totalStudentsToAllocate) {
        this.totalStudentsToAllocate = totalStudentsToAllocate;
    }

    public int getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(int totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public int getHallsUsedCount() {
        return hallsUsedCount;
    }

    public void setHallsUsedCount(int hallsUsedCount) {
        this.hallsUsedCount = hallsUsedCount;
    }

    public int getUnavoidableConflictsCount() {
        return unavoidableConflictsCount;
    }

    public void setUnavoidableConflictsCount(int unavoidableConflictsCount) {
        this.unavoidableConflictsCount = unavoidableConflictsCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getHallSummary() {
        return hallSummary;
    }

    public void setHallSummary(List<String> hallSummary) {
        this.hallSummary = hallSummary;
    }
}
