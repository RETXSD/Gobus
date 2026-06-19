package com.gobus.service;

import com.gobus.dao.JadwalDAO;
import com.gobus.entity.Jadwal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JadwalService {

    private final JadwalDAO jadwalDAO;

    public JadwalService(JadwalDAO jadwalDAO) {
        this.jadwalDAO = jadwalDAO;
    }

    public List<Jadwal> findAll() {
        return jadwalDAO.findAll();
    }

    public List<Jadwal> search(String busName, String destination) {
        String normalizedBusName = normalizeFilter(busName);
        String normalizedDestination = normalizeFilter(destination);
        if (normalizedBusName == null && normalizedDestination == null) {
            return jadwalDAO.findAll();
        }
        return jadwalDAO.search(normalizedBusName, normalizedDestination);
    }

    public Jadwal findById(Long id) {
        return jadwalDAO.findById(id);
    }

    public void save(Jadwal jadwal) {
        jadwalDAO.save(jadwal);
    }

    public void update(Jadwal jadwal) {
        jadwalDAO.update(jadwal);
    }

    public void delete(Long id) {
        jadwalDAO.delete(id);
    }

    public boolean deleteIfExpired(Long id) {
        Jadwal jadwal = jadwalDAO.findById(id);
        if (jadwal == null || !jadwal.getDepartureTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        jadwalDAO.delete(id);
        return true;
    }

    private String normalizeFilter(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
