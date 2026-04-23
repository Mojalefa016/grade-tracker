package gradetracker;

import gradetracker.db.DatabaseManager;
import gradetracker.model.Grade;
import gradetracker.model.Student;
import gradetracker.service.GradeService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static GradeService service;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       GRADETRACK — Result System     ║");
        System.out.println("║        by Mojalefa Mokhampane        ║");
        System.out.println("╚══════════════════════════════════════╝");
        try {
            DatabaseManager db = new DatabaseManager();
            service = new GradeService(db);
            mainMenu();
            db.close();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    static void mainMenu() throws SQLException {
        while (true) {
            System.out.println("\n──────────────────────────────");
            System.out.println("  MAIN MENU");
            System.out.println("──────────────────────────────");
            System.out.println("  1. Add Student");
            System.out.println("  2. Add Grade");
            System.out.println("  3. View Student Report");
            System.out.println("  4. View All Students");
            System.out.println("  5. View Class Summary");
            System.out.println("  6. Delete Student");
            System.out.println("  0. Exit");
            System.out.print("\n  Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addStudentMenu();
                case "2" -> addGradeMenu();
                case "3" -> studentReportMenu();
                case "4" -> listStudentsMenu();
                case "5" -> service.printClassSummary();
                case "6" -> deleteStudentMenu();
                case "0" -> { System.out.println("  Goodbye!"); return; }
                default  -> System.out.println("  Invalid option. Try again.");
            }
        }
    }

    static void addStudentMenu() throws SQLException {
        System.out.println("\n  ── Add New Student ──");
        System.out.print("  Student Number : "); String num = scanner.nextLine().trim();
        System.out.print("  First Name     : "); String fn  = scanner.nextLine().trim();
        System.out.print("  Last Name      : "); String ln  = scanner.nextLine().trim();
        System.out.print("  Course         : "); String crs = scanner.nextLine().trim();
        System.out.print("  Year of Study  : ");
        int yr;
        try { yr = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  Invalid year."); return; }
        Student s = service.registerStudent(num, fn, ln, crs, yr);
        System.out.printf("  ✅ Student added: %s (ID: %d)%n", s.getFullName(), s.getId());
    }

    static void addGradeMenu() throws SQLException {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students found. Add a student first."); return; }
        System.out.println("\n  ── Add Grade ──");
        for (Student s : students) System.out.printf("    [%d] %s (%s)%n", s.getId(), s.getFullName(), s.getStudentNumber());
        System.out.print("  Select student ID: ");
        int sid;
        try { sid = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  Invalid ID."); return; }
        System.out.print("  Subject  : "); String subj = scanner.nextLine().trim();
        System.out.print("  Mark     : ");
        System.out.print("  Max Mark : ");
        double mark, max;
        try {
            mark = Double.parseDouble(scanner.nextLine().trim());
            max  = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) { System.out.println("  Invalid mark."); return; }
        Grade g = service.recordGrade(sid, subj, mark, max);
        System.out.printf("  ✅ Grade saved: %s — %.1f/%.1f (%.1f%%) [%s]%n",
                subj, mark, max, g.getPercentage(), g.getLetterGrade());
    }

    static void studentReportMenu() throws SQLException {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students found."); return; }
        for (Student s : students) System.out.printf("    [%d] %s%n", s.getId(), s.getFullName());
        System.out.print("  Select student ID: ");
        try {
            int sid = Integer.parseInt(scanner.nextLine().trim());
            Student s = students.stream().filter(st -> st.getId() == sid).findFirst().orElse(null);
            if (s == null) { System.out.println("  Student not found."); return; }
            service.printStudentReport(s);
        } catch (NumberFormatException e) { System.out.println("  Invalid ID."); }
    }

    static void listStudentsMenu() throws SQLException {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students registered yet."); return; }
        System.out.println("\n  Registered Students:");
        System.out.println("  " + "-".repeat(55));
        students.forEach(s -> System.out.println("  " + s));
        System.out.println("  " + "-".repeat(55));
        System.out.printf("  Total: %d students%n", students.size());
    }

    static void deleteStudentMenu() throws SQLException {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students found."); return; }
        for (Student s : students) System.out.printf("    [%d] %s%n", s.getId(), s.getFullName());
        System.out.print("  Enter student ID to delete: ");
        try {
            int sid = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("  Are you sure? This deletes all grades too. (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if ("yes".equalsIgnoreCase(confirm)) { service.removeStudent(sid); System.out.println("  ✅ Student deleted."); }
            else System.out.println("  Cancelled.");
        } catch (NumberFormatException e) { System.out.println("  Invalid ID."); }
    }
}
