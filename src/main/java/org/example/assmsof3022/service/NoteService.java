package org.example.assmsof3022.service;

import org.example.assmsof3022.model.Exam;
import org.example.assmsof3022.model.Note;
import org.example.assmsof3022.repository.NoteRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NoteRepository repo;
    public NoteService(NoteRepository repo) { this.repo = repo; }

    public Note save(Note n) { return repo.save(n); }
    public Optional<Note> findById(Integer id) { return repo.findById(id); }
    public List<Note> findByDate(LocalDate date) { return repo.findByDateOrderByTime(date); }
    public List<Note> findByExam(Exam e) { return repo.findByExam(e); }
    public void delete(Integer id) { repo.deleteById(id); }
}

