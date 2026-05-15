package models;

public class Student extends User {
    private String studentId;
    private String department;
    private Attendance attendance; 

    public Student(String username, String name, String studentId, String department) {
        super(username, name, "Student");
        this.studentId = studentId;
        this.department = department;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
}