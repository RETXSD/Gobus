package com.gobus.service;

import com.gobus.dao.BookingDAO;
import com.gobus.dao.ScheduleDAO;
import com.gobus.entity.Booking;
import com.gobus.entity.Schedule;
import com.gobus.observer.BookingEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class BookingService {

    private final BookingDAO bookingDAO;
    private final ScheduleDAO scheduleDAO;
    private final BookingEventPublisher bookingEventPublisher;

    public BookingService(BookingDAO bookingDAO, ScheduleDAO scheduleDAO, BookingEventPublisher bookingEventPublisher) {
        this.bookingDAO = bookingDAO;
        this.scheduleDAO = scheduleDAO;
        this.bookingEventPublisher = bookingEventPublisher;
    }

    public List<Integer> getBookedSeats(Long scheduleId) {
        return bookingDAO.findBookedSeatsByScheduleId(scheduleId);
    }

    public List<Booking> findByUserId(Long userId) {
        return bookingDAO.findByUserId(userId);
    }

    public List<Booking> findAll() {
        return bookingDAO.findAll();
    }

    public List<Booking> findByScheduleId(Long scheduleId) {
        return bookingDAO.findByScheduleId(scheduleId);
    }

    public List<Booking> searchByScheduleIdAndBookingCode(Long scheduleId, String bookingCode) {
        if (bookingCode == null || bookingCode.trim().isEmpty()) {
            return bookingDAO.findByScheduleId(scheduleId);
        }
        return bookingDAO.searchByScheduleIdAndBookingCode(scheduleId, bookingCode.trim());
    }

    public Booking findById(Long id) {
        return bookingDAO.findById(id);
    }

    public List<Booking> findBookingGroup(Long bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            return List.of();
        }
        return bookingDAO.findGroup(booking.getUserId(), booking.getScheduleId(), booking.getCreatedAt());
    }

    /**
     * Creates a booking with PENDING payment status.
     * Returns the generated booking ID.
     */
    public Long createBooking(Long userId, Long scheduleId, int seatNumber) {
        Long bookingId = createSingleBooking(userId, scheduleId, seatNumber);
        bookingDAO.saveSeats(bookingId, List.of(seatNumber));
        return bookingId;
    }

    public List<Long> createBookings(Long userId, Long scheduleId, List<Integer> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new RuntimeException("Please select at least one seat");
        }

        List<Integer> bookedSeats = bookingDAO.findBookedSeatsByScheduleId(scheduleId);
        for (Integer seatNumber : seatNumbers) {
            if (bookedSeats.contains(seatNumber)) {
                throw new RuntimeException("Seat " + seatNumber + " is already booked");
            }
        }

        Long bookingId = createSingleBooking(userId, scheduleId, seatNumbers.get(0));
        bookingDAO.saveSeats(bookingId, seatNumbers);
        return List.of(bookingId);
    }

    private Long createSingleBooking(Long userId, Long scheduleId, int seatNumber) {
        Schedule schedule = scheduleDAO.findById(scheduleId);
        if (schedule == null) throw new RuntimeException("Schedule not found");

        String brand = schedule.getBusBrand().substring(0, Math.min(3, schedule.getBusBrand().length())).toUpperCase();
        String seat = String.format("%02d", seatNumber);
        String random = String.format("%04d", new Random().nextInt(10000));
        String bookingCode = "GB-" + brand + seat + "-" + random;

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setScheduleId(scheduleId);
        booking.setSeatNumber(seatNumber);
        booking.setBookingCode(bookingCode);
        booking.setPaymentStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());

        return bookingDAO.save(booking);
    }

    /**
     * Simulates payment and notifies observers after the payment succeeds.
     */
    public Booking pay(Long bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }

        List<Booking> bookingGroup = bookingDAO.findPendingGroup(booking.getUserId(), booking.getScheduleId(), booking.getCreatedAt());
        if (bookingGroup.isEmpty()) {
            bookingGroup = List.of(booking);
        }

        for (Booking item : bookingGroup) {
            bookingDAO.updatePaymentStatus(item.getId(), "PAID");
            Booking paidBooking = bookingDAO.findById(item.getId());
            bookingEventPublisher.notifyPaymentSuccess(paidBooking);
        }

        return booking;
    }
}
