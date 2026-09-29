package com.example.Seat.Plan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_roll", columnList = "rollNumber", unique = true),
    @Index(name = "idx_student_class_sec", columnList = "className, section")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Roll number is required")
    @Column(nullable = false, unique = true)
    private String rollNumber;

    @NotBlank(message = "Student name is required")
    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String department;

    @NotBlank(message = "Class name is required")
    @Column(nullable = false)
    private String className;

    @NotBlank(message = "Section is required")
    @Column(nullable = false)
    private String section;

    public Student() {
    }

    public Student(String rollNumber, String name, String department, String className, String section) {
        this.rollNumber = rollNumber != null ? rollNumber.trim() : null;
        this.name = name != null ? name.trim() : null;
        this.department = department != null ? department.trim() : null;
        this.className = className != null ? className.trim() : null;
        this.section = section != null ? section.trim() : null;
    }

    public String getSectionKey() {
        String c = className != null ? className.trim().toUpperCase() : "";
        String s = section != null ? section.trim().toUpperCase() : "";
        return c + "-" + s;
    }

    // Getters and Setters

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
        this.rollNumber = rollNumber != null ? rollNumber.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : null;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className != null ? className.trim() : null;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section != null ? section.trim() : null;
    }
}
