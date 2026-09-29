package com.example.Seat.Plan.controller;

import com.example.Seat.Plan.dto.*;
import com.example.Seat.Plan.service.SeatAllocationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/allocations")
@CrossOrigin(origins = "*")
public class SeatAllocationController {

    private final SeatAllocationService seatAllocationService;

    public SeatAllocationController(SeatAllocationService seatAllocationService) {
        this.seatAllocationService = seatAllocationService;
    }

    /**
     * Auto-allocate seats for an ExamSession across halls avoiding adjacent same-section students.
     */
    @PostMapping("/auto-allocate")
    public ResponseEntity<AutoAllocateResponseDTO> autoAllocate(@Valid @RequestBody AutoAllocateRequestDTO request) {
        AutoAllocateResponseDTO response = seatAllocationService.autoAllocateSeats(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get JSON seating chart for a specific hall in an exam session.
     */
    @GetMapping("/sessions/{sessionId}/halls/{hallId}/chart")
    public ResponseEntity<SeatingChartDTO> getSeatingChart(
            @PathVariable Long sessionId,
            @PathVariable Long hallId) {
        SeatingChartDTO chart = seatAllocationService.getSeatingChart(sessionId, hallId);
        return ResponseEntity.ok(chart);
    }

    /**
     * Look up student's allocated hall and seat number by roll number (path variable).
     */
    @GetMapping("/student/{rollNumber}")
    public ResponseEntity<StudentLookupDTO> lookupByRollNumberPath(@PathVariable String rollNumber) {
        return ResponseEntity.ok(seatAllocationService.lookupStudentByRollNumber(rollNumber));
    }

    /**
     * Look up student's allocated hall and seat number by roll number (query parameter).
     */
    @GetMapping("/lookup")
    public ResponseEntity<StudentLookupDTO> lookupByRollNumberParam(@RequestParam String rollNumber) {
        return ResponseEntity.ok(seatAllocationService.lookupStudentByRollNumber(rollNumber));
    }

    /**
     * Clear allocations for an exam session.
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> clearSessionAllocations(@PathVariable Long sessionId) {
        seatAllocationService.clearAllocationsForSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Printable HTML seating chart view ready for direct printing (Ctrl+P / Exam Notice Board).
     */
    @GetMapping(value = "/sessions/{sessionId}/halls/{hallId}/chart/print", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getPrintableSeatingChartHtml(
            @PathVariable Long sessionId,
            @PathVariable Long hallId) {
        SeatingChartDTO chart = seatAllocationService.getSeatingChart(sessionId, hallId);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<title>Seating Chart - ").append(escapeHtml(chart.getHallName())).append("</title>\n");
        sb.append("<style>\n");
        sb.append("@page { size: landscape; margin: 10mm; }\n");
        sb.append("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; margin: 20px; color: #1e293b; background: #fff; }\n");
        sb.append(".header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 15px; }\n");
        sb.append(".header h1 { margin: 0 0 5px 0; font-size: 24px; text-transform: uppercase; letter-spacing: 1px; color: #0f172a; }\n");
        sb.append(".header .meta { display: flex; justify-content: space-around; font-size: 14px; margin-top: 8px; color: #475569; }\n");
        sb.append(".stage { text-align: center; background: #e2e8f0; border: 1px dashed #94a3b8; padding: 6px; margin: 15px auto; width: 60%; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; font-size: 13px; }\n");
        sb.append(".grid { display: table; margin: 0 auto; border-collapse: separate; border-spacing: 8px; }\n");
        sb.append(".row { display: table-row; }\n");
        sb.append(".seat { display: table-cell; border: 1.5px solid #64748b; border-radius: 6px; padding: 8px 10px; width: 110px; min-height: 75px; text-align: center; vertical-align: middle; background: #f8fafc; font-size: 11px; }\n");
        sb.append(".seat.occupied { background: #f0fdf4; border-color: #22c55e; }\n");
        sb.append(".seat.conflict { background: #fef2f2; border-color: #ef4444; }\n");
        sb.append(".seat-code { font-weight: bold; font-size: 12px; color: #0f172a; margin-bottom: 4px; }\n");
        sb.append(".roll { font-weight: 700; color: #1e40af; font-size: 12px; }\n");
        sb.append(".name { font-weight: 600; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100px; }\n");
        sb.append(".class-sec { display: inline-block; background: #dbeafe; color: #1e40af; padding: 2px 6px; border-radius: 4px; font-weight: bold; font-size: 10px; margin-top: 3px; }\n");
        sb.append(".empty { color: #94a3b8; font-style: italic; }\n");
        sb.append(".footer { margin-top: 25px; display: flex; justify-content: space-between; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; padding-top: 10px; }\n");
        sb.append(".print-btn { background: #2563eb; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 600; margin-bottom: 15px; }\n");
        sb.append("@media print { .print-btn { display: none; } }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<button class=\"print-btn\" onclick=\"window.print()\">🖨️ Print Seating Chart</button>\n");
        sb.append("<div class=\"header\">\n");
        sb.append("<h1>EXAM HALL SEATING ARRANGEMENT</h1>\n");
        sb.append("<div class=\"meta\">\n");
        sb.append("<div><strong>Session:</strong> ").append(escapeHtml(chart.getSessionName())).append("</div>\n");
        if (chart.getCourseCode() != null) {
            sb.append("<div><strong>Course:</strong> ").append(escapeHtml(chart.getCourseCode())).append("</div>\n");
        }
        sb.append("<div><strong>Date:</strong> ").append(chart.getExamDate() != null ? chart.getExamDate().toString() : "-").append("</div>\n");
        sb.append("<div><strong>Hall:</strong> ").append(escapeHtml(chart.getHallName())).append("</div>\n");
        sb.append("<div><strong>Capacity:</strong> ").append(chart.getCapacity()).append("</div>\n");
        sb.append("<div><strong>Allocated:</strong> ").append(chart.getAllocatedCount()).append("</div>\n");
        sb.append("</div>\n</div>\n");

        sb.append("<div class=\"stage\">=== INSTRUCTOR / BLACKBOARD / FRONT ===</div>\n");

        sb.append("<div class=\"grid\">\n");
        if (chart.getGrid() != null) {
            for (java.util.List<SeatCellDTO> row : chart.getGrid()) {
                sb.append("<div class=\"row\">\n");
                for (SeatCellDTO cell : row) {
                    String seatClass = "seat";
                    if (cell.isOccupied()) {
                        seatClass += cell.isHasAdjacentConflict() ? " occupied conflict" : " occupied";
                    }
                    sb.append("<div class=\"").append(seatClass).append("\">\n");
                    sb.append("<div class=\"seat-code\">").append(escapeHtml(cell.getSeatCode())).append("</div>\n");
                    if (cell.isOccupied()) {
                        sb.append("<div class=\"roll\">").append(escapeHtml(cell.getStudentRollNumber())).append("</div>\n");
                        sb.append("<div class=\"name\" title=\"").append(escapeHtml(cell.getStudentName())).append("\">")
                          .append(escapeHtml(cell.getStudentName())).append("</div>\n");
                        sb.append("<div class=\"class-sec\">").append(escapeHtml(cell.getSectionKey())).append("</div>\n");
                    } else {
                        sb.append("<div class=\"empty\">VACANT</div>\n");
                    }
                    sb.append("</div>\n");
                }
                sb.append("</div>\n");
            }
        }
        sb.append("</div>\n");

        sb.append("<div class=\"footer\">\n");
        sb.append("<div>Generated by SeatPlan Exam Seating System</div>\n");
        sb.append("<div>Hall: ").append(escapeHtml(chart.getHallName())).append(" | Total Seated: ").append(chart.getAllocatedCount()).append("</div>\n");
        sb.append("<div>Chief Superintendent Signature: __________________</div>\n");
        sb.append("</div>\n");

        sb.append("</body>\n</html>");

        return ResponseEntity.ok(sb.toString());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
