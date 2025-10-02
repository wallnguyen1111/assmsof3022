package org.example.assmsof3022.service;


import org.example.assmsof3022.model.Exam;
import org.example.assmsof3022.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {
    private final ExamRepository repo;

    public ExamService(ExamRepository repo) {
        this.repo = repo;
    }

    public Exam save(Exam e) {
        return repo.save(e);
    }

    public Optional<Exam> findById(Integer id) {
        return repo.findById(id);
    }

    public List<Exam> findAll() {
        return repo.findAll();
    }

    public List<Exam> findByDate(LocalDate date) {
        return repo.findByExamDate(date);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

