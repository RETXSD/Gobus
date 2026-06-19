package com.gobus.dao;

import com.gobus.entity.Jadwal;
import com.gobus.util.DBUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JadwalDAO {

    private final DBUtil dbUtil;

    public JadwalDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public List<Jadwal> findAll() {
        List<Jadwal> list = new ArrayList<>();
        String sql = """
            SELECT s.*, b.brand AS bus_brand, b.plate_number AS bus_plate, b.total_seats AS bus_seats
            FROM schedule s
            JOIN bus b ON s.bus_id = b.id
            ORDER BY s.departure_time
            """;
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching schedules", e);
        }
        return list;
    }

    public List<Jadwal> search(String busName, String destination) {
        List<Jadwal> list = new ArrayList<>();
        String sql = """
            SELECT s.*, b.brand AS bus_brand, b.plate_number AS bus_plate, b.total_seats AS bus_seats
            FROM schedule s
            JOIN bus b ON s.bus_id = b.id
            WHERE (? IS NULL OR LOWER(b.brand) LIKE LOWER(CONCAT('%', ?, '%')))
              AND (? IS NULL OR LOWER(SUBSTRING_INDEX(s.route, ' - ', -1)) LIKE LOWER(CONCAT('%', ?, '%')))
            ORDER BY s.departure_time
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, busName);
            ps.setString(2, busName);
            ps.setString(3, destination);
            ps.setString(4, destination);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error searching schedules", e);
        }
        return list;
    }

    public Jadwal findById(Long id) {
        String sql = """
            SELECT s.*, b.brand AS bus_brand, b.plate_number AS bus_plate, b.total_seats AS bus_seats
            FROM schedule s
            JOIN bus b ON s.bus_id = b.id
            WHERE s.id = ?
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding schedule by id", e);
        }
        return null;
    }

    public void save(Jadwal jadwal) {
        String sql = "INSERT INTO schedule (bus_id, route, departure_time, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jadwal.getBusId());
            ps.setString(2, jadwal.getRoute());
            ps.setTimestamp(3, Timestamp.valueOf(jadwal.getDepartureTime()));
            ps.setBigDecimal(4, jadwal.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving schedule", e);
        }
    }

    public void update(Jadwal jadwal) {
        String sql = "UPDATE schedule SET bus_id=?, route=?, departure_time=?, price=? WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jadwal.getBusId());
            ps.setString(2, jadwal.getRoute());
            ps.setTimestamp(3, Timestamp.valueOf(jadwal.getDepartureTime()));
            ps.setBigDecimal(4, jadwal.getPrice());
            ps.setLong(5, jadwal.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating schedule", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM schedule WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting schedule", e);
        }
    }

    private Jadwal mapRow(ResultSet rs) throws SQLException {
        Jadwal j = new Jadwal();
        j.setId(rs.getLong("id"));
        j.setBusId(rs.getLong("bus_id"));
        j.setRoute(rs.getString("route"));
        j.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
        j.setPrice(rs.getBigDecimal("price"));
        j.setBusBrand(rs.getString("bus_brand"));
        j.setBusPlateNumber(rs.getString("bus_plate"));
        j.setBusTotalSeats(rs.getInt("bus_seats"));
        return j;
    }
}
