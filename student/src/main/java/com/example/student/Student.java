package com.example.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student {
    // Encapsulation: all fields private
    private int studentId;
    private String name;
    private String email;
    private String course;
    private double programmingScore;
    private double mathScore;
    private List<String> skills;

    // Default constructor (needed by Spring to read JSON)
    public Student() {
        this.skills = new ArrayList<>();
    }

    // Parameterized constructor
    public Student(int studentId, String name, String email, String course,
                   double programmingScore, double mathScore, List<String> skills) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.course = course;
        this.programmingScore = programmingScore;
        this.mathScore = mathScore;
        this.skills = new ArrayList<>(skills);
    }

    // Getters and setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public double getProgrammingScore() { return programmingScore; }
    public void setProgrammingScore(double programmingScore) { this.programmingScore = programmingScore; }
    public double getMathScore() { return mathScore; }
    public void setMathScore(double mathScore) { this.mathScore = mathScore; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = new ArrayList<>(skills); }

    // Object methods (business logic)
    public double getAverage() {
        return (programmingScore + mathScore) / 2;
    }

    public String getCategory() {
        double avg = getAverage();
        if (avg >= 85) return "Excellent";
        else if (avg >= 70) return "Good";
        else if (avg >= 50) return "Satisfactory";
        else return "Needs Improvement";
    }

    public String getStrongerSubject() {
        if (programmingScore > mathScore) return "Programming";
        else if (mathScore > programmingScore) return "Mathematics";
        else return "Both equal";
    }

    public static boolean isValidScore(double score) {
        return score >= 0 && score <= 100;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-8s %-18s %-6s Prog:%-5.1f Math:%-5.1f Avg:%-6.2f %-18s Stronger:%-12s Skills:%s",
                studentId, name, email, course, programmingScore, mathScore,
                getAverage(), getCategory(), getStrongerSubject(), skills);
    }

    // Two students are equal if their IDs match
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        return studentId == ((Student) o).studentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
