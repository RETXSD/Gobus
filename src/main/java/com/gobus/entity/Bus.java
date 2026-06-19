package com.gobus.entity;

public class Bus {
    private Long id;
    private String brand;
    private String plateNumber;
    private int totalSeats;

    public Bus() {}

    public Bus(Long id, String brand, String plateNumber, int totalSeats) {
        this.id = id;
        this.brand = brand;
        this.plateNumber = plateNumber;
        this.totalSeats = totalSeats;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
}
