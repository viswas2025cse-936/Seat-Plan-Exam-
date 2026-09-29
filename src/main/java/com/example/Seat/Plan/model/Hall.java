package com.example.Seat.Plan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hall name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "Rows count is required")
    @Positive(message = "Rows count must be positive")
    @Column(nullable = false)
    private Integer totalRows;

    @NotNull(message = "Columns count is required")
    @Positive(message = "Columns count must be positive")
    @Column(nullable = false)
    private Integer totalColumns;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    @Column(nullable = false)
    private Integer capacity;

    @JsonIgnore
    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rowNum ASC, colNum ASC")
    private List<Seat> seats = new ArrayList<>();

    public Hall() {
    }

    public Hall(String name, Integer totalRows, Integer totalColumns, Integer capacity) {
        this.name = name;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.capacity = capacity;
    }

    public void generateSeats() {
        this.seats.clear();
        for (int r = 1; r <= this.totalRows; r++) {
            for (int c = 1; c <= this.totalColumns; c++) {
                String seatCode = "R" + r + "C" + c;
                Seat seat = new Seat(this, r, c, seatCode);
                this.seats.add(seat);
            }
        }
    }

    // Getters and Setters

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

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}
