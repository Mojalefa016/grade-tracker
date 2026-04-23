package gradetracker.service;

import gradetracker.db.DatabaseManager;
import gradetracker.model.Grade;
import gradetracker.model.Student;

import java.sql.SQLException;
import java.util.List;

public class GradeService {

    private final DatabaseManager db;

    public GradeService(DatabaseManager db) { this.db = db; }

    public Student registerStudent(String number, String firstName, String lastName,
                                   String course, int year) throws SQLException {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name required.");
        if (lastName  == null || lastName.isBlank())  throw new IllegalArgumentException("Last name required.");
        if (year < 1 || year > 7)                     throw new IllegalArgumentException("Year must be between 1 and 7.");
        Student s = new Student(number, firstName.trim(), lastName.trim(), course, year);
        db.addStudent(s);
        return s;
    }

    public List<Student> getAllStudents() throws SQLException { return db.getAllStudents(); }

    public boolean removeStudent(int studentId) throws SQLException { return db.deleteStudent(studentId); }

    public Grade recordGrade(int studentId, String subject, double mark, double maxMark) throws SQLException {
        if (subject == null || subject.isBlank()) throw new IllegalArgumentException("Subject name required.");
        if (mark < 0)       throw new IllegalArgumentException("Mark cannot be negative.");
        if (mark > maxMark) throw new IllegalArgumentException("Mark cannot exceed maximum mark.");
        if (maxMark <= 0)   throw new IllegalArgumentException("Max mark must be greater than 0.");
        Grade g = new Grade(studentId, subject.trim(), mark, maxMark);
        db.addGrade(g);
        return g;
    }

    public List<Grade> getStudentGrades(int studentId) throws SQLException { return db.getGradesByStudent(studentId); }

    public boolean removeGrade(int gradeId) throws SQLException { return db.deleteGrade(gradeId); }

    public void printStudentReport(Student student) throws SQLException {
        List<Grade> grades = db.getGradesByStudent(student.getId());
        double avg         = db.getStudentAverage(student.getId());
        String status      = avg >= 50 ? "PASS" : "FAIL";
        String letter      = avg >= 75 ? "A" : avg >= 60 ? "B" : avg >= 50 ? "C" : "F";

        System.out.println("=".repeat(60));
        System.out.println("  STUDENT RESULT REPORT");
        System.out.println("=".repeat(60));
        System.out.printf("  %-18s %s%n", "Name:",          student.getFullName());
        System.out.printf("  %-18s %s%n", "Student Number:", student.getStudentNumber());
        System.out.printf("  %-18s %s%n", "Course:",         student.getCourse());
        System.out.printf("  %-18s Year %d%n", "Year of Study:", student.getYearOfStudy());
        System.out.println("-".repeat(60));
        System.out.printf("  %-25s %-12s %-8s %-8s%n", "Subject", "Mark", "Grade", "Status");
        System.out.println("-".repeat(60));
        for (Grade g : grades) System.out.printf("  %s%n", g.toString());
        System.out.println("-".repeat(60));
        System.out.printf("  %-25s %5.1f%%  [%s]  %s%n", "OVERALL AVERAGE:", avg, letter, status);
        System.out.println("=".repeat(60));
    }

    public void printClassSummary() throws SQLException {
        List<Student> students = db.getAllStudents();
        double classAvg        = db.getClassAverage();
        int[]  pf              = db.getPassFailCount();

        System.out.println("=".repeat(70));
        System.out.println("  CLASS SUMMARY REPORT");
        System.out.println("=".repeat(70));
        System.out.printf("  %-6s %-20s %-22s %-8s %-6s%n", "ID", "Name", "Course", "Avg %", "Status");
        System.out.println("-".repeat(70));
        for (Student s : students) {
            double avg    = db.getStudentAverage(s.getId());
            String status = avg >= 50 ? "PASS" : "FAIL";
            System.out.printf("  %-6s %-20s %-22s %5.1f%%  %s%n",
                    s.getStudentNumber(), s.getFullName(), s.getCourse(), avg, status);
        }
        System.out.println("-".repeat(70));
        System.out.printf("  Total: %d  |  Passing: %d  |  Failing: %d  |  Class Avg: %.1f%%%n",
                students.size(), pf[0], pf[1], classAvg);
        System.out.println("=".repeat(70));
    }
}
