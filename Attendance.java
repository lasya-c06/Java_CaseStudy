package models;

import java.io.Serializable;

public class Attendance implements Serializable {
    private int totalClasses;
    private int attendedClasses;

    public Attendance(int totalClasses, int attendedClasses) {
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
    }

    public double calculatePercentage() {
        if (totalClasses == 0) return 0;
        return ((double) attendedClasses / totalClasses) * 100;
    }

    public boolean isEligible() {
        return calculatePercentage() >= 75.0;
    }

    public int getTotalClasses() { return totalClasses; }
    public int getAttendedClasses() { return attendedClasses; }
}