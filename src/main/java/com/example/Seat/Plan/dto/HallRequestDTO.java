package com.example.Seat.Plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class HallRequestDTO {

    @NotBlank(message = "Hall name is required")
    private String name;

    @NotNull(message = "Total rows is required")
    @Positive(message = "Total rows must be greater than 0")
    private Integer totalRows;

    @NotNull(message = "Total columns is required")
    @Positive(message = "Total columns must be greater than 0")
    private Integer totalColumns;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;

    public HallRequestDTO() {
    }

    public HallRequestDTO(String name, Integer totalRows, Integer totalColumns, Integer capacity) {
        this.name = name;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.capacity = capacity;
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
}
