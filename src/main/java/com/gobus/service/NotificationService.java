package com.gobus.service;

import com.gobus.dao.NotificationDAO;
import com.gobus.entity.Notification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationDAO notificationDAO;

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public List<Notification> findByUserId(Long userId) {
        return notificationDAO.findByUserId(userId);
    }

    public long countUnread(Long userId) {
        return notificationDAO.countUnreadByUserId(userId);
    }

    public void markAllRead(Long userId) {
        notificationDAO.markAllReadByUserId(userId);
    }

    public void save(Notification notification) {
        notificationDAO.save(notification);
    }

    public boolean existsByBookingIdAndMessage(Long bookingId, String message) {
        return notificationDAO.existsByBookingIdAndMessage(bookingId, message);
    }
}
