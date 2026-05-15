package models;

import java.util.ArrayList;
import java.util.List;

public class Marksheet extends Result {
    private Student student;
    private List<Subject> subjects; 
    private String status;
    private String remarks;

    private String institutionName;
    private String institutionAddress;
    private String teacherId;
    private int maxMarksPerSubject;
    private boolean correctionRequested;
    private String correctionDetails;

    public Marksheet(Student student) {
        this.student = student;
        this.subjects = new ArrayList<>();
        this.status = "Generated";
        this.remarks = "";
        this.maxMarksPerSubject = 100; 
    }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getInstitutionAddress() { return institutionAddress; }
    public void setInstitutionAddress(String institutionAddress) { this.institutionAddress = institutionAddress; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public int getMaxMarksPerSubject() { return maxMarksPerSubject; }
    public void setMaxMarksPerSubject(int maxMarksPerSubject) { this.maxMarksPerSubject = maxMarksPerSubject; }

    public boolean isCorrectionRequested() { return correctionRequested; }
    public void setCorrectionRequested(boolean correctionRequested) { this.correctionRequested = correctionRequested; }

    public String getCorrectionDetails() { return correctionDetails; }
    public void setCorrectionDetails(String correctionDetails) { this.correctionDetails = correctionDetails; }

    public void addSubject(Subject subject) {
        this.subjects.add(subject);
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public Student getStudent() {
        return student;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public int calculateTotal() {
        int total = 0;
        for (Subject s : subjects) {
            total += s.getMarks();
        }
        return total;
    }

    @Override
    public String calculateGrade() {
        if (!status.equals("Generated")) {
            return "N/A";
        }

        int totalMarks = calculateTotal();
        int maxMarks = subjects.size() * maxMarksPerSubject;
        if (maxMarks == 0) return "N/A";
        
        double percentage = ((double) totalMarks / maxMarks) * 100;
        
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
}