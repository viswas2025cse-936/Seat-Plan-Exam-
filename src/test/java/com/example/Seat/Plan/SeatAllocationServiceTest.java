package com.example.Seat.Plan;

import com.example.Seat.Plan.dto.*;
import com.example.Seat.Plan.exception.DuplicateResourceException;
import com.example.Seat.Plan.exception.InsufficientCapacityException;
import com.example.Seat.Plan.model.*;
import com.example.Seat.Plan.repository.ExamSessionRepository;
import com.example.Seat.Plan.repository.HallRepository;
import com.example.Seat.Plan.repository.StudentRepository;
import com.example.Seat.Plan.service.ExamSessionService;
import com.example.Seat.Plan.service.HallService;
import com.example.Seat.Plan.service.SeatAllocationService;
import com.example.Seat.Plan.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SeatAllocationServiceTest {

    @Autowired
    private HallService hallService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ExamSessionService examSessionService;

    @Autowired
    private SeatAllocationService seatAllocationService;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Test
    @DisplayName("Rule 1: Enforce hall declared capacity - reject when students exceed capacity")
    void testCapacityExceededRule() {
        // Register small hall with capacity 4 (2x2 grid)
        HallResponseDTO hall = hallService.createHall(new HallRequestDTO("Test Hall Small", 2, 2, 4));

        // Create 6 students (more than capacity 4)
        List<StudentRequestDTO> students = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            students.add(new StudentRequestDTO("TEST-CAP-" + i, "Cap Student " + i, "CS", "CSE", "A"));
        }
        List<StudentResponseDTO> savedStudents = studentService.bulkRegisterStudents(new StudentBulkRegisterDTO(students));

        List<Long> studentIds = savedStudents.stream().map(StudentResponseDTO::getId).toList();

        // Create Exam Session
        ExamSessionResponseDTO session = examSessionService.createSession(
            new ExamSessionRequestDTO("Cap Test Session", "T101", LocalDate.now(), "10:00", "12:00",
                List.of(hall.getId()), studentIds)
        );

        // Auto-allocate must throw InsufficientCapacityException
        InsufficientCapacityException ex = assertThrows(
            InsufficientCapacityException.class,
            () -> seatAllocationService.autoAllocateSeats(new AutoAllocateRequestDTO(session.getId()))
        );

        assertTrue(ex.getMessage().contains("exceeds the combined declared capacity"));
    }

    @Test
    @DisplayName("Rule 2: Avoid placing adjacent seats for same section students when alternatives exist")
    void testAdjacentSeatingRule() {
        // Register hall with 4 rows x 4 columns = 16 capacity
        HallResponseDTO hall = hallService.createHall(new HallRequestDTO("Test Hall Grid", 4, 4, 16));

        // Create 8 students from Section A and 8 students from Section B (equal split)
        List<StudentRequestDTO> students = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            students.add(new StudentRequestDTO("TEST-A-" + i, "Student A " + i, "CS", "CSE", "A"));
        }
        for (int i = 1; i <= 8; i++) {
            students.add(new StudentRequestDTO("TEST-B-" + i, "Student B " + i, "IT", "IT", "B"));
        }
        List<StudentResponseDTO> savedStudents = studentService.bulkRegisterStudents(new StudentBulkRegisterDTO(students));
        List<Long> studentIds = savedStudents.stream().map(StudentResponseDTO::getId).toList();

        // Create Exam Session
        ExamSessionResponseDTO session = examSessionService.createSession(
            new ExamSessionRequestDTO("Adjacent Test Session", "T202", LocalDate.now(), "09:00", "11:00",
                List.of(hall.getId()), studentIds)
        );

        // Auto-allocate
        AutoAllocateResponseDTO response = seatAllocationService.autoAllocateSeats(
            new AutoAllocateRequestDTO(session.getId())
        );

        assertEquals(16, response.getTotalAllocated());
        // Alternatives existed throughout, so conflicts must be 0
        assertEquals(0, response.getUnavoidableConflictsCount(), "Adjacent conflicts should be 0 when alternatives exist!");

        // Retrieve and inspect seating chart
        SeatingChartDTO chart = seatAllocationService.getSeatingChart(session.getId(), hall.getId());
        assertNotNull(chart);
        assertEquals(4, chart.getTotalRows());
        assertEquals(4, chart.getTotalColumns());
        assertEquals(16, chart.getAllocatedCount());
        assertEquals(0, chart.getUnavoidableConflictsCount());

        List<List<SeatCellDTO>> grid = chart.getGrid();

        // Verify that horizontally and vertically adjacent seats have different section keys
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                SeatCellDTO cell = grid.get(r).get(c);
                assertTrue(cell.isOccupied());
                assertFalse(cell.isHasAdjacentConflict(), "Seat " + cell.getSeatCode() + " flagged with conflict!");

                // Check right neighbor
                if (c + 1 < 4) {
                    SeatCellDTO right = grid.get(r).get(c + 1);
                    assertNotEquals(cell.getSectionKey(), right.getSectionKey(),
                        "Horizontal adjacency violation between " + cell.getSeatCode() + " and " + right.getSeatCode());
                }

                // Check bottom neighbor
                if (r + 1 < 4) {
                    SeatCellDTO bottom = grid.get(r + 1).get(c);
                    assertNotEquals(cell.getSectionKey(), bottom.getSectionKey(),
                        "Vertical adjacency violation between " + cell.getSeatCode() + " and " + bottom.getSeatCode());
                }
            }
        }
    }

    @Test
    @DisplayName("Rule 3: Student seat lookup retrieves correct hall and seat coordinates")
    void testStudentSeatLookup() {
        HallResponseDTO hall = hallService.createHall(new HallRequestDTO("Lookup Hall", 3, 3, 9));

        List<StudentRequestDTO> students = List.of(
            new StudentRequestDTO("LOOKUP-001", "Alice Wonder", "CSE", "CSE", "A"),
            new StudentRequestDTO("LOOKUP-002", "Bob Builder", "MECH", "MECH", "B")
        );
        List<StudentResponseDTO> savedStudents = studentService.bulkRegisterStudents(new StudentBulkRegisterDTO(students));
        List<Long> studentIds = savedStudents.stream().map(StudentResponseDTO::getId).toList();

        ExamSessionResponseDTO session = examSessionService.createSession(
            new ExamSessionRequestDTO("Lookup Session", "LK101", LocalDate.now(), "14:00", "17:00",
                List.of(hall.getId()), studentIds)
        );

        seatAllocationService.autoAllocateSeats(new AutoAllocateRequestDTO(session.getId()));

        // Lookup Alice
        StudentLookupDTO lookup = seatAllocationService.lookupStudentByRollNumber("LOOKUP-001");
        assertNotNull(lookup);
        assertEquals("Alice Wonder", lookup.getStudentName());
        assertEquals("LOOKUP-001", lookup.getRollNumber());
        assertFalse(lookup.getAllocations().isEmpty());

        StudentAllocationItemDTO alloc = lookup.getAllocations().get(0);
        assertEquals(hall.getName(), alloc.getHallName());
        assertNotNull(alloc.getSeatCode());
        assertTrue(alloc.getRowNumber() >= 1);
        assertTrue(alloc.getColNumber() >= 1);
    }

    @Test
    @DisplayName("Rule 4: Reject duplicate student registration with clean exception")
    void testDuplicateStudentRollNumber() {
        StudentRequestDTO student = new StudentRequestDTO("DUP-001", "David Clark", "ECE", "ECE", "A");
        studentService.registerStudent(student);

        // Attempting to register again with same roll number
        assertThrows(DuplicateResourceException.class, () -> studentService.registerStudent(student));
    }
}
