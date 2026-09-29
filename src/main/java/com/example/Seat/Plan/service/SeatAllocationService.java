package com.example.Seat.Plan.service;

import com.example.Seat.Plan.dto.*;
import com.example.Seat.Plan.exception.InsufficientCapacityException;
import com.example.Seat.Plan.exception.ResourceNotFoundException;
import com.example.Seat.Plan.exception.SeatAllocationException;
import com.example.Seat.Plan.model.*;
import com.example.Seat.Plan.repository.ExamSessionRepository;
import com.example.Seat.Plan.repository.HallRepository;
import com.example.Seat.Plan.repository.SeatAllocationRepository;
import com.example.Seat.Plan.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatAllocationService {

    private static final Logger logger = LoggerFactory.getLogger(SeatAllocationService.class);

    private final SeatAllocationRepository seatAllocationRepository;
    private final ExamSessionRepository examSessionRepository;
    private final HallRepository hallRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public SeatAllocationService(SeatAllocationRepository seatAllocationRepository,
                                 ExamSessionRepository examSessionRepository,
                                 HallRepository hallRepository,
                                 StudentRepository studentRepository,
                                 NotificationService notificationService) {
        this.seatAllocationRepository = seatAllocationRepository;
        this.examSessionRepository = examSessionRepository;
        this.hallRepository = hallRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    /**
     * Auto-allocate seats for an ExamSession across assigned halls ensuring:
     * 1. No student is allocated beyond hall's declared capacity.
     * 2. Adjacent seats differ in class/section wherever alternatives exist.
     * 3. Clear error messages when rules cannot be met.
     */
    @Transactional
    public AutoAllocateResponseDTO autoAllocateSeats(AutoAllocateRequestDTO request) {
        Long sessionId = request.getExamSessionId();
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + sessionId));

        // 1. Determine participating halls
        List<Hall> halls;
        if (request.getHallIds() != null && !request.getHallIds().isEmpty()) {
            halls = hallRepository.findAllById(request.getHallIds());
            if (halls.size() != request.getHallIds().size()) {
                throw new ResourceNotFoundException("One or more specified halls were not found.");
            }
        } else if (session.getHalls() != null && !session.getHalls().isEmpty()) {
            halls = new ArrayList<>(session.getHalls());
        } else {
            throw new SeatAllocationException("No exam halls assigned to this session. Please assign at least one hall.");
        }

        // Sort halls by name for deterministic order
        halls.sort(Comparator.comparing(Hall::getName));

        // 2. Determine participating students
        List<Student> students;
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            students = studentRepository.findAllById(request.getStudentIds());
            if (students.size() != request.getStudentIds().size()) {
                throw new ResourceNotFoundException("One or more specified students were not found.");
            }
        } else if (session.getStudents() != null && !session.getStudents().isEmpty()) {
            students = new ArrayList<>(session.getStudents());
        } else {
            // Default to all registered students if session doesn't explicitly restrict
            students = studentRepository.findAll();
            if (students.isEmpty()) {
                throw new SeatAllocationException("No students found to allocate. Please register students first.");
            }
        }

        int totalStudents = students.size();
        if (totalStudents == 0) {
            throw new SeatAllocationException("Cannot run allocation: student list is empty.");
        }

        // 3. Business Rule Check: Total Capacity
        int totalDeclaredCapacity = halls.stream()
            .mapToInt(h -> (h.getCapacity() != null ? h.getCapacity() : 0))
            .sum();

        if (totalStudents > totalDeclaredCapacity) {
            throw new InsufficientCapacityException(
                String.format("Total students to allocate (%d) exceeds the combined declared capacity of selected halls (%d). Please allocate more halls or reduce student count.",
                    totalStudents, totalDeclaredCapacity)
            );
        }

        // Remove any previous allocations for this session to allow clean reallocation
        seatAllocationRepository.deleteByExamSessionId(sessionId);

        // 4. Distribute students across halls to balance sections evenly
        // Group students by sectionKey (e.g. "B.TECH CSE-A")
        Map<String, List<Student>> studentsBySection = new HashMap<>();
        for (Student s : students) {
            studentsBySection.computeIfAbsent(s.getSectionKey(), k -> new ArrayList<>()).add(s);
        }

        // Prepare per-hall student buckets
        Map<Long, List<Student>> hallStudentBuckets = new LinkedHashMap<>();
        for (Hall hall : halls) {
            hallStudentBuckets.put(hall.getId(), new ArrayList<>());
        }

        // Distribute students section by section across halls (round-robin) respecting each hall's declared capacity
        // This guarantees each hall receives an optimal multi-section mix rather than all of one section
        List<String> sectionKeys = new ArrayList<>(studentsBySection.keySet());
        // Sort sections descending by size to place larger sections evenly
        sectionKeys.sort((a, b) -> Integer.compare(studentsBySection.get(b).size(), studentsBySection.get(a).size()));

        for (String secKey : sectionKeys) {
            List<Student> secStudents = new ArrayList<>(studentsBySection.get(secKey));
            int hallIndex = 0;
            for (Student student : secStudents) {
                // Find a hall with remaining capacity
                int attempts = 0;
                boolean placed = false;
                while (attempts < halls.size()) {
                    Hall targetHall = halls.get(hallIndex);
                    List<Student> currentInHall = hallStudentBuckets.get(targetHall.getId());
                    if (currentInHall.size() < targetHall.getCapacity()) {
                        currentInHall.add(student);
                        placed = true;
                        hallIndex = (hallIndex + 1) % halls.size();
                        break;
                    }
                    hallIndex = (hallIndex + 1) % halls.size();
                    attempts++;
                }
                if (!placed) {
                    throw new InsufficientCapacityException("Failed to distribute student " + student.getRollNumber() + " within hall capacities.");
                }
            }
        }

        // 5. Allocate seats in each hall using intelligent constraint satisfaction
        List<SeatAllocation> allAllocationsToSave = new ArrayList<>();
        List<String> hallSummaryLogs = new ArrayList<>();
        int totalConflicts = 0;

        for (Hall hall : halls) {
            List<Student> hallStudents = hallStudentBuckets.get(hall.getId());
            if (hallStudents.isEmpty()) {
                hallSummaryLogs.add(hall.getName() + ": 0 students allocated (Hall empty).");
                continue;
            }

            // Ensure hall has generated seats
            List<Seat> seats = hall.getSeats();
            if (seats == null || seats.isEmpty()) {
                hall.generateSeats();
                hallRepository.save(hall);
                seats = hall.getSeats();
            }

            // Hall-level capacity check (enforced before saving)
            if (hallStudents.size() > hall.getCapacity()) {
                throw new InsufficientCapacityException(
                    "Allocating " + hallStudents.size() + " students to hall '" + hall.getName() +
                    "' exceeds declared capacity of " + hall.getCapacity() + "."
                );
            }

            // Run non-adjacent seat allocation algorithm for this hall
            HallAllocationResult hallResult = allocateHallSeats(session, hall, seats, hallStudents);
            allAllocationsToSave.addAll(hallResult.allocations);
            totalConflicts += hallResult.unavoidableConflicts;

            hallSummaryLogs.add(String.format("%s: %d/%d seats allocated (%d unavoidable adjacent section conflicts).",
                hall.getName(), hallResult.allocations.size(), hall.getCapacity(), hallResult.unavoidableConflicts));
        }

        // 6. Save all allocations
        seatAllocationRepository.saveAll(allAllocationsToSave);

        // Update session status to ALLOCATED
        session.setStatus(SessionStatus.ALLOCATED);
        session.setHalls(new HashSet<>(halls));
        session.setStudents(new HashSet<>(students));
        examSessionRepository.save(session);

        String message = String.format("Auto-allocation completed successfully for session '%s'. %d students allocated across %d hall(s) with %d unavoidable conflict(s).",
            session.getSessionName(), allAllocationsToSave.size(), halls.size(), totalConflicts);

        notificationService.logEvent(
            "SEATING_ALLOCATED",
            "Seating Auto-Allocation Complete",
            message,
            totalConflicts > 0 ? "WARNING" : "SUCCESS"
        );

        return new AutoAllocateResponseDTO(
            session.getId(),
            session.getSessionName(),
            totalStudents,
            allAllocationsToSave.size(),
            halls.size(),
            totalConflicts,
            message,
            hallSummaryLogs
        );
    }

    private static class HallAllocationResult {
        List<SeatAllocation> allocations;
        int unavoidableConflicts;

        HallAllocationResult(List<SeatAllocation> allocations, int unavoidableConflicts) {
            this.allocations = allocations;
            this.unavoidableConflicts = unavoidableConflicts;
        }
    }

    /**
     * Allocates seats within a hall avoiding placing two students from the same section
     * in adjacent seats (horizontal and vertical neighbors) whenever alternatives exist.
     */
    private HallAllocationResult allocateHallSeats(ExamSession session, Hall hall, List<Seat> seats, List<Student> students) {
        int totalRows = hall.getTotalRows();
        int totalCols = hall.getTotalColumns();
        int capacity = hall.getCapacity();

        // Map physical seats by coordinate (row, col)
        Map<String, Seat> seatMap = new HashMap<>();
        for (Seat s : seats) {
            seatMap.put(s.getRowNum() + "-" + s.getColNum(), s);
        }

        // Group hall students by sectionKey
        Map<String, List<Student>> sectionPool = new HashMap<>();
        for (Student s : students) {
            sectionPool.computeIfAbsent(s.getSectionKey(), k -> new ArrayList<>()).add(s);
        }

        // Grid to track assigned student at (row, col)
        Student[][] grid = new Student[totalRows + 1][totalCols + 1];
        List<SeatAllocation> allocations = new ArrayList<>();
        int unavoidableConflicts = 0;

        int seatsToFill = Math.min(students.size(), capacity);
        int filledCount = 0;

        // Traverse seats row-by-row
        rowLoop:
        for (int r = 1; r <= totalRows; r++) {
            for (int c = 1; c <= totalCols; c++) {
                if (filledCount >= seatsToFill) {
                    break rowLoop;
                }

                Seat seat = seatMap.get(r + "-" + c);
                if (seat == null || !seat.getIsActive()) {
                    continue;
                }

                // Identify sections of adjacent seats (left and top already assigned)
                Set<String> adjacentForbiddenSections = new HashSet<>();
                Set<String> diagonalForbiddenSections = new HashSet<>();

                // Left neighbor
                if (c > 1 && grid[r][c - 1] != null) {
                    adjacentForbiddenSections.add(grid[r][c - 1].getSectionKey());
                }
                // Top neighbor
                if (r > 1 && grid[r - 1][c] != null) {
                    adjacentForbiddenSections.add(grid[r - 1][c].getSectionKey());
                }
                // Diagonal top-left
                if (r > 1 && c > 1 && grid[r - 1][c - 1] != null) {
                    diagonalForbiddenSections.add(grid[r - 1][c - 1].getSectionKey());
                }
                // Diagonal top-right
                if (r > 1 && c < totalCols && grid[r - 1][c + 1] != null) {
                    diagonalForbiddenSections.add(grid[r - 1][c + 1].getSectionKey());
                }

                // Candidate selection:
                // Tier 1: Sections not in adjacent forbidden AND not in diagonal forbidden
                // Tier 2: Sections not in adjacent forbidden
                // Tier 3: Any section with remaining students (no alternative exists!)
                List<String> tier1Candidates = new ArrayList<>();
                List<String> tier2Candidates = new ArrayList<>();
                List<String> allAvailableSections = new ArrayList<>();

                for (Map.Entry<String, List<Student>> entry : sectionPool.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        String sec = entry.getKey();
                        allAvailableSections.add(sec);
                        boolean isAdjacentForbidden = adjacentForbiddenSections.contains(sec);
                        boolean isDiagonalForbidden = diagonalForbiddenSections.contains(sec);

                        if (!isAdjacentForbidden && !isDiagonalForbidden) {
                            tier1Candidates.add(sec);
                        } else if (!isAdjacentForbidden) {
                            tier2Candidates.add(sec);
                        }
                    }
                }

                String chosenSection = null;
                boolean conflict = false;

                if (!tier1Candidates.isEmpty()) {
                    // Pick candidate from Tier 1 with largest remaining students (Most Constrained First)
                    tier1Candidates.sort((a, b) -> Integer.compare(sectionPool.get(b).size(), sectionPool.get(a).size()));
                    chosenSection = tier1Candidates.get(0);
                } else if (!tier2Candidates.isEmpty()) {
                    // Pick candidate from Tier 2 with largest remaining students
                    tier2Candidates.sort((a, b) -> Integer.compare(sectionPool.get(b).size(), sectionPool.get(a).size()));
                    chosenSection = tier2Candidates.get(0);
                } else if (!allAvailableSections.isEmpty()) {
                    // No alternative exists! All remaining students belong to sections present in adjacent seats
                    allAvailableSections.sort((a, b) -> Integer.compare(sectionPool.get(b).size(), sectionPool.get(a).size()));
                    chosenSection = allAvailableSections.get(0);
                    conflict = true;
                    unavoidableConflicts++;
                    logger.warn("Unavoidable adjacent section placement at Seat {}-{} in Hall '{}' for section '{}'. No alternatives available.",
                        r, c, hall.getName(), chosenSection);
                } else {
                    break rowLoop; // No more students left
                }

                List<Student> pool = sectionPool.get(chosenSection);
                Student chosenStudent = pool.remove(0);

                grid[r][c] = chosenStudent;
                filledCount++;

                SeatAllocation allocation = new SeatAllocation(session, hall, seat, chosenStudent);
                allocation.setHasConflict(conflict);
                allocations.add(allocation);
            }
        }

        return new HallAllocationResult(allocations, unavoidableConflicts);
    }

    /**
     * Generate printable seating chart for a specific hall in an exam session.
     */
    @Transactional(readOnly = true)
    public SeatingChartDTO getSeatingChart(Long sessionId, Long hallId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + sessionId));

        Hall hall = hallRepository.findById(hallId)
            .orElseThrow(() -> new ResourceNotFoundException("Hall not found with ID: " + hallId));

        List<SeatAllocation> allocations = seatAllocationRepository.findByExamSessionIdAndHallId(sessionId, hallId);
        List<Seat> seats = hall.getSeats();
        if (seats == null || seats.isEmpty()) {
            seats = hallRepository.findById(hallId).map(Hall::getSeats).orElse(Collections.emptyList());
        }

        // Map allocations by seatId
        Map<Long, SeatAllocation> allocMap = new HashMap<>();
        for (SeatAllocation sa : allocations) {
            allocMap.put(sa.getSeat().getId(), sa);
        }

        int totalRows = hall.getTotalRows();
        int totalCols = hall.getTotalColumns();

        // 2D grid
        SeatCellDTO[][] cellGrid = new SeatCellDTO[totalRows + 1][totalCols + 1];

        // Populate base seats
        for (Seat seat : seats) {
            int r = seat.getRowNum();
            int c = seat.getColNum();
            SeatCellDTO cell = new SeatCellDTO(r, c, seat.getSeatCode());

            SeatAllocation sa = allocMap.get(seat.getId());
            if (sa != null) {
                cell.setOccupied(true);
                Student st = sa.getStudent();
                cell.setStudentId(st.getId());
                cell.setStudentRollNumber(st.getRollNumber());
                cell.setStudentName(st.getName());
                cell.setClassName(st.getClassName());
                cell.setSection(st.getSection());
                cell.setDepartment(st.getDepartment());
                cell.setSectionKey(st.getSectionKey());
                cell.setHasAdjacentConflict(Boolean.TRUE.equals(sa.getHasConflict()));
            }

            cellGrid[r][c] = cell;
        }

        // Double check adjacent conflicts across all four directions (left, right, top, bottom)
        int conflictCount = 0;
        for (int r = 1; r <= totalRows; r++) {
            for (int c = 1; c <= totalCols; c++) {
                SeatCellDTO current = cellGrid[r][c];
                if (current != null && current.isOccupied() && current.getSectionKey() != null) {
                    boolean conflict = false;
                    // Check right
                    if (c < totalCols && cellGrid[r][c + 1] != null && cellGrid[r][c + 1].isOccupied()) {
                        if (current.getSectionKey().equalsIgnoreCase(cellGrid[r][c + 1].getSectionKey())) {
                            conflict = true;
                        }
                    }
                    // Check left
                    if (c > 1 && cellGrid[r][c - 1] != null && cellGrid[r][c - 1].isOccupied()) {
                        if (current.getSectionKey().equalsIgnoreCase(cellGrid[r][c - 1].getSectionKey())) {
                            conflict = true;
                        }
                    }
                    // Check bottom
                    if (r < totalRows && cellGrid[r + 1][c] != null && cellGrid[r + 1][c].isOccupied()) {
                        if (current.getSectionKey().equalsIgnoreCase(cellGrid[r + 1][c].getSectionKey())) {
                            conflict = true;
                        }
                    }
                    // Check top
                    if (r > 1 && cellGrid[r - 1][c] != null && cellGrid[r - 1][c].isOccupied()) {
                        if (current.getSectionKey().equalsIgnoreCase(cellGrid[r - 1][c].getSectionKey())) {
                            conflict = true;
                        }
                    }

                    if (conflict) {
                        current.setHasAdjacentConflict(true);
                        conflictCount++;
                    }
                }
            }
        }

        // Build 2D list
        List<List<SeatCellDTO>> gridList = new ArrayList<>();
        for (int r = 1; r <= totalRows; r++) {
            List<SeatCellDTO> rowList = new ArrayList<>();
            for (int c = 1; c <= totalCols; c++) {
                SeatCellDTO cell = cellGrid[r][c];
                if (cell == null) {
                    cell = new SeatCellDTO(r, c, "R" + r + "C" + c);
                }
                rowList.add(cell);
            }
            gridList.add(rowList);
        }

        SeatingChartDTO chart = new SeatingChartDTO();
        chart.setExamSessionId(session.getId());
        chart.setSessionName(session.getSessionName());
        chart.setCourseCode(session.getCourseCode());
        chart.setExamDate(session.getExamDate());
        chart.setStartTime(session.getStartTime());
        chart.setEndTime(session.getEndTime());

        chart.setHallId(hall.getId());
        chart.setHallName(hall.getName());
        chart.setTotalRows(totalRows);
        chart.setTotalColumns(totalCols);
        chart.setCapacity(hall.getCapacity());
        chart.setAllocatedCount(allocations.size());
        chart.setEmptySeatsCount((totalRows * totalCols) - allocations.size());
        chart.setUnavoidableConflictsCount(conflictCount);
        chart.setGrid(gridList);

        return chart;
    }

    /**
     * Look up student's allocated hall and seat number by roll number.
     */
    @Transactional(readOnly = true)
    public StudentLookupDTO lookupStudentByRollNumber(String rollNumber) {
        String cleanRoll = rollNumber.trim();
        Student student = studentRepository.findByRollNumberIgnoreCase(cleanRoll)
            .orElseThrow(() -> new ResourceNotFoundException("No student found with roll number: " + cleanRoll));

        List<SeatAllocation> allocations = seatAllocationRepository.findAllocationsByStudentRollNumber(cleanRoll);

        StudentLookupDTO lookup = new StudentLookupDTO();
        lookup.setStudentId(student.getId());
        lookup.setRollNumber(student.getRollNumber());
        lookup.setStudentName(student.getName());
        lookup.setDepartment(student.getDepartment());
        lookup.setClassName(student.getClassName());
        lookup.setSection(student.getSection());

        List<StudentAllocationItemDTO> items = allocations.stream().map(a -> {
            StudentAllocationItemDTO item = new StudentAllocationItemDTO();
            item.setAllocationId(a.getId());
            item.setExamSessionId(a.getExamSession().getId());
            item.setSessionName(a.getExamSession().getSessionName());
            item.setCourseCode(a.getExamSession().getCourseCode());
            item.setExamDate(a.getExamSession().getExamDate());
            item.setStartTime(a.getExamSession().getStartTime());
            item.setEndTime(a.getExamSession().getEndTime());

            item.setHallId(a.getHall().getId());
            item.setHallName(a.getHall().getName());

            item.setSeatId(a.getSeat().getId());
            item.setSeatCode(a.getSeat().getSeatCode());
            item.setRowNumber(a.getSeat().getRowNum());
            item.setColNumber(a.getSeat().getColNum());
            return item;
        }).collect(Collectors.toList());

        lookup.setAllocations(items);
        return lookup;
    }

    @Transactional
    public void clearAllocationsForSession(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + sessionId));

        seatAllocationRepository.deleteByExamSessionId(sessionId);
        session.setStatus(SessionStatus.SCHEDULED);
        examSessionRepository.save(session);

        notificationService.logEvent(
            "ALLOCATIONS_CLEARED",
            "Allocations Cleared",
            "All seat allocations for session '" + session.getSessionName() + "' were reset.",
            "INFO"
        );
    }
}
