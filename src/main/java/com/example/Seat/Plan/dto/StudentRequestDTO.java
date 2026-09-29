package com.example.Seat.Plan.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentRequestDTO {

    @NotBlank(message = "Roll number is required")
    private String rollNumber;

    @NotBlank(message = "Student name is required")
    private String name;

    private String department;

    @NotBlank(message = "Class name is required")
    private String className;

    @NotBlank(message = "Section is required")
    private String section;

    public StudentRequestDTO() {
    }

    public StudentRequestDTO(String rollNumber, String name, String department, String className, String section) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
        this.className = className;
        this.section = section;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
