package org.example.assmsof3022.controller;

import jakarta.validation.Valid;
import org.example.assmsof3022.dto.NoteRequest;
import org.example.assmsof3022.model.Exam;
import org.example.assmsof3022.model.Note;
import org.example.assmsof3022.service.ExamService;
import org.example.assmsof3022.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class ApiNoteController {
    private final NoteService noteService;
    private final ExamService examService;

    public ApiNoteController(NoteService noteService, ExamService examService) {
        this.noteService = noteService;
        this.examService = examService;
    }

    /**
     * GET /api/notes?date=YYYY-MM-DD
     * Trả về tất cả notes của ngày (kể cả các note đã gắn exam).
     */
    @GetMapping(params = "date")
    public List<NoteResponse> getByDate(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<Note> notes = noteService.findByDate(date);
        return notes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * GET /api/notes?examId=NNN
     * Trả về notes được gắn vào exam cụ thể.
     */
    @GetMapping(params = "examId")
    public List<NoteResponse> getByExam(@RequestParam("examId") Integer examId) {
        return examService.findById(examId)
                .map(noteService::findByExam)
                .map(list -> list.stream().map(this::toResponse).collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * GET /api/notes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> get(@PathVariable Integer id) {
        return noteService.findById(id).map(n -> ResponseEntity.ok(toResponse(n))).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/notes  (body = NoteRequest)
     */
    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest req) {
        Note n = mapToEntity(req);
        Note saved = noteService.save(n);
        return ResponseEntity.ok(toResponse(saved));
    }

    /**
     * PUT /api/notes/{id}  (body = NoteRequest)
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(@PathVariable Integer id, @Valid @RequestBody NoteRequest req) {
        return noteService.findById(id).map(existing -> {
            existing.setDate(req.getDate());
            existing.setTime(formatRange(req.getStartTime(), req.getEndTime()));
            existing.setSubject(req.getSubject());
            existing.setTitle(req.getTitle());
            existing.setContent(req.getContent());
            existing.setPriority(req.getPriority());
            existing.setTags(req.getTags());
            existing.setRemindAt(req.getRemindAt());
            if (req.getExamId() != null) {
                Exam e = examService.findById(req.getExamId()).orElse(null);
                existing.setExam(e);
            } else {
                existing.setExam(null);
            }
            Note updated = noteService.save(existing);
            return ResponseEntity.ok(toResponse(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/notes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set status (Done / Pending)
     * body: {"status":"Done"} or {"status":"Pending"}
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<NoteResponse> setStatus(@PathVariable Integer id, @RequestBody StatusPayload payload) {
        return noteService.findById(id).map(existing -> {
            existing.setStatus(payload.getStatus());
            Note saved = noteService.save(existing);
            return ResponseEntity.ok(toResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    /* ----------------- Helpers ----------------- */

    private Note mapToEntity(NoteRequest r) {
        Note n = new Note();
        n.setDate(r.getDate());
        n.setTime(formatRange(r.getStartTime(), r.getEndTime()));
        n.setSubject(r.getSubject());
        n.setTitle(r.getTitle());
        n.setContent(r.getContent());
        n.setPriority(r.getPriority());
        n.setTags(r.getTags());
        n.setRemindAt(r.getRemindAt());
        if (r.getExamId() != null) {
            Exam e = examService.findById(r.getExamId()).orElse(null);
            n.setExam(e);
        }
        return n;
    }

    private String formatRange(java.time.LocalTime s, java.time.LocalTime e) {
        if (s == null && e == null) return "";
        if (s != null && e != null) return s.toString() + " - " + e.toString();
        return s != null ? s.toString() : e.toString();
    }

    private NoteResponse toResponse(Note n) {
        NoteResponse r = new NoteResponse();
        r.setId(n.getId());
        r.setDate(n.getDate());
        r.setTime(n.getTime());
        r.setSubject(n.getSubject());
        r.setTitle(n.getTitle());
        r.setContent(n.getContent());
        r.setPriority(n.getPriority());
        r.setTags(n.getTags());
        r.setRemindAt(n.getRemindAt());

        // compute status
        String status = computeStatus(n.getStatus(), n.getRemindAt());
        r.setStatus(status);

        if (n.getExam() != null) {
            Exam ex = n.getExam();
            Integer exId = ex.getId();
            String exSubject = ex.getSubject();
            String exStart = ex.getStartTime();
            r.setExam(new ExamSimpleDTO(exId, exSubject, exStart));
        } else {
            r.setExam(null);
        }
        return r;
    }

    private String computeStatus(String persistedStatus, LocalDateTime remindAt) {
        if ("Done".equalsIgnoreCase(persistedStatus)) return "Done";
        LocalDateTime now = LocalDateTime.now();
        if (remindAt != null && now.isAfter(remindAt)) return "Overdue";
        return "Pending";
    }

    /* ---------- DTO classes (inner static) ---------- */

    // lightweight exam info sent to client
    public static class ExamSimpleDTO {
        private Integer id;
        private String subject;
        private String startTime;

        public ExamSimpleDTO() {}

        public ExamSimpleDTO(Integer id, String subject, String startTime) {
            this.id = id;
            this.subject = subject;
            this.startTime = startTime;
        }

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
    }

    // response object for a Note (avoids sending entity/proxy)
    public static class NoteResponse {
        private Integer id;
        private java.time.LocalDate date;
        private String time;
        private String subject;
        private String title;
        private String content;
        private String priority;
        private String tags;
        private ExamSimpleDTO exam;
        private LocalDateTime remindAt;
        private String status;

        public NoteResponse() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public java.time.LocalDate getDate() { return date; }
        public void setDate(java.time.LocalDate date) { this.date = date; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public ExamSimpleDTO getExam() { return exam; }
        public void setExam(ExamSimpleDTO exam) { this.exam = exam; }

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
