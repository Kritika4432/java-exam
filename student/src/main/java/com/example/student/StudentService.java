package com.example.student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    public Optional<Student> getStudentById(int id) {
        return repo.findById(id);
    }

    // Rules: scores must be 0-100, student ID must be unique
    public Student addStudent(Student s) {
        validateScores(s.getProgrammingScore(), s.getMathScore());
        if (repo.existsById(s.getStudentId())) {
            throw new IllegalStateException("Student ID " + s.getStudentId() + " already exists.");
        }
        repo.save(s);
        return s;
    }

    public Optional<Student> updateScoresAndSkills(int id, Student updated) {
        if (!repo.existsById(id)) {
            return Optional.empty();
        }
        validateScores(updated.getProgrammingScore(), updated.getMathScore());
        repo.updateScoresAndSkills(id, updated.getProgrammingScore(), updated.getMathScore(), updated.getSkills());
        return repo.findById(id);
    }

    public boolean deleteStudent(int id) {
        return repo.deleteById(id) > 0;
    }

    // Merit list: sort the List by average, highest first
    public List<Student> getMeritList() {
        List<Student> list = new ArrayList<>(repo.findAll());
        list.sort(Comparator.comparingDouble(Student::getAverage).reversed());
        return list;
    }

    private void validateScores(double prog, double math) {
        if (!Student.isValidScore(prog) || !Student.isValidScore(math)) {
            throw new IllegalArgumentException("Scores must be between 0 and 100.");
        }
    }
}
