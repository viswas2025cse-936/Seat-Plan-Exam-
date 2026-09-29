package com.example.Seat.Plan.repository;

import com.example.Seat.Plan.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findTop50ByOrderByTimestampDesc();
}
