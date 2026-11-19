package com.example.student;

import java.sql.*;
import java.util.*;

public class StudentManagement {

    static final String URL = "jdbc:mysql://localhost:3307/student_db";
    static final String USER = "root";
    static final String PASSWORD = "gogo6";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in);
             Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Connected to student_db");
            int choice;
            do {
                System.out.println("\n1. Add student");
                System.out.println("2. Show all (average, category, stronger subject)");
                System.out.println("3. Search by ID / email / course");
                System.out.println("4. Merit list");
                System.out.println("5. Students with same technical skills");
                System.out.println("6. Weak in programming but maths >= 75");
                System.out.println("0. Exit");
                System.out.print("Choice: ");
                choice = readInt(sc);

                switch (choice) {
                    case 1 -> addStudent(con, sc);
                    case 2 -> showAll(con);
                    case 3 -> search(con, sc);
                    case 4 -> meritList(con);
                    case 5 -> sameSkills(con);
                    case 6 -> weakProgrammingStrongMaths(con);
                    case 0 -> System.out.println("Goodbye.");
                    default -> System.out.println("Invalid choice.");
                }
            } while (choice != 0);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Req 8: validate scores 0-100 and prevent duplicate IDs
    static void addStudent(Connection con, Scanner sc) {
        System.out.print("Student ID: ");
        int id = readInt(sc);
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Course: ");
        String course = sc.nextLine();
        System.out.print("Programming score (0-100): ");
        double prog = readDouble(sc);
        System.out.print("Mathematics score (0-100): ");
        double math = readDouble(sc);
        System.out.print("Skills (comma separated, e.g. Java,SQL): ");
        String skillLine = sc.nextLine();

        if (!Student.isValidScore(prog) || !Student.isValidScore(math)) {
            System.out.println("Scores must be between 0 and 100. Student not added.");
            return;
        }

        String sql = "INSERT INTO students (student_id, name, email, course, programming_score, math_score) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, course);
            ps.setDouble(5, prog);
            ps.setDouble(6, math);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate student ID or email. Student not added.");
            return;
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            return;
        }

        // Set removes duplicate skills typed by the user
        Set<String> skills = new LinkedHashSet<>();
        for (String s : skillLine.split(",")) {
            if (!s.isBlank()) skills.add(s.trim());
        }
        String skillSql = "INSERT INTO student_skills (student_id, skill) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(skillSql)) {
            for (String skill : skills) {
                ps.setInt(1, id);
                ps.setString(2, skill);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Skill insert failed: " + e.getMessage());
        }
        System.out.println("Student " + id + " added.");
    }

    // Req 1, 2, 3: average, category and stronger subject
    static void showAll(Connection con) {
        List<Student> list = loadStudents(con, "SELECT * FROM students ORDER BY student_id", null);
        if (list.isEmpty()) System.out.println("No students found.");
        for (Student s : list) System.out.println(s);
    }

    // Req 4: search by ID, email or course
    static void search(Connection con, Scanner sc) {
        System.out.print("Search by 1) ID  2) Email  3) Course : ");
        int type = readInt(sc);
        String sql;
        if (type == 1) {
            System.out.print("ID: ");
            sql = "SELECT * FROM students WHERE student_id = ?";
        } else if (type == 2) {
            System.out.print("Email: ");
            sql = "SELECT * FROM students WHERE email = ?";
        } else if (type == 3) {
            System.out.print("Course: ");
            sql = "SELECT * FROM students WHERE course = ?";
        } else {
            System.out.println("Invalid option.");
            return;
        }
        String value = sc.nextLine();
        List<Student> list = loadStudents(con, sql, value);
        if (list.isEmpty()) System.out.println("No matching student.");
        for (Student s : list) System.out.println(s);
    }

    // Req 5: merit list - sort the List by average, highest first
    static void meritList(Connection con) {
        List<Student> list = loadStudents(con, "SELECT * FROM students", null);
        list.sort(Comparator.comparingDouble(Student::getAverage).reversed());
        int rank = 1;
        for (Student s : list) {
            System.out.printf("Rank %d: %-8s Avg %.2f  %s%n", rank++, s.getName(), s.getAverage(), s.getCategory());
        }
    }

    // Req 6: Map of skill -> students who have it; show skills shared by 2+ students
    static void sameSkills(Connection con) {
        List<Student> list = loadStudents(con, "SELECT * FROM students", null);
        Map<String, List<String>> skillMap = new TreeMap<>();
        for (Student s : list) {
            for (String skill : s.getSkills()) {
                skillMap.computeIfAbsent(skill, k -> new ArrayList<>()).add(s.getName());
            }
        }
        boolean found = false;
        for (Map.Entry<String, List<String>> entry : skillMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                found = true;
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
        }
        if (!found) System.out.println("No shared skills.");
    }

    // Req 7: programming below 50 (needs improvement) but maths at least 75
    static void weakProgrammingStrongMaths(Connection con) {
        String sql = "SELECT * FROM students WHERE programming_score < 50 AND math_score >= 75";
        List<Student> list = loadStudents(con, sql, null);
        if (list.isEmpty()) System.out.println("No such students.");
        for (Student s : list) System.out.println(s);
    }

    // Helper: run a SELECT (0 or 1 parameter) and build Student objects
    static List<Student> loadStudents(Connection con, String sql, String param) {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("student_id");
                    list.add(new Student(id,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("course"),
                            rs.getDouble("programming_score"),
                            rs.getDouble("math_score"),
                            getSkills(con, id)));
                }
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return list;
    }

    // Helper: all skills of one student
    static List<String> getSkills(Connection con, int id) throws SQLException {
        List<String> skills = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT skill FROM student_skills WHERE student_id = ? ORDER BY skill")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(rs.getString("skill"));
            }
        }
        return skills;
    }

    static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Enter a whole number: ");
        }
        int n = sc.nextInt();
        sc.nextLine();
        return n;
    }

    static double readDouble(Scanner sc) {
        while (!sc.hasNextDouble()) {
            sc.next();
            System.out.print("Enter a number: ");
        }
        double n = sc.nextDouble();
        sc.nextLine();
        return n;
    }
}
