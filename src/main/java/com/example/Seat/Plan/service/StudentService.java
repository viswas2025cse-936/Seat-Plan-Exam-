package com.example.Seat.Plan.service;

import com.example.Seat.Plan.dto.StudentBulkRegisterDTO;
import com.example.Seat.Plan.dto.StudentRequestDTO;
import com.example.Seat.Plan.dto.StudentResponseDTO;
import com.example.Seat.Plan.exception.DuplicateResourceException;
import com.example.Seat.Plan.exception.ResourceNotFoundException;
import com.example.Seat.Plan.model.Student;
import com.example.Seat.Plan.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public StudentService(StudentRepository studentRepository, NotificationService notificationService) {
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public StudentResponseDTO registerStudent(StudentRequestDTO request) {
        String rollNumber = request.getRollNumber().trim();
        if (studentRepository.existsByRollNumberIgnoreCase(rollNumber)) {
            throw new DuplicateResourceException("Student with roll number '" + rollNumber + "' already exists.");
        }

        Student student = new Student(
            rollNumber,
            request.getName().trim(),
            request.getDepartment() != null ? request.getDepartment().trim() : null,
            request.getClassName().trim(),
            request.getSection().trim()
        );

        Student saved = studentRepository.save(student);

        notificationService.logEvent(
            "STUDENT_REGISTERED",
            "Student Registered",
            "Student " + saved.getName() + " (" + saved.getRollNumber() + ") enrolled under class " +
            saved.getClassName() + "-" + saved.getSection() + ".",
            "SUCCESS"
        );

        return toResponseDTO(saved);
    }

    @Transactional
    public List<StudentResponseDTO> bulkRegisterStudents(StudentBulkRegisterDTO request) {
        List<Student> toSave = new ArrayList<>();
        Set<String> seenRollNumbers = new HashSet<>();

        for (StudentRequestDTO sReq : request.getStudents()) {
            String roll = sReq.getRollNumber().trim().toUpperCase();
            if (seenRollNumbers.contains(roll)) {
                throw new DuplicateResourceException("Duplicate roll number '" + roll + "' found within the request batch.");
            }
            if (studentRepository.existsByRollNumberIgnoreCase(roll)) {
                throw new DuplicateResourceException("Student with roll number '" + roll + "' already exists in database.");
            }
            seenRollNumbers.add(roll);

            toSave.add(new Student(
                sReq.getRollNumber().trim(),
                sReq.getName().trim(),
                sReq.getDepartment() != null ? sReq.getDepartment().trim() : null,
                sReq.getClassName().trim(),
                sReq.getSection().trim()
            ));
        }

        List<Student> savedList = studentRepository.saveAll(toSave);

        notificationService.logEvent(
            "BULK_STUDENTS_REGISTERED",
            "Bulk Students Registered",
            "Registered " + savedList.size() + " students in bulk.",
            "SUCCESS"
        );

        return savedList.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StudentResponseDTO> searchStudents(String search, Pageable pageable) {
        String term = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return studentRepository.searchStudents(term, pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return toResponseDTO(student);
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentByRollNumber(String rollNumber) {
        Student student = studentRepository.findByRollNumberIgnoreCase(rollNumber.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with Roll Number: " + rollNumber));
        return toResponseDTO(student);
    }

    @Transactional(readOnly = true)
    public Student getStudentEntityById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }

    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        String rollNumber = request.getRollNumber().trim();
        if (studentRepository.existsByRollNumberIgnoreCaseAndIdNot(rollNumber, id)) {
            throw new DuplicateResourceException("Another student with roll number '" + rollNumber + "' already exists.");
        }

        student.setRollNumber(rollNumber);
        student.setName(request.getName().trim());
        student.setDepartment(request.getDepartment() != null ? request.getDepartment().trim() : null);
        student.setClassName(request.getClassName().trim());
        student.setSection(request.getSection().trim());

        Student saved = studentRepository.save(student);
        return toResponseDTO(saved);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        studentRepository.delete(student);

        notificationService.logEvent(
            "STUDENT_DELETED",
            "Student Deleted",
            "Student " + student.getName() + " (" + student.getRollNumber() + ") removed.",
            "WARNING"
        );
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctClasses() {
        return studentRepository.findDistinctClasses();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctSections() {
        return studentRepository.findDistinctSections();
    }

    public StudentResponseDTO toResponseDTO(Student student) {
        return new StudentResponseDTO(
            student.getId(),
            student.getRollNumber(),
            student.getName(),
            student.getDepartment(),
            student.getClassName(),
            student.getSection()
        );
    }
}
