package com.example.Seat.Plan.controller;

import com.example.Seat.Plan.dto.HallRequestDTO;
import com.example.Seat.Plan.dto.HallResponseDTO;
import com.example.Seat.Plan.service.HallService;
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
@RequestMapping("/api/halls")
@CrossOrigin(origins = "*")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    @PostMapping
    public ResponseEntity<HallResponseDTO> registerHall(@Valid @RequestBody HallRequestDTO request) {
        HallResponseDTO created = hallService.createHall(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HallResponseDTO>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<HallResponseDTO>> getAllHallsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hallService.getAllHallsPaged(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallResponseDTO> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallResponseDTO> updateHall(@PathVariable Long id, @Valid @RequestBody HallRequestDTO request) {
        return ResponseEntity.ok(hallService.updateHall(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}
