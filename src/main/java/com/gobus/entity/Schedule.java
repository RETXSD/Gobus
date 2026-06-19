package com.gobus.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Schedule {
    private Long id;
    private Long busId;
    private String route;
    private LocalDateTime departureTime;
    private BigDecimal price;

    // Joined fields from bus table
    private String busBrand;
    private String busPlateNumber;
    private int busTotalSeats;

    public Schedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getBusBrand() { return busBrand; }
    public void setBusBrand(String busBrand) { this.busBrand = busBrand; }

    public String getBusPlateNumber() { return busPlateNumber; }
    public void setBusPlateNumber(String busPlateNumber) { this.busPlateNumber = busPlateNumber; }

    public int getBusTotalSeats() { return busTotalSeats; }
    public void setBusTotalSeats(int busTotalSeats) { this.busTotalSeats = busTotalSeats; }
}
