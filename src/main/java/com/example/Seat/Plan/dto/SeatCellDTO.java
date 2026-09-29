package com.example.Seat.Plan.dto;

public class SeatCellDTO {

    private int rowNumber;
    private int colNumber;
    private String seatCode;
    private boolean isOccupied;
    private Long studentId;
    private String studentRollNumber;
    private String studentName;
    private String className;
    private String section;
    private String department;
    private String sectionKey;
    private boolean hasAdjacentConflict;

    public SeatCellDTO() {
    }

    public SeatCellDTO(int rowNumber, int colNumber, String seatCode) {
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.seatCode = seatCode;
        this.isOccupied = false;
        this.hasAdjacentConflict = false;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColNumber() {
        return colNumber;
    }

    public void setColNumber(int colNumber) {
        this.colNumber = colNumber;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSectionKey() {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public boolean isHasAdjacentConflict() {
        return hasAdjacentConflict;
    }

    public void setHasAdjacentConflict(boolean hasAdjacentConflict) {
        this.hasAdjacentConflict = hasAdjacentConflict;
    }
}
