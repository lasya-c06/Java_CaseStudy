package models;

public class Teacher extends User {
    private String department;
    private String teacherId;

    public Teacher(String username, String name, String department) {
        this(username, name, username, department);
    }

    public Teacher(String username, String name, String teacherId, String department) {
        super(username, name, "Teacher");
        this.teacherId = teacherId;
        this.department = department;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getDepartment() {
        return department;
    }
}
