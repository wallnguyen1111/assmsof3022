package org.example.assmsof3022.repository;


import org.example.assmsof3022.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Integer> {
    List<Exam> findByExamDate(LocalDate date);


}

