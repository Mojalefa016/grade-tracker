package gradetracker.db;

import gradetracker.model.Grade;
import gradetracker.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:gradetracker.db";
    private Connection connection;

    public DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTables();
    }

    private void createTables() throws SQLException {
        String createStudents = """
                CREATE TABLE IF NOT EXISTS students (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_number TEXT    NOT NULL UNIQUE,
                    first_name     TEXT    NOT NULL,
                    last_name      TEXT    NOT NULL,
                    course         TEXT,
                    year_of_study  INTEGER DEFAULT 1,
                    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
        String createGrades = """
                CREATE TABLE IF NOT EXISTS grades (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id  INTEGER NOT NULL,
                    subject     TEXT    NOT NULL,
                    mark        REAL    NOT NULL,
                    max_mark    REAL    NOT NULL DEFAULT 100,
                    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createStudents);
            stmt.execute(createGrades);
        }
    }

    public int addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_number, first_name, last_name, course, year_of_study) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentNumber());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getCourse());
            ps.setInt(5,    student.getYearOfStudy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) { int id = keys.getInt(1); student.setId(id); return id; }
        }
        return -1;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapStudent(rs));
        }
        return list;
    }

    public Student findByStudentNumber(String number) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, number);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapStudent(rs);
        }
        return null;
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    public int addGrade(Grade grade) throws SQLException {
        String sql = "INSERT INTO grades (student_id, subject, mark, max_mark) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    grade.getStudentId());
            ps.setString(2, grade.getSubject());
            ps.setDouble(3, grade.getMark());
            ps.setDouble(4, grade.getMaxMark());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) { int id = keys.getInt(1); grade.setId(id); return id; }
        }
        return -1;
    }

    public List<Grade> getGradesByStudent(int studentId) throws SQLException {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE student_id = ? ORDER BY subject";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapGrade(rs));
        }
        return list;
    }

    public double getStudentAverage(int studentId) throws SQLException {
        String sql = "SELECT AVG((mark / max_mark) * 100) AS avg FROM grades WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("avg");
        }
        return 0.0;
    }

    public boolean deleteGrade(int gradeId) throws SQLException {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            return ps.executeUpdate() > 0;
        }
    }

    public double getClassAverage() throws SQLException {
        String sql = "SELECT AVG((mark / max_mark) * 100) AS avg FROM grades";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("avg");
        }
        return 0.0;
    }

    public int[] getPassFailCount() throws SQLException {
        String sql = """
                SELECT
                    COUNT(CASE WHEN avg_pct >= 50 THEN 1 END) AS passing,
                    COUNT(CASE WHEN avg_pct <  50 THEN 1 END) AS failing
                FROM (
                    SELECT student_id, AVG((mark / max_mark) * 100) AS avg_pct
                    FROM grades GROUP BY student_id
                )
                """;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return new int[]{ rs.getInt("passing"), rs.getInt("failing") };
        }
        return new int[]{0, 0};
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setStudentNumber(rs.getString("student_number"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setCourse(rs.getString("course"));
        s.setYearOfStudy(rs.getInt("year_of_study"));
        return s;
    }

    private Grade mapGrade(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setId(rs.getInt("id"));
        g.setStudentId(rs.getInt("student_id"));
        g.setSubject(rs.getString("subject"));
        g.setMark(rs.getDouble("mark"));
        g.setMaxMark(rs.getDouble("max_mark"));
        return g;
    }

    public void close() {
        try { if (connection != null) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
