package gui;

import models.Marksheet;
import models.Subject;
import services.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;

public class StudentDashboard extends JPanel {
    private MainApplication app;
    private JLabel nameLabel, idLabel, deptLabel, attLabel, statusLabel, gradeLabel, totalLabel;
    private JLabel instNameLabel, instAddrLabel, teacherLabel;
    private JPanel marksPanel;
    private Marksheet currentMarksheet;
    private JPanel centerPanel;

    public StudentDashboard(MainApplication app) {
        this.app = app;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(200, 200, 200)); // Grey desktop background

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        topPanel.setBackground(new Color(180, 180, 180));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Verdana", Font.BOLD, 13));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setBackground(new Color(220, 220, 220));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> app.showScreen("Login"));
        topPanel.add(logoutBtn);
        add(topPanel, BorderLayout.NORTH);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        centerPanel.setMaximumSize(new Dimension(620, Integer.MAX_VALUE));
       
        JPanel a4Outer = new JPanel();
        a4Outer.setLayout(new BoxLayout(a4Outer, BoxLayout.X_AXIS));
        a4Outer.setBackground(new Color(200, 200, 200));
        a4Outer.add(Box.createHorizontalGlue());
        a4Outer.add(centerPanel);
        a4Outer.add(Box.createHorizontalGlue());

        JPanel a4Wrapper = new JPanel(new BorderLayout());
        a4Wrapper.setBackground(new Color(200, 200, 200));
        a4Wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        a4Wrapper.add(a4Outer, BorderLayout.CENTER);

        JPanel topInstPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        topInstPanel.setBackground(Color.WHITE);
        topInstPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        try {
            File logoFile = new File("res/logo.png");
            if (logoFile.exists()) {
                Image logoImg = ImageIO.read(logoFile).getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                topInstPanel.add(new JLabel(new ImageIcon(logoImg)));
            }
        } catch (Exception e) {}

        JPanel textInstPanel = new JPanel();
        textInstPanel.setLayout(new BoxLayout(textInstPanel, BoxLayout.Y_AXIS));
        textInstPanel.setBackground(Color.WHITE);

        instNameLabel = new JLabel("", SwingConstants.CENTER);
        instNameLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        instNameLabel.setForeground(Color.BLACK);
        instNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        instAddrLabel = new JLabel("", SwingConstants.CENTER);
        instAddrLabel.setFont(new Font("Georgia", Font.ITALIC, 13));
        instAddrLabel.setForeground(Color.BLACK);
        instAddrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textInstPanel.add(instNameLabel);
        textInstPanel.add(instAddrLabel);
        topInstPanel.add(textInstPanel);

        centerPanel.add(topInstPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setForeground(Color.DARK_GRAY);
        centerPanel.add(sep);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel titleLabel = new JLabel("STUDENT MARKSHEET", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel detailCard = new JPanel(new GridLayout(0, 2, 8, 8));
        detailCard.setBackground(Color.WHITE);
        detailCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        detailCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "Student Details",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 12), Color.BLACK),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        nameLabel   = makeDetailLabel();
        idLabel     = makeDetailLabel();
        deptLabel   = makeDetailLabel();
        attLabel    = makeDetailLabel();
        statusLabel = makeDetailLabel();

        detailCard.add(makeKeyLabel("Name:"));         detailCard.add(nameLabel);
        detailCard.add(makeKeyLabel("Student ID:"));   detailCard.add(idLabel);
        detailCard.add(makeKeyLabel("Department:"));   detailCard.add(deptLabel);
        detailCard.add(makeKeyLabel("Attendance:"));   detailCard.add(attLabel);
        detailCard.add(makeKeyLabel("Status:"));       detailCard.add(statusLabel);

        centerPanel.add(detailCard);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        marksPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        marksPanel.setBackground(Color.WHITE);
        marksPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        marksPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "Academic Performance",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 12), Color.BLACK),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        centerPanel.add(marksPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel bottomCard = new JPanel(new BorderLayout(5, 5));
        bottomCard.setBackground(Color.WHITE);
        bottomCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel totalsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        totalsPanel.setBackground(Color.WHITE);

        totalLabel = new JLabel();
        gradeLabel = new JLabel();
        totalLabel.setFont(new Font("Verdana", Font.BOLD, 15));
        totalLabel.setForeground(Color.BLACK);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gradeLabel.setFont(new Font("Verdana", Font.BOLD, 15));
        gradeLabel.setForeground(Color.BLACK);
        gradeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        totalsPanel.add(totalLabel);
        totalsPanel.add(gradeLabel);

        teacherLabel = new JLabel();
        teacherLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
        teacherLabel.setForeground(Color.BLACK);

        bottomCard.add(totalsPanel, BorderLayout.EAST);

        JPanel signaturePanel = new JPanel(new BorderLayout(0, 5));
        signaturePanel.setBackground(Color.WHITE);
        signaturePanel.add(teacherLabel, BorderLayout.NORTH);

        JLabel sigImage;
        try {
            File sigFile = new File("res/signature.png");
            if (sigFile.exists()) {
                Image sImg = ImageIO.read(sigFile).getScaledInstance(150, 50, Image.SCALE_SMOOTH);
                sigImage = new JLabel(new ImageIcon(sImg));
            } else {
                sigImage = new JLabel("<Digital Signature>");
                sigImage.setFont(new Font("Brush Script MT", Font.ITALIC, 18));
                sigImage.setForeground(Color.BLACK);
            }
        } catch (Exception e) {
            sigImage = new JLabel("<Digital Signature>");
            sigImage.setFont(new Font("Brush Script MT", Font.ITALIC, 18));
            sigImage.setForeground(Color.BLACK);
        }
        signaturePanel.add(sigImage, BorderLayout.SOUTH);
        bottomCard.add(signaturePanel, BorderLayout.WEST);

        centerPanel.add(bottomCard);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        actionsPanel.setBackground(new Color(180, 180, 180));
        actionsPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(150, 150, 150)));

        JButton correctionBtn = new JButton("\u26A0 Report Error");
        correctionBtn.setFont(new Font("Verdana", Font.BOLD, 16));
        correctionBtn.setForeground(Color.BLACK);
        correctionBtn.setBackground(new Color(173, 216, 230)); // light blue
        correctionBtn.setBorderPainted(false);
        correctionBtn.setFocusPainted(false);
        correctionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton downloadBtn = new JButton("\u2B07 Download Marksheet");
        downloadBtn.setFont(new Font("Verdana", Font.BOLD, 16));
        downloadBtn.setForeground(Color.BLACK);
        downloadBtn.setBackground(new Color(180, 235, 250)); // light teal-blue
        downloadBtn.setBorderPainted(false);
        downloadBtn.setFocusPainted(false);
        downloadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        correctionBtn.addActionListener(e -> {
            if (currentMarksheet == null || !currentMarksheet.getStatus().equals("Generated")) {
                JOptionPane.showMessageDialog(this, "No valid marksheet to report error.");
                return;
            }
            if (currentMarksheet.isCorrectionRequested()) {
                JOptionPane.showMessageDialog(this, "Correction request already pending.");
                return;
            }
            String reason = JOptionPane.showInputDialog(this, "Enter reason for correction request:");
            if (reason != null && !reason.trim().isEmpty()) {
                currentMarksheet.setCorrectionRequested(true);
                currentMarksheet.setCorrectionDetails(reason);
                List<Marksheet> all = DatabaseManager.readMarksheet();
                for (int i = 0; i < all.size(); i++) {
                    if (all.get(i).getStudent().getStudentId().equals(currentMarksheet.getStudent().getStudentId())) {
                        all.set(i, currentMarksheet);
                        break;
                    }
                }
                DatabaseManager.saveMarksheet(all);
                JOptionPane.showMessageDialog(this, "Request submitted to teacher.");
            }
        });

        downloadBtn.addActionListener(e -> {
            try {
                BufferedImage image = new BufferedImage(centerPanel.getWidth(), centerPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                centerPanel.paint(g2);
                g2.dispose();
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) file = new File(file.getAbsolutePath() + ".png");
                    ImageIO.write(image, "png", file);
                    JOptionPane.showMessageDialog(this, "Marksheet saved successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
            }
        });

        actionsPanel.add(correctionBtn);
        actionsPanel.add(downloadBtn);

        JScrollPane scrollPane = new JScrollPane(a4Wrapper);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(200, 200, 200));
        add(scrollPane, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);
    }

    private JLabel makeKeyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 13));
        lbl.setForeground(Color.BLACK);
        return lbl;
    }

    private JLabel makeDetailLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lbl.setForeground(Color.BLACK);
        return lbl;
    }

    public void loadStudentData(String studentId) {
        List<Marksheet> marksheets = DatabaseManager.readMarksheet();
        Marksheet target = null;
        for (Marksheet ms : marksheets) {
            if (ms.getStudent().getStudentId().equals(studentId)) {
                target = ms;
                break;
            }
        }

        if (target != null) {
            currentMarksheet = target;
            instNameLabel.setText(target.getInstitutionName() != null ? target.getInstitutionName() : "");
            instAddrLabel.setText(target.getInstitutionAddress() != null ? target.getInstitutionAddress() : "");
            teacherLabel.setText("Teacher ID: " + (target.getTeacherId() != null ? target.getTeacherId() : "N/A"));

            nameLabel.setText(target.getStudent().getName());
            idLabel.setText(target.getStudent().getStudentId());
            deptLabel.setText(target.getStudent().getDepartment());
            
            if (target.getStudent().getAttendance() != null) {
                double pct = target.getStudent().getAttendance().calculatePercentage();
                attLabel.setText(String.format("%.1f%%", pct));
            } else {
                attLabel.setText("N/A");
            }
            
            statusLabel.setText(target.getStatus());

            marksPanel.removeAll();
            if (target.getStatus().equals("Generated")) {
                marksPanel.add(makeColHeader("Subject",          SwingConstants.LEFT));
                marksPanel.add(makeColHeader("Marks Obtained",   SwingConstants.CENTER));
                marksPanel.add(makeColHeader("Class Average",    SwingConstants.CENTER));
                marksPanel.add(makeColHeader("Highest in Class", SwingConstants.RIGHT));

                for (int sIndex = 0; sIndex < target.getSubjects().size(); sIndex++) {
                    Subject sub = target.getSubjects().get(sIndex);

                    int totalSum = 0;
                    int validCount = 0;
                    for (Marksheet ms : marksheets) {
                        if (ms.getTeacherId() != null && ms.getTeacherId().equals(target.getTeacherId()) && ms.getStatus().equals("Generated")) {
                            if (sIndex < ms.getSubjects().size() && ms.getSubjects().get(sIndex).getSubjectName().equals(sub.getSubjectName())) {
                                totalSum += ms.getSubjects().get(sIndex).getMarks();
                                validCount++;
                            }
                        }
                    }
                    double avg = validCount > 0 ? (double) totalSum / validCount : 0;

                    marksPanel.add(makeColCell(sub.getSubjectName(),                   SwingConstants.LEFT));
                    marksPanel.add(makeColCell(String.valueOf(sub.getMarks()),          SwingConstants.CENTER));
                    marksPanel.add(makeColCell(String.format("%.1f", avg),             SwingConstants.CENTER));
                    marksPanel.add(makeColCell(String.valueOf(sub.getHighestMarks()),   SwingConstants.RIGHT));
                }
                int maxTotal = target.getSubjects().size() * target.getMaxMarksPerSubject();
                totalLabel.setText("Total Score: " + target.calculateTotal() + " / " + maxTotal);
                gradeLabel.setText("Final Grade: " + target.calculateGrade());
            } else {
                JLabel na = new JLabel("Marksheet Not Available");
                na.setFont(new Font("Tahoma", Font.ITALIC, 13));
                na.setForeground(Color.DARK_GRAY);
                marksPanel.add(na);
                totalLabel.setText("");
                gradeLabel.setText("");
            }
            marksPanel.revalidate();
            marksPanel.repaint();
        } else {
            currentMarksheet = null;
            instNameLabel.setText("");
            instAddrLabel.setText("");
            teacherLabel.setText("");
            nameLabel.setText("Not Found");
            idLabel.setText(studentId);
            deptLabel.setText("");
            attLabel.setText("");
            statusLabel.setText("");
            marksPanel.removeAll();
            totalLabel.setText("");
            gradeLabel.setText("");
        }
    }
    private JLabel makeColHeader(String text, int align) {
        JLabel lbl = new JLabel(text, align);
        lbl.setFont(new Font("Verdana", Font.BOLD, 12));
        lbl.setForeground(Color.BLACK);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        return lbl;
    }

    private JLabel makeColCell(String text, int align) {
        JLabel lbl = new JLabel(text, align);
        lbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lbl.setForeground(Color.BLACK);
        return lbl;
    }
}
