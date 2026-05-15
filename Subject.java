package models;

import java.io.Serializable;

public class Subject implements Serializable {
    private String subjectName;
    private int marks;
    private int highestMarks;

    public Subject(String subjectName) {
        this.subjectName = subjectName;
        this.marks = 0;
        this.highestMarks = 0;
    }

    public Subject(String subjectName, int marks) {
        this.subjectName = subjectName;
        setMarks(marks);
    }

    public void setMarks(int marks) {
        if (isValidMarks(marks)) {
            this.marks = marks;
        } else {
            throw new IllegalArgumentException("Marks must be >= 0.");
        }
    }

    public boolean isValidMarks(int m) {
        return m >= 0;
    }

    public String getSubjectName() { return subjectName; }
    public int getMarks() { return marks; }
    public int getHighestMarks() { return highestMarks; }
    public void setHighestMarks(int highestMarks) { this.highestMarks = highestMarks; }
}