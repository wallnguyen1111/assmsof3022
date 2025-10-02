package org.example.assmsof3022.repository;


import org.example.assmsof3022.model.Exam;
import org.example.assmsof3022.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;



public interface NoteRepository extends JpaRepository<Note, Integer> {
    List<Note> findByDateOrderByTime(LocalDate date);
    List<Note> findByExam(Exam exam);
}
