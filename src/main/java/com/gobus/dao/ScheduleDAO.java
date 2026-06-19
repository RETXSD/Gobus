package com.gobus.dao;

import com.gobus.entity.Schedule;
import com.gobus.util.DBUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ScheduleDAO {

    private final DBUtil dbUtil;

    public ScheduleDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public List<Schedule> findAll() {
        List<Schedule> list = new ArrayList<>();
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

    public List<Schedule> search(String busName, String destination) {
        List<Schedule> list = new ArrayList<>();
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

    public Schedule findById(Long id) {
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

    public void save(Schedule schedule) {
        String sql = "INSERT INTO schedule (bus_id, route, departure_time, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, schedule.getBusId());
            ps.setString(2, schedule.getRoute());
            ps.setTimestamp(3, Timestamp.valueOf(schedule.getDepartureTime()));
            ps.setBigDecimal(4, schedule.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving schedule", e);
        }
    }

    public void update(Schedule schedule) {
        String sql = "UPDATE schedule SET bus_id=?, route=?, departure_time=?, price=? WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, schedule.getBusId());
            ps.setString(2, schedule.getRoute());
            ps.setTimestamp(3, Timestamp.valueOf(schedule.getDepartureTime()));
            ps.setBigDecimal(4, schedule.getPrice());
            ps.setLong(5, schedule.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating schedule", e);
        }
    }

    public void delete(Long id) {
        String deleteNotificationsSql = """
            DELETE n
            FROM notification n
            JOIN booking bk ON n.booking_id = bk.id
            WHERE bk.schedule_id = ?
            """;
        String deleteBookingSeatsSql = """
            DELETE bs
            FROM booking_seat bs
            JOIN booking bk ON bs.booking_id = bk.id
            WHERE bk.schedule_id = ?
            """;
        String deleteBookingsSql = "DELETE FROM booking WHERE schedule_id = ?";
        String deleteScheduleSql = "DELETE FROM schedule WHERE id = ?";

        try (Connection conn = dbUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                executeDelete(conn, deleteNotificationsSql, id);
                executeDelete(conn, deleteBookingSeatsSql, id);
                executeDelete(conn, deleteBookingsSql, id);
                executeDelete(conn, deleteScheduleSql, id);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting schedule", e);
        }
    }

    private void executeDelete(Connection conn, String sql, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Schedule mapRow(ResultSet rs) throws SQLException {
        Schedule j = new Schedule();
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
