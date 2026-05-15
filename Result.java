package models;

import java.io.Serializable;

public abstract class Result implements Serializable {
    public abstract int calculateTotal();
    public abstract String calculateGrade();
}