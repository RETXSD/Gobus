package com.gobus.service;

import com.gobus.dao.ScheduleDAO;
import com.gobus.entity.Schedule;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleDAO scheduleDAO;

    public ScheduleService(ScheduleDAO scheduleDAO) {
        this.scheduleDAO = scheduleDAO;
    }

    public List<Schedule> findAll() {
        return scheduleDAO.findAll();
    }

    public List<Schedule> search(String busName, String destination) {
        String normalizedBusName = normalizeFilter(busName);
        String normalizedDestination = normalizeFilter(destination);
        if (normalizedBusName == null && normalizedDestination == null) {
            return scheduleDAO.findAll();
        }
        return scheduleDAO.search(normalizedBusName, normalizedDestination);
    }

    public Schedule findById(Long id) {
        return scheduleDAO.findById(id);
    }

    public void save(Schedule schedule) {
        scheduleDAO.save(schedule);
    }

    public void update(Schedule schedule) {
        scheduleDAO.update(schedule);
    }

    public void delete(Long id) {
        scheduleDAO.delete(id);
    }

    public boolean deleteIfExpired(Long id) {
        Schedule schedule = scheduleDAO.findById(id);
        if (schedule == null || !schedule.getDepartureTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        scheduleDAO.delete(id);
        return true;
    }

    private String normalizeFilter(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
