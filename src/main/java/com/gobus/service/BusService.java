package com.gobus.service;

import com.gobus.dao.BusDAO;
import com.gobus.entity.Bus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusService {

    private final BusDAO busDAO;

    public BusService(BusDAO busDAO) {
        this.busDAO = busDAO;
    }

    public List<Bus> findAll() {
        return busDAO.findAll();
    }

    public Bus findById(Long id) {
        return busDAO.findById(id);
    }

    public void save(Bus bus) {
        busDAO.save(bus);
    }

    public void update(Bus bus) {
        busDAO.update(bus);
    }

    public void delete(Long id) {
        busDAO.delete(id);
    }
}
