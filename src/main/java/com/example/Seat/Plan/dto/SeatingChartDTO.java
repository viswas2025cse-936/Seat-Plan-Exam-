package com.example.Seat.Plan.dto;

import java.time.LocalDate;
import java.util.List;

public class SeatingChartDTO {

    private Long examSessionId;
    private String sessionName;
    private String courseCode;
    private LocalDate examDate;
    private String startTime;
    private String endTime;

    private Long hallId;
    private String hallName;
    private int totalRows;
    private int totalColumns;
    private int capacity;
    private int allocatedCount;
    private int emptySeatsCount;
    private int unavoidableConflictsCount;

    private List<List<SeatCellDTO>> grid;

    public SeatingChartDTO() {
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

    public Long getHallId() {
        return hallId;
    }

    public void setHallId(Long hallId) {
        this.hallId = hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(int totalColumns) {
        this.totalColumns = totalColumns;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAllocatedCount() {
        return allocatedCount;
    }

    public void setAllocatedCount(int allocatedCount) {
        this.allocatedCount = allocatedCount;
    }

    public int getEmptySeatsCount() {
        return emptySeatsCount;
    }

    public void setEmptySeatsCount(int emptySeatsCount) {
        this.emptySeatsCount = emptySeatsCount;
    }

    public int getUnavoidableConflictsCount() {
        return unavoidableConflictsCount;
    }

    public void setUnavoidableConflictsCount(int unavoidableConflictsCount) {
        this.unavoidableConflictsCount = unavoidableConflictsCount;
    }

    public List<List<SeatCellDTO>> getGrid() {
        return grid;
    }

    public void setGrid(List<List<SeatCellDTO>> grid) {
        this.grid = grid;
    }
}
