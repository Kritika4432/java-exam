package com.example.student;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    // 1. Add a student
    @PostMapping
    public ResponseEntity<Object> addStudent(@RequestBody Student student) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.addStudent(student));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }
    }

    // 2. Retrieve all students
    @GetMapping
    public List<Student> getAllStudents() {
        return service.getAllStudents();
    }

    // 6. Display the merit list
    @GetMapping("/merit-list")
    public List<Student> getMeritList() {
        return service.getMeritList();
    }

    // 3. Search for a student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> getStudentById(@PathVariable("id") int id) {
        Optional<Student> result = service.getStudentById(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student " + id + " not found.");
    }

    // 4. Update student scores and skills
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateStudent(@PathVariable("id") int id,
                                                @RequestBody Student updated) {
        try {
            Optional<Student> result = service.updateScoresAndSkills(id, updated);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student " + id + " not found.");
            }
            return ResponseEntity.ok(result.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Delete a student record
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteStudent(@PathVariable("id") int id) {
        if (service.deleteStudent(id)) {
            return ResponseEntity.ok("Student " + id + " deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student " + id + " not found.");
    }
}