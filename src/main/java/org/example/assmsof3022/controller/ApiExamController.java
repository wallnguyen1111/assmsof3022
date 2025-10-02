package org.example.assmsof3022.controller;

import jakarta.validation.Valid;
import org.example.assmsof3022.dto.ExamRequest;
import org.example.assmsof3022.model.Exam;
import org.example.assmsof3022.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exams")
public class ApiExamController {
    private final ExamService examService;
    public ApiExamController(ExamService examService) { this.examService = examService; }

    @GetMapping
    public List<ExamResponse> all() {
        return examService.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/byDate")
    public List<ExamResponse> byDate(@RequestParam("date") String dateStr) {
        return examService.findByDate(java.time.LocalDate.parse(dateStr)).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> get(@PathVariable Integer id) {
        return examService.findById(id).map(e -> ResponseEntity.ok(toResponse(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ExamResponse> create(@Valid @RequestBody ExamRequest req) {
        Exam e = mapToEntity(req);
        Exam saved = examService.save(e);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> update(@PathVariable Integer id, @Valid @RequestBody ExamRequest req) {
        return examService.findById(id).map(existing -> {
            existing.setSubject(req.getSubject());
            existing.setExamDate(req.getExamDate());
            existing.setStartTime(req.getStartTime().toString());
            existing.setDurationMinutes(req.getDurationMinutes());
            existing.setLocation(req.getLocation());
            existing.setType(req.getType());
            existing.setDescription(req.getDescription());
            existing.setRemindAt(req.getRemindAt());
            // do not auto-change status here
            Exam updated = examService.save(existing);
            return ResponseEntity.ok(toResponse(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        examService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set status (Done / Pending)
     * body: {"status":"Done"} or {"status":"Pending"}
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ExamResponse> setStatus(@PathVariable Integer id, @RequestBody StatusPayload payload) {
        return examService.findById(id).map(existing -> {
            existing.setStatus(payload.getStatus());
            Exam saved = examService.save(existing);
            return ResponseEntity.ok(toResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Exam mapToEntity(ExamRequest r) {
        Exam e = new Exam();
        e.setSubject(r.getSubject());
        e.setExamDate(r.getExamDate());
        e.setStartTime(r.getStartTime().toString());
        e.setDurationMinutes(r.getDurationMinutes());
        e.setLocation(r.getLocation());
        e.setType(r.getType());
        e.setDescription(r.getDescription());
        e.setRemindAt(r.getRemindAt());
        // default status null -> computed in response
        return e;
    }

    private ExamResponse toResponse(Exam e) {
        ExamResponse r = new ExamResponse();
        r.setId(e.getId());
        r.setSubject(e.getSubject());
        r.setExamDate(e.getExamDate());
        r.setStartTime(e.getStartTime());
        r.setDurationMinutes(e.getDurationMinutes());
        r.setLocation(e.getLocation());
        r.setType(e.getType());
        r.setDescription(e.getDescription());
        r.setRemindAt(e.getRemindAt());
        // compute status
        String status = computeStatus(e.getStatus(), e.getRemindAt());
        r.setStatus(status);
        return r;
    }

    private String computeStatus(String persistedStatus, LocalDateTime remindAt) {
        if ("Done".equalsIgnoreCase(persistedStatus)) return "Done";
        LocalDateTime now = LocalDateTime.now();
        if (remindAt != null && now.isAfter(remindAt)) return "Overdue";
        return "Pending";
    }

    /* ---------- DTO classes (inner static) ---------- */
    public static class ExamResponse {
        private Integer id;
        private String subject;
        private java.time.LocalDate examDate;
        private String startTime;
        private Integer durationMinutes;
        private String location;
        private String type;
        private String description;
        private LocalDateTime remindAt;
        private String status;

        public ExamResponse() {}

        // getters / setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public java.time.LocalDate getExamDate() { return examDate; }
        public void setExamDate(java.time.LocalDate examDate) { this.examDate = examDate; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getRemindAt() { return remindAt; }
        public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class StatusPayload {
        private String status;
        public StatusPayload() {}
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
