package com.example.Seat.Plan.dto;

import java.util.List;

public class StudentLookupDTO {

    private Long studentId;
    private String rollNumber;
    private String studentName;
    private String department;
    private String className;
    private String section;
    private List<StudentAllocationItemDTO> allocations;

    public StudentLookupDTO() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public List<StudentAllocationItemDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<StudentAllocationItemDTO> allocations) {
        this.allocations = allocations;
    }
}
