package com.example.Seat.Plan.dto;

public class HallResponseDTO {

    private Long id;
    private String name;
    private Integer totalRows;
    private Integer totalColumns;
    private Integer capacity;
    private Integer totalSeats;

    public HallResponseDTO() {
    }

    public HallResponseDTO(Long id, String name, Integer totalRows, Integer totalColumns, Integer capacity, Integer totalSeats) {
        this.id = id;
        this.name = name;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.capacity = capacity;
        this.totalSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getTotalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(Integer totalColumns) {
        this.totalColumns = totalColumns;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}
