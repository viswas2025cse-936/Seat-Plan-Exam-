package com.example.Seat.Plan.controller;

import com.example.Seat.Plan.dto.ExamSessionRequestDTO;
import com.example.Seat.Plan.dto.ExamSessionResponseDTO;
import com.example.Seat.Plan.model.SessionStatus;
import com.example.Seat.Plan.service.ExamSessionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class ExamSessionController {

    private final ExamSessionService examSessionService;

    public ExamSessionController(ExamSessionService examSessionService) {
        this.examSessionService = examSessionService;
    }

    @PostMapping
    public ResponseEntity<ExamSessionResponseDTO> createSession(@Valid @RequestBody ExamSessionRequestDTO request) {
        ExamSessionResponseDTO created = examSessionService.createSession(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExamSessionResponseDTO>> getAllSessions() {
        return ResponseEntity.ok(examSessionService.getAllSessions());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ExamSessionResponseDTO>> getAllSessionsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "examDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(examSessionService.getAllSessionsPaged(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamSessionResponseDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(examSessionService.getSessionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamSessionResponseDTO> updateSession(@PathVariable Long id, @Valid @RequestBody ExamSessionRequestDTO request) {
        return ResponseEntity.ok(examSessionService.updateSession(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExamSessionResponseDTO> updateSessionStatus(
            @PathVariable Long id,
            @RequestParam SessionStatus status) {
        return ResponseEntity.ok(examSessionService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        examSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
