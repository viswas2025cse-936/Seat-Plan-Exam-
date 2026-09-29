package com.example.Seat.Plan.service;

import com.example.Seat.Plan.dto.HallRequestDTO;
import com.example.Seat.Plan.dto.HallResponseDTO;
import com.example.Seat.Plan.exception.DuplicateResourceException;
import com.example.Seat.Plan.exception.ResourceNotFoundException;
import com.example.Seat.Plan.exception.SeatAllocationException;
import com.example.Seat.Plan.model.Hall;
import com.example.Seat.Plan.repository.HallRepository;
import com.example.Seat.Plan.repository.SeatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HallService {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final NotificationService notificationService;

    public HallService(HallRepository hallRepository, SeatRepository seatRepository, NotificationService notificationService) {
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public HallResponseDTO createHall(HallRequestDTO request) {
        if (hallRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Hall with name '" + request.getName() + "' already exists.");
        }

        int maxPhysicalSeats = request.getTotalRows() * request.getTotalColumns();
        if (request.getCapacity() > maxPhysicalSeats) {
            throw new IllegalArgumentException(
                "Declared capacity (" + request.getCapacity() + 
                ") cannot exceed total physical seats (" + maxPhysicalSeats + 
                ") calculated as rows * columns."
            );
        }

        Hall hall = new Hall(
            request.getName().trim(),
            request.getTotalRows(),
            request.getTotalColumns(),
            request.getCapacity()
        );
        hall.generateSeats();

        Hall saved = hallRepository.save(hall);

        notificationService.logEvent(
            "HALL_CREATED",
            "Hall Registered",
            "New exam hall '" + saved.getName() + "' created with capacity " + saved.getCapacity() +
            " (" + saved.getTotalRows() + "x" + saved.getTotalColumns() + " grid).",
            "SUCCESS"
        );

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<HallResponseDTO> getAllHalls() {
        return hallRepository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HallResponseDTO> getAllHallsPaged(Pageable pageable) {
        return hallRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public HallResponseDTO getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hall not found with ID: " + id));
        return toResponseDTO(hall);
    }

    @Transactional(readOnly = true)
    public Hall getHallEntityById(Long id) {
        return hallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hall not found with ID: " + id));
    }

    @Transactional
    public HallResponseDTO updateHall(Long id, HallRequestDTO request) {
        Hall hall = hallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hall not found with ID: " + id));

        if (hallRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new DuplicateResourceException("Another hall already uses the name '" + request.getName() + "'.");
        }

        int maxPhysicalSeats = request.getTotalRows() * request.getTotalColumns();
        if (request.getCapacity() > maxPhysicalSeats) {
            throw new IllegalArgumentException(
                "Declared capacity (" + request.getCapacity() + 
                ") cannot exceed total physical seats (" + maxPhysicalSeats + ")."
            );
        }

        boolean dimensionsChanged = !hall.getTotalRows().equals(request.getTotalRows()) ||
                                    !hall.getTotalColumns().equals(request.getTotalColumns());

        hall.setName(request.getName().trim());
        hall.setTotalRows(request.getTotalRows());
        hall.setTotalColumns(request.getTotalColumns());
        hall.setCapacity(request.getCapacity());

        if (dimensionsChanged) {
            hall.generateSeats();
        }

        Hall saved = hallRepository.save(hall);

        notificationService.logEvent(
            "HALL_UPDATED",
            "Hall Updated",
            "Hall '" + saved.getName() + "' updated (Capacity: " + saved.getCapacity() + ").",
            "INFO"
        );

        return toResponseDTO(saved);
    }

    @Transactional
    public void deleteHall(Long id) {
        Hall hall = hallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hall not found with ID: " + id));

        hallRepository.delete(hall);

        notificationService.logEvent(
            "HALL_DELETED",
            "Hall Deleted",
            "Hall '" + hall.getName() + "' has been deleted.",
            "WARNING"
        );
    }

    public HallResponseDTO toResponseDTO(Hall hall) {
        int seatsCount = (hall.getSeats() != null && !hall.getSeats().isEmpty()) 
            ? hall.getSeats().size() 
            : (hall.getTotalRows() * hall.getTotalColumns());

        return new HallResponseDTO(
            hall.getId(),
            hall.getName(),
            hall.getTotalRows(),
            hall.getTotalColumns(),
            hall.getCapacity(),
            seatsCount
        );
    }
}
