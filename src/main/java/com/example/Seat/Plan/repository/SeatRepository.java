package com.example.Seat.Plan.repository;

import com.example.Seat.Plan.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByHallIdOrderByRowNumAscColNumAsc(Long hallId);
    List<Seat> findByHallIdAndIsActiveTrueOrderByRowNumAscColNumAsc(Long hallId);
    Optional<Seat> findByHallIdAndRowNumAndColNum(Long hallId, Integer rowNum, Integer colNum);
    long countByHallId(Long hallId);
    void deleteByHallId(Long hallId);
}
