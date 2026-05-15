package models;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String username;
    private String name;
    private String role;
    private String securityQuestion;
    private String securityAnswer;

    public User(String username, String name, String role) {
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityQuestion() { return securityQuestion; }

    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    public String getSecurityAnswer() { return securityAnswer; }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }
}