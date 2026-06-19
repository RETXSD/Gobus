package com.gobus.dao;

import com.gobus.entity.Notifikasi;
import com.gobus.util.DBUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotifikasiDAO {

    private final DBUtil dbUtil;

    public NotifikasiDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public List<Notifikasi> findByUserId(Long userId) {
        List<Notifikasi> list = new ArrayList<>();
        String sql = """
            SELECT n.*, bk.booking_code
            FROM notification n
            JOIN booking bk ON n.booking_id = bk.id
            WHERE n.user_id = ?
            ORDER BY n.send_time DESC
            """;
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching notifications", e);
        }
        return list;
    }

    public long countUnreadByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE user_id=? AND is_read=FALSE";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread notifications", e);
        }
        return 0;
    }

    public void markAllReadByUserId(Long userId) {
        String sql = "UPDATE notification SET is_read=TRUE WHERE user_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking notifications read", e);
        }
    }

    public void save(Notifikasi n) {
        String sql = "INSERT INTO notification (user_id, booking_id, message, send_time, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, n.getUserId());
            ps.setLong(2, n.getBookingId());
            ps.setString(3, n.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(n.getSendTime()));
            ps.setBoolean(5, n.isRead());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving notification", e);
        }
    }

    public boolean existsByBookingIdAndMessage(Long bookingId, String message) {
        String sql = "SELECT COUNT(*) FROM notification WHERE booking_id=? AND message=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setString(2, message);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking notification existence", e);
        }
        return false;
    }

    private Notifikasi mapRow(ResultSet rs) throws SQLException {
        Notifikasi n = new Notifikasi();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setBookingId(rs.getLong("booking_id"));
        n.setMessage(rs.getString("message"));
        Timestamp ts = rs.getTimestamp("send_time");
        if (ts != null) n.setSendTime(ts.toLocalDateTime());
        n.setRead(rs.getBoolean("is_read"));
        n.setBookingCode(rs.getString("booking_code"));
        return n;
    }
}
