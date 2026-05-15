package services;
import models.User;
import models.Teacher;
import models.Student;
import java.util.HashMap;
import java.util.Map;
public class AuthService {
    private static final String LEGACY_SEEDED_TEACHER_USERNAME = "teacher";
    private static final String OLD_SEEDED_TEACHER_USERNAME = "Lasya";

    private Map<String, String> userCredentials;
    private Map<String, User> userDetails;

    public AuthService() {
        userCredentials = new HashMap<>();
        userDetails = new HashMap<>();
        DatabaseManager.loadUsers(userCredentials, userDetails);

        removeDefaultTeacherAccounts();
    }

    private void removeDefaultTeacherAccounts() {
        removeDefaultTeacher(LEGACY_SEEDED_TEACHER_USERNAME);
        removeDefaultTeacher(OLD_SEEDED_TEACHER_USERNAME);
    }

    private void removeDefaultTeacher(String username) {
        User user = userDetails.get(username);
        if (user instanceof Teacher) {
            userCredentials.remove(username);
            userDetails.remove(username);
            DatabaseManager.deleteUser(username);
        }
    }
    public boolean register(String username, String password, String name, String role, String deptId, String securityQuestion, String securityAnswer) {
        if (userCredentials.containsKey(username)) {
            return false; // User already exists
        }
        
        userCredentials.put(username, password);
        User newUser;
        if (role.equals("Teacher")) {
            newUser = new Teacher(username, name, deptId);
        } else {
            newUser = new Student(username, name, username, deptId);
        }
        newUser.setSecurityQuestion(securityQuestion);
        newUser.setSecurityAnswer(securityAnswer);
        userDetails.put(username, newUser);
        
        DatabaseManager.saveUsers(userCredentials, userDetails);
        return true;
    }
    public User login(String username, String password, String role) {
        if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
            User user = userDetails.get(username);
            if (user != null && user.getRole().equalsIgnoreCase(role)) {
                return user;
            }
        }
        return null;
    }

    public boolean resetPassword(String username, String securityAnswer, String newPassword, String role) {
        if (userCredentials.containsKey(username)) {
            User user = userDetails.get(username);
            if (user != null && user.getRole().equalsIgnoreCase(role) && user.getSecurityAnswer() != null && user.getSecurityAnswer().equalsIgnoreCase(securityAnswer)) {
                userCredentials.put(username, newPassword);
                DatabaseManager.saveUsers(userCredentials, userDetails);
                return true;
            }
        }
        return false;
    }
    
    public String getSecurityQuestion(String username, String role) {
        if (userDetails.containsKey(username)) {
            User user = userDetails.get(username);
            if (user.getRole().equalsIgnoreCase(role)) {
                return user.getSecurityQuestion();
            }
        }
        return null;
    }
}
