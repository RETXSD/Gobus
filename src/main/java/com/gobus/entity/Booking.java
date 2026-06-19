package com.gobus.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking {
    private Long id;
    private Long userId;
    private Long scheduleId;
    private int seatNumber;
    private List<Integer> seatNumbers = new ArrayList<>();
    private String bookingCode;
    private String paymentStatus; // "PENDING" or "PAID"
    private LocalDateTime createdAt;

    // Joined fields
    private String userName;
    private String userEmail;
    private String busBrand;
    private String busPlateNumber;
    private String route;
    private LocalDateTime departureTime;
    private java.math.BigDecimal price;

    public Booking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public List<Integer> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<Integer> seatNumbers) { this.seatNumbers = seatNumbers; }

    public String getSeatNumbersText() {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            return String.valueOf(seatNumber);
        }
        return seatNumbers.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + ", " + b)
                .orElse(String.valueOf(seatNumber));
    }

    public int getSeatCount() {
        return seatNumbers == null || seatNumbers.isEmpty() ? 1 : seatNumbers.size();
    }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getBusBrand() { return busBrand; }
    public void setBusBrand(String busBrand) { this.busBrand = busBrand; }

    public String getBusPlateNumber() { return busPlateNumber; }
    public void setBusPlateNumber(String busPlateNumber) { this.busPlateNumber = busPlateNumber; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
}
