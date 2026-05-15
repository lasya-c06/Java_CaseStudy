package gui;

import services.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainApplication extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private TeacherDashboard teacherDashboard;
    private StudentDashboard studentDashboard;

    public MainApplication() {
        setTitle("Digital Marksheet Generator");
        setSize(900, 600);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseManager.closeDatabase();
                dispose();
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        teacherDashboard = new TeacherDashboard(this);
        studentDashboard = new StudentDashboard(this);

        mainPanel.add(loginPanel, "Login");
        mainPanel.add(teacherDashboard, "TeacherDashboard");
        mainPanel.add(studentDashboard, "StudentDashboard");

        add(mainPanel); 
        showScreen("Login");
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
    }

    public void showTeacherDashboard(String teacherId, String department) {
        boolean success = teacherDashboard.loadData(teacherId, department);
        if (success) {
            cardLayout.show(mainPanel, "TeacherDashboard");
        } else {
            showScreen("Login");
        }
    }

    public void showStudentDashboard(String studentId) {
        studentDashboard.loadStudentData(studentId);
        cardLayout.show(mainPanel, "StudentDashboard");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        new Thread(() -> {
            DatabaseManager.connectDatabase();

            SwingUtilities.invokeLater(() -> {
                new MainApplication().setVisible(true);
            });
        }).start();
    }
}