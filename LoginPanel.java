package gui;
import services.AuthService;
import models.User;
import models.Teacher;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private MainApplication app;
    private AuthService authService;

    public LoginPanel(MainApplication app) {
        this.app = app;
        this.authService = new AuthService();
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(200, 200, 200));

        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setBackground(Color.WHITE);
        innerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), "User Authentication"));
        innerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20),
            innerPanel.getBorder()
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Digital Marksheet Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 22));
        titleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        innerPanel.add(titleLabel, gbc);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        roleLabel.setForeground(Color.BLACK);
        gbc.gridy++; gbc.gridwidth = 1;
        innerPanel.add(roleLabel, gbc);

        String[] roles = {"Teacher", "Student"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(new Font("Tahoma", Font.PLAIN, 13));
        gbc.gridx = 1;
        innerPanel.add(roleCombo, gbc);

        JLabel userLabel = new JLabel("Username / ID:");
        userLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        userLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy++;
        innerPanel.add(userLabel, gbc);

        JTextField userField = new JTextField(15);
        userField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        gbc.gridx = 1;
        innerPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        passLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy++;
        innerPanel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        gbc.gridx = 1;
        innerPanel.add(passField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Verdana", Font.BOLD, 14));
        loginBtn.setBackground(new Color(210, 210, 210));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setOpaque(true);

        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.setFont(new Font("Verdana", Font.BOLD, 14));
        signUpBtn.setBackground(new Color(210, 210, 210));
        signUpBtn.setForeground(Color.BLACK);
        signUpBtn.setBorderPainted(false);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setOpaque(true);

        btnPanel.add(loginBtn);
        btnPanel.add(signUpBtn);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        innerPanel.add(btnPanel, gbc);

        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotBtn.setBackground(Color.WHITE);
        forgotBtn.setForeground(new Color(0, 122, 204));
        forgotBtn.setBorderPainted(false);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy++;
        innerPanel.add(forgotBtn, gbc);

        // ✅ loginBtn listener - properly closed with });
        loginBtn.addActionListener(e -> {
            String role = (String) roleCombo.getSelectedItem();
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            User loggedIn = authService.login(user, pass, role);
            if (loggedIn != null) {
                if (role.equals("Teacher")) {
                    Teacher teacher = (Teacher) loggedIn;
                    app.showTeacherDashboard(teacher.getTeacherId(), teacher.getDepartment());
                } else {
                    app.showStudentDashboard(loggedIn.getUsername());
                }
                userField.setText("");
                passField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }); // ✅ loginBtn listener ends here

        // ✅ These three lines are now OUTSIDE the loginBtn lambda
        signUpBtn.addActionListener(e -> showSignUpDialog());
        forgotBtn.addActionListener(e -> showForgotDialog());
        add(innerPanel);
    }

    private void showSignUpDialog() {
        JDialog dialog = new JDialog(app, "Sign Up", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel roleL = new JLabel("Role:");
        JComboBox<String> roleC = new JComboBox<>(new String[]{"Teacher", "Student"});

        JLabel nameL = new JLabel("Full Name:");
        JTextField nameF = new JTextField(15);

        JLabel userL = new JLabel("Username / ID:");
        JTextField userF = new JTextField(15);

        JLabel deptL = new JLabel("Department:");
        JTextField deptF = new JTextField(15);

        JLabel secQL = new JLabel("Security Question:");
        JComboBox<String> secQC = new JComboBox<>(new String[]{
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What city were you born in?",
            "What is your favorite color?"
        });

        JLabel secAL = new JLabel("Security Answer:");
        JTextField secAF = new JTextField(15);

        JLabel passL = new JLabel("Password:");
        JPasswordField passF = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(roleL, gbc);
        gbc.gridx = 1; dialog.add(roleC, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(nameL, gbc);
        gbc.gridx = 1; dialog.add(nameF, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(userL, gbc);
        gbc.gridx = 1; dialog.add(userF, gbc);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(deptL, gbc);
        gbc.gridx = 1; dialog.add(deptF, gbc);

        gbc.gridx = 0; gbc.gridy = 4; dialog.add(secQL, gbc);
        gbc.gridx = 1; dialog.add(secQC, gbc);

        gbc.gridx = 0; gbc.gridy = 5; dialog.add(secAL, gbc);
        gbc.gridx = 1; dialog.add(secAF, gbc);

        gbc.gridx = 0; gbc.gridy = 6; dialog.add(passL, gbc);
        gbc.gridx = 1; dialog.add(passF, gbc);

        JButton regBtn = new JButton("Register");
        regBtn.setFont(new Font("Verdana", Font.BOLD, 13));
        regBtn.setBackground(new Color(210, 210, 210));
        regBtn.setForeground(Color.BLACK);
        regBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        regBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        dialog.add(regBtn, gbc);

        regBtn.addActionListener(e -> {
            String role = (String) roleC.getSelectedItem();
            String name = nameF.getText().trim();
            String user = userF.getText().trim();
            String dept = deptF.getText().trim();
            String secQ = (String) secQC.getSelectedItem();
            String secA = secAF.getText().trim();
            String pass = new String(passF.getPassword());

            if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || secA.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = authService.register(user, pass, name, role, dept, secQ, secA);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Username/ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void showForgotDialog() {
        JDialog dialog = new JDialog(app, "Reset Password", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel roleL = new JLabel("Role:");
        JComboBox<String> roleC = new JComboBox<>(new String[]{"Teacher", "Student"});

        JLabel userL = new JLabel("Username / ID:");
        JTextField userF = new JTextField(15);
        JButton getQBtn = new JButton("Get Question");
        getQBtn.setMargin(new Insets(2, 5, 2, 5));

        JPanel userPanel = new JPanel(new BorderLayout(5, 0));
        userPanel.setBackground(Color.WHITE);
        userPanel.add(userF, BorderLayout.CENTER);
        userPanel.add(getQBtn, BorderLayout.EAST);

        JLabel secQL = new JLabel("Security Question:");
        JTextField secQF = new JTextField(15);
        secQF.setEditable(false);
        secQF.setBackground(new Color(240, 240, 240));

        JLabel secAL = new JLabel("Answer:");
        JTextField secAF = new JTextField(15);

        JLabel passL = new JLabel("New Password:");
        JPasswordField passF = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(roleL, gbc);
        gbc.gridx = 1; dialog.add(roleC, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(userL, gbc);
        gbc.gridx = 1; dialog.add(userPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(secQL, gbc);
        gbc.gridx = 1; dialog.add(secQF, gbc);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(secAL, gbc);
        gbc.gridx = 1; dialog.add(secAF, gbc);

        gbc.gridx = 0; gbc.gridy = 4; dialog.add(passL, gbc);
        gbc.gridx = 1; dialog.add(passF, gbc);

        JButton resetBtn = new JButton("Reset Password");
        resetBtn.setFont(new Font("Verdana", Font.BOLD, 13));
        resetBtn.setBackground(new Color(255, 255, 255));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        resetBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        dialog.add(resetBtn, gbc);

        getQBtn.addActionListener(e -> {
            String role = (String) roleC.getSelectedItem();
            String user = userF.getText().trim();
            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sq = authService.getSecurityQuestion(user, role);
            if (sq != null) {
                secQF.setText(sq);
            } else {
                secQF.setText("");
                JOptionPane.showMessageDialog(dialog, "User not found or role mismatch.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        resetBtn.addActionListener(e -> {
            String role = (String) roleC.getSelectedItem();
            String user = userF.getText().trim();
            String secA = secAF.getText().trim();
            String pass = new String(passF.getPassword());

            if (user.isEmpty() || pass.isEmpty() || secA.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = authService.resetPassword(user, secA, pass, role);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Password reset successful! You can now login with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Username/ID not found or role mismatch.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}