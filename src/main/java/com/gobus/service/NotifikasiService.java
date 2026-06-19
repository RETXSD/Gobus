package com.gobus.service;

import com.gobus.dao.NotifikasiDAO;
import com.gobus.entity.Notifikasi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifikasiService {

    private final NotifikasiDAO notifikasiDAO;

    public NotifikasiService(NotifikasiDAO notifikasiDAO) {
        this.notifikasiDAO = notifikasiDAO;
    }

    public List<Notifikasi> findByUserId(Long userId) {
        return notifikasiDAO.findByUserId(userId);
    }

    public long countUnread(Long userId) {
        return notifikasiDAO.countUnreadByUserId(userId);
    }

    public void markAllRead(Long userId) {
        notifikasiDAO.markAllReadByUserId(userId);
    }

    public void save(Notifikasi notifikasi) {
        notifikasiDAO.save(notifikasi);
    }

    public boolean existsByBookingIdAndMessage(Long bookingId, String message) {
        return notifikasiDAO.existsByBookingIdAndMessage(bookingId, message);
    }
}
