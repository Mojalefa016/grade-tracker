package gradetracker.model;

public class Student {

    private int    id;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private String course;
    private int    yearOfStudy;

    public Student() {}

    public Student(String studentNumber, String firstName, String lastName,
                   String course, int yearOfStudy) {
        this.studentNumber = studentNumber;
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.course        = course;
        this.yearOfStudy   = yearOfStudy;
    }

    public int    getId()            { return id; }
    public void   setId(int id)      { this.id = id; }

    public String getStudentNumber()                     { return studentNumber; }
    public void   setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getFirstName()                { return firstName; }
    public void   setFirstName(String firstName){ this.firstName = firstName; }

    public String getLastName()               { return lastName; }
    public void   setLastName(String lastName){ this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getCourse()             { return course; }
    public void   setCourse(String course){ this.course = course; }

    public int  getYearOfStudy()         { return yearOfStudy; }
    public void setYearOfStudy(int year) { this.yearOfStudy = year; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s | %s | Year %d",
                studentNumber, firstName, lastName, course, yearOfStudy);
    }
}
