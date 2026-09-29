package com.example.Seat.Plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class StudentBulkRegisterDTO {

    @NotEmpty(message = "Students list cannot be empty")
    @Valid
    private List<StudentRequestDTO> students;

    public StudentBulkRegisterDTO() {
    }

    public StudentBulkRegisterDTO(List<StudentRequestDTO> students) {
        this.students = students;
    }

    public List<StudentRequestDTO> getStudents() {
        return students;
    }

    public void setStudents(List<StudentRequestDTO> students) {
        this.students = students;
    }
}
