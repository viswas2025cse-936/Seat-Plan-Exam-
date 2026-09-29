package com.example.Seat.Plan.service;

import com.example.Seat.Plan.dto.ExamSessionRequestDTO;
import com.example.Seat.Plan.dto.ExamSessionResponseDTO;
import com.example.Seat.Plan.dto.HallResponseDTO;
import com.example.Seat.Plan.exception.ResourceNotFoundException;
import com.example.Seat.Plan.model.ExamSession;
import com.example.Seat.Plan.model.Hall;
import com.example.Seat.Plan.model.SessionStatus;
import com.example.Seat.Plan.model.Student;
import com.example.Seat.Plan.repository.ExamSessionRepository;
import com.example.Seat.Plan.repository.HallRepository;
import com.example.Seat.Plan.repository.SeatAllocationRepository;
import com.example.Seat.Plan.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamSessionService {

    private final ExamSessionRepository examSessionRepository;
    private final HallRepository hallRepository;
    private final StudentRepository studentRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final HallService hallService;
    private final NotificationService notificationService;

    public ExamSessionService(ExamSessionRepository examSessionRepository,
                              HallRepository hallRepository,
                              StudentRepository studentRepository,
                              SeatAllocationRepository seatAllocationRepository,
                              HallService hallService,
                              NotificationService notificationService) {
        this.examSessionRepository = examSessionRepository;
        this.hallRepository = hallRepository;
        this.studentRepository = studentRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.hallService = hallService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ExamSessionResponseDTO createSession(ExamSessionRequestDTO request) {
        ExamSession session = new ExamSession(
            request.getSessionName().trim(),
            request.getCourseCode() != null ? request.getCourseCode().trim() : null,
            request.getExamDate(),
            request.getStartTime(),
            request.getEndTime()
        );

        if (request.getHallIds() != null && !request.getHallIds().isEmpty()) {
            List<Hall> halls = hallRepository.findAllById(request.getHallIds());
            session.setHalls(new HashSet<>(halls));
        }

        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            List<Student> students = studentRepository.findAllById(request.getStudentIds());
            session.setStudents(new HashSet<>(students));
        }

        ExamSession saved = examSessionRepository.save(session);

        notificationService.logEvent(
            "SESSION_CREATED",
            "Exam Session Created",
            "Session '" + saved.getSessionName() + "' scheduled for " + saved.getExamDate() + ".",
            "SUCCESS"
        );

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ExamSessionResponseDTO> getAllSessions() {
        return examSessionRepository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExamSessionResponseDTO> getAllSessionsPaged(Pageable pageable) {
        return examSessionRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ExamSessionResponseDTO getSessionById(Long id) {
        ExamSession session = examSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + id));
        return toResponseDTO(session);
    }

    @Transactional(readOnly = true)
    public ExamSession getSessionEntityById(Long id) {
        return examSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + id));
    }

    @Transactional
    public ExamSessionResponseDTO updateSession(Long id, ExamSessionRequestDTO request) {
        ExamSession session = examSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + id));

        session.setSessionName(request.getSessionName().trim());
        session.setCourseCode(request.getCourseCode() != null ? request.getCourseCode().trim() : null);
        session.setExamDate(request.getExamDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());

        if (request.getHallIds() != null) {
            List<Hall> halls = hallRepository.findAllById(request.getHallIds());
            session.setHalls(new HashSet<>(halls));
        }

        if (request.getStudentIds() != null) {
            List<Student> students = studentRepository.findAllById(request.getStudentIds());
            session.setStudents(new HashSet<>(students));
        }

        ExamSession saved = examSessionRepository.save(session);
        return toResponseDTO(saved);
    }

    @Transactional
    public ExamSessionResponseDTO updateStatus(Long id, SessionStatus status) {
        ExamSession session = examSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + id));

        session.setStatus(status);
        ExamSession saved = examSessionRepository.save(session);

        notificationService.logEvent(
            "SESSION_STATUS_CHANGED",
            "Session Status Changed",
            "Session '" + saved.getSessionName() + "' status changed to " + status + ".",
            "INFO"
        );

        return toResponseDTO(saved);
    }

    @Transactional
    public void deleteSession(Long id) {
        ExamSession session = examSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found with ID: " + id));

        seatAllocationRepository.deleteByExamSessionId(id);
        examSessionRepository.delete(session);

        notificationService.logEvent(
            "SESSION_DELETED",
            "Exam Session Deleted",
            "Exam session '" + session.getSessionName() + "' and its allocations were deleted.",
            "WARNING"
        );
    }

    public ExamSessionResponseDTO toResponseDTO(ExamSession session) {
        ExamSessionResponseDTO dto = new ExamSessionResponseDTO();
        dto.setId(session.getId());
        dto.setSessionName(session.getSessionName());
        dto.setCourseCode(session.getCourseCode());
        dto.setExamDate(session.getExamDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setStatus(session.getStatus());

        int hallsCount = session.getHalls() != null ? session.getHalls().size() : 0;
        int studentsCount = session.getStudents() != null ? session.getStudents().size() : 0;
        int totalDeclaredCapacity = 0;
        if (session.getHalls() != null) {
            for (Hall h : session.getHalls()) {
                totalDeclaredCapacity += (h.getCapacity() != null ? h.getCapacity() : 0);
            }
        }

        long allocatedCount = seatAllocationRepository.countByExamSessionId(session.getId());

        dto.setHallsCount(hallsCount);
        dto.setStudentsCount(studentsCount);
        dto.setTotalDeclaredCapacity(totalDeclaredCapacity);
        dto.setAllocatedCount(allocatedCount);

        if (session.getHalls() != null) {
            List<HallResponseDTO> hallDTOs = session.getHalls().stream()
                .map(hallService::toResponseDTO)
                .collect(Collectors.toList());
            dto.setHalls(hallDTOs);
        }

        return dto;
    }
}
