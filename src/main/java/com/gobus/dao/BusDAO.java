package com.gobus.dao;

import com.gobus.entity.Bus;
import com.gobus.util.DBUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BusDAO {

    private final DBUtil dbUtil;

    public BusDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public List<Bus> findAll() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM bus ORDER BY brand";
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching buses", e);
        }
        return list;
    }

    public Bus findById(Long id) {
        String sql = "SELECT * FROM bus WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding bus by id", e);
        }
        return null;
    }

    public void save(Bus bus) {
        String sql = "INSERT INTO bus (brand, plate_number, total_seats) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bus.getBrand());
            ps.setString(2, bus.getPlateNumber());
            ps.setInt(3, bus.getTotalSeats());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving bus", e);
        }
    }

    public void update(Bus bus) {
        String sql = "UPDATE bus SET brand=?, plate_number=?, total_seats=? WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bus.getBrand());
            ps.setString(2, bus.getPlateNumber());
            ps.setInt(3, bus.getTotalSeats());
            ps.setLong(4, bus.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating bus", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM bus WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting bus", e);
        }
    }

    private Bus mapRow(ResultSet rs) throws SQLException {
        Bus b = new Bus();
        b.setId(rs.getLong("id"));
        b.setBrand(rs.getString("brand"));
        b.setPlateNumber(rs.getString("plate_number"));
        b.setTotalSeats(rs.getInt("total_seats"));
        return b;
    }
}
