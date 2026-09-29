package com.example.Seat.Plan.service;

import com.example.Seat.Plan.model.NotificationLog;
import com.example.Seat.Plan.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Transactional
    public NotificationLog logEvent(String eventType, String title, String message, String severity) {
        // Console notification
        String logLine = String.format("[NOTIFICATION - %s] %s: %s", severity, title, message);
        switch (severity.toUpperCase()) {
            case "ERROR":
                logger.error(logLine);
                break;
            case "WARNING":
                logger.warn(logLine);
                break;
            default:
                logger.info(logLine);
                break;
        }

        NotificationLog log = new NotificationLog(eventType, title, message, severity);
        return notificationLogRepository.save(log);
    }

    public List<NotificationLog> getRecentNotifications() {
        return notificationLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
