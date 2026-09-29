package com.example.Seat.Plan.dto;

public class StudentResponseDTO {

    private Long id;
    private String rollNumber;
    private String name;
    private String department;
    private String className;
    private String section;
    private String sectionKey;

    public StudentResponseDTO() {
    }

    public StudentResponseDTO(Long id, String rollNumber, String name, String department, String className, String section) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
        this.className = className;
        this.section = section;
        this.sectionKey = (className != null ? className.trim().toUpperCase() : "") + "-" +
                          (section != null ? section.trim().toUpperCase() : "");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSectionKey() {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }
}
