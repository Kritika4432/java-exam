package com.example.student;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Converts one database row into one Student object
    private final RowMapper<Student> mapper = (rs, rowNum) -> {
        int id = rs.getInt("student_id");
        return new Student(id,
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("course"),
                rs.getDouble("programming_score"),
                rs.getDouble("math_score"),
                findSkills(id));
    };

    public List<String> findSkills(int id) {
        return jdbc.queryForList(
                "SELECT skill FROM student_skills WHERE student_id = ? ORDER BY skill", String.class, id);
    }

    public List<Student> findAll() {
        return jdbc.query("SELECT * FROM students ORDER BY student_id", mapper);
    }

    public Optional<Student> findById(int id) {
        List<Student> result = jdbc.query("SELECT * FROM students WHERE student_id = ?", mapper, id);
        return result.stream().findFirst();
    }

    public boolean existsById(int id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE student_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void save(Student s) {
        jdbc.update("INSERT INTO students (student_id, name, email, course, programming_score, math_score) "
                  + "VALUES (?, ?, ?, ?, ?, ?)",
                s.getStudentId(), s.getName(), s.getEmail(), s.getCourse(),
                s.getProgrammingScore(), s.getMathScore());
        saveSkills(s.getStudentId(), s.getSkills());
    }

    public void updateScoresAndSkills(int id, double prog, double math, List<String> skills) {
        jdbc.update("UPDATE students SET programming_score = ?, math_score = ? WHERE student_id = ?",
                prog, math, id);
        jdbc.update("DELETE FROM student_skills WHERE student_id = ?", id);
        saveSkills(id, skills);
    }

    public int deleteById(int id) {
        // ON DELETE CASCADE removes the student's skills too
        return jdbc.update("DELETE FROM students WHERE student_id = ?", id);
    }

    private void saveSkills(int id, List<String> skills) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();   // Set removes duplicate skills
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) unique.add(skill.trim());
        }
        for (String skill : unique) {
            jdbc.update("INSERT INTO student_skills (student_id, skill) VALUES (?, ?)", id, skill);
        }
    }
}
