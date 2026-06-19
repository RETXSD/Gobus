package com.gobus.dao;

import com.gobus.entity.Booking;
import com.gobus.util.DBUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookingDAO {

    private final DBUtil dbUtil;

    public BookingDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    @PostConstruct
    public void ensureBookingSeatTableExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS booking_seat (
              id          BIGINT AUTO_INCREMENT PRIMARY KEY,
              booking_id  BIGINT NOT NULL,
              seat_number INT    NOT NULL,
              UNIQUE KEY uq_booking_seat (booking_id, seat_number),
              FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
            )
            """;
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error preparing booking_seat table: " + e.getMessage(), e);
        }
    }

    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            ORDER BY bk.created_at DESC
            """;
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching bookings: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Booking> findByUserId(Long userId) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.user_id = ?
            ORDER BY bk.created_at DESC
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user bookings: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Booking> findByScheduleId(Long scheduleId) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.schedule_id = ?
            ORDER BY bk.seat_number
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, scheduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching schedule bookings", e);
        }
        return list;
    }

    public List<Booking> searchByScheduleIdAndBookingCode(Long scheduleId, String bookingCode) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.schedule_id = ?
              AND LOWER(bk.booking_code) LIKE LOWER(CONCAT('%', ?, '%'))
            ORDER BY bk.seat_number
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, scheduleId);
            ps.setString(2, bookingCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error searching schedule bookings", e);
        }
        return list;
    }

    public Booking findById(Long id) {
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.id = ?
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding booking by id", e);
        }
        return null;
    }

    public List<Booking> findPendingGroup(Long userId, Long scheduleId, java.time.LocalDateTime createdAt) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.user_id = ?
              AND bk.schedule_id = ?
              AND bk.payment_status = 'PENDING'
              AND bk.created_at BETWEEN ? AND ?
            ORDER BY bk.seat_number
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, scheduleId);
            ps.setTimestamp(3, Timestamp.valueOf(createdAt.minusSeconds(3)));
            ps.setTimestamp(4, Timestamp.valueOf(createdAt.plusSeconds(3)));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching pending booking group", e);
        }
        return list;
    }

    public List<Booking> findGroup(Long userId, Long scheduleId, java.time.LocalDateTime createdAt) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.user_id = ?
              AND bk.schedule_id = ?
              AND bk.created_at BETWEEN ? AND ?
            ORDER BY bk.seat_number
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, scheduleId);
            ps.setTimestamp(3, Timestamp.valueOf(createdAt.minusSeconds(3)));
            ps.setTimestamp(4, Timestamp.valueOf(createdAt.plusSeconds(3)));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching booking group", e);
        }
        return list;
    }

    public List<Integer> findBookedSeatsByScheduleId(Long scheduleId) {
        List<Integer> seats = new ArrayList<>();
        String sql = """
            SELECT bs.seat_number
            FROM booking bk
            JOIN booking_seat bs ON bs.booking_id = bk.id
            WHERE bk.schedule_id = ?
            UNION
            SELECT seat_number
            FROM booking
            WHERE schedule_id = ?
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, scheduleId);
            ps.setLong(2, scheduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) seats.add(rs.getInt("seat_number"));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching booked seats", e);
        }
        return seats;
    }

    public Long save(Booking booking) {
        String sql = "INSERT INTO booking (user_id, schedule_id, seat_number, booking_code, payment_status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getUserId());
            ps.setLong(2, booking.getScheduleId());
            ps.setInt(3, booking.getSeatNumber());
            ps.setString(4, booking.getBookingCode());
            ps.setString(5, booking.getPaymentStatus());
            ps.setTimestamp(6, Timestamp.valueOf(booking.getCreatedAt()));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving booking", e);
        }
        return null;
    }

    public void saveSeats(Long bookingId, List<Integer> seatNumbers) {
        String sql = "INSERT INTO booking_seat (booking_id, seat_number) VALUES (?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer seatNumber : seatNumbers) {
                ps.setLong(1, bookingId);
                ps.setInt(2, seatNumber);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving booking seats", e);
        }
    }

    public void updatePaymentStatus(Long id, String status) {
        String sql = "UPDATE booking SET payment_status=? WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payment status", e);
        }
    }

    /**
     * Find bookings with PAID status whose departure_time is within the given range
     * Used by the scheduler to send reminders.
     */
    public List<Booking> findPaidBookingsWithDepartureBetween(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT bk.*, u.name AS user_name, u.email AS user_email,
                   b.brand AS bus_brand, b.plate_number AS bus_plate,
                   s.route, s.departure_time, s.price,
                   COALESCE(bs.seat_numbers, CAST(bk.seat_number AS CHAR)) AS seat_numbers
            FROM booking bk
            JOIN users u ON bk.user_id = u.id
            JOIN schedule s ON bk.schedule_id = s.id
            JOIN bus b ON s.bus_id = b.id
            LEFT JOIN (
                SELECT booking_id, GROUP_CONCAT(seat_number ORDER BY seat_number SEPARATOR ',') AS seat_numbers
                FROM booking_seat
                GROUP BY booking_id
            ) bs ON bs.booking_id = bk.id
            WHERE bk.payment_status = 'PAID'
              AND s.departure_time BETWEEN ? AND ?
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching paid bookings for scheduler", e);
        }
        return list;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        b.setScheduleId(rs.getLong("schedule_id"));
        b.setSeatNumber(rs.getInt("seat_number"));
        b.setSeatNumbers(parseSeatNumbers(rs.getString("seat_numbers"), b.getSeatNumber()));
        b.setBookingCode(rs.getString("booking_code"));
        b.setPaymentStatus(rs.getString("payment_status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        b.setUserName(rs.getString("user_name"));
        b.setUserEmail(rs.getString("user_email"));
        b.setBusBrand(rs.getString("bus_brand"));
        b.setBusPlateNumber(rs.getString("bus_plate"));
        b.setRoute(rs.getString("route"));
        b.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
        b.setPrice(rs.getBigDecimal("price"));
        return b;
    }

    private List<Integer> parseSeatNumbers(String seatNumbersText, int fallbackSeat) {
        List<Integer> seats = new ArrayList<>();
        if (seatNumbersText == null || seatNumbersText.isBlank()) {
            seats.add(fallbackSeat);
            return seats;
        }

        for (String seat : seatNumbersText.split(",")) {
            if (!seat.isBlank()) {
                seats.add(Integer.parseInt(seat.trim()));
            }
        }
        return seats;
    }
}
