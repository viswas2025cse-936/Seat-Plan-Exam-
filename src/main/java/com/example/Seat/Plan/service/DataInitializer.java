package com.example.Seat.Plan.service;

import com.example.Seat.Plan.dto.HallRequestDTO;
import com.example.Seat.Plan.dto.StudentBulkRegisterDTO;
import com.example.Seat.Plan.dto.StudentRequestDTO;
import com.example.Seat.Plan.model.ExamSession;
import com.example.Seat.Plan.model.Hall;
import com.example.Seat.Plan.model.SessionStatus;
import com.example.Seat.Plan.model.Student;
import com.example.Seat.Plan.repository.ExamSessionRepository;
import com.example.Seat.Plan.repository.HallRepository;
import com.example.Seat.Plan.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final HallRepository hallRepository;
    private final StudentRepository studentRepository;
    private final ExamSessionRepository examSessionRepository;
    private final HallService hallService;
    private final StudentService studentService;
    private final NotificationService notificationService;

    public DataInitializer(HallRepository hallRepository,
                           StudentRepository studentRepository,
                           ExamSessionRepository examSessionRepository,
                           HallService hallService,
                           StudentService studentService,
                           NotificationService notificationService) {
        this.hallRepository = hallRepository;
        this.studentRepository = studentRepository;
        this.examSessionRepository = examSessionRepository;
        this.hallService = hallService;
        this.studentService = studentService;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (hallRepository.count() == 0 && studentRepository.count() == 0) {
            logger.info("Database is empty. Populating sample exam data...");

            // 1. Create Sample Halls
            hallService.createHall(new HallRequestDTO("Hall A (Science Wing)", 5, 6, 28)); // 5x6 = 30 grid, 28 capacity
            hallService.createHall(new HallRequestDTO("Hall B (Main Block)", 6, 6, 32));   // 6x6 = 36 grid, 32 capacity
            hallService.createHall(new HallRequestDTO("Hall C (Tech Block)", 4, 5, 20));   // 4x5 = 20 grid, 20 capacity

            // 2. Create Sample Students across sections
            List<StudentRequestDTO> students = new ArrayList<>();

            // 15 Students from CSE Section A
            for (int i = 1; i <= 15; i++) {
                String roll = String.format("24CS%03d", i);
                students.add(new StudentRequestDTO(roll, "Student " + roll, "Computer Science", "CSE", "A"));
            }

            // 15 Students from CSE Section B
            for (int i = 16; i <= 30; i++) {
                String roll = String.format("24CS%03d", i);
                students.add(new StudentRequestDTO(roll, "Student " + roll, "Computer Science", "CSE", "B"));
            }

            // 12 Students from ECE Section A
            for (int i = 1; i <= 12; i++) {
                String roll = String.format("24EC%03d", i);
                students.add(new StudentRequestDTO(roll, "Student " + roll, "Electronics", "ECE", "A"));
            }

            // 12 Students from MECH Section A
            for (int i = 1; i <= 12; i++) {
                String roll = String.format("24ME%03d", i);
                students.add(new StudentRequestDTO(roll, "Student " + roll, "Mechanical", "MECH", "A"));
            }

            studentService.bulkRegisterStudents(new StudentBulkRegisterDTO(students));

            // 3. Create Sample Exam Session
            List<Hall> allHalls = hallRepository.findAll();
            List<Student> allStudents = studentRepository.findAll();

            ExamSession session = new ExamSession(
                "Semester Final - Data Structures & Algorithms",
                "CS201",
                LocalDate.now().plusDays(2),
                "10:00 AM",
                "01:00 PM"
            );
            session.setHalls(new HashSet<>(allHalls.subList(0, 2))); // Halls A and B
            session.setStudents(new HashSet<>(allStudents));
            session.setStatus(SessionStatus.SCHEDULED);
            examSessionRepository.save(session);

            notificationService.logEvent(
                "SYSTEM_INIT",
                "Sample Data Initialized",
                "System initialized with 3 Exam Halls, 54 Students across 4 sections, and 1 Exam Session.",
                "SUCCESS"
            );

            logger.info("Sample data initialization completed successfully.");
        }
    }
}
