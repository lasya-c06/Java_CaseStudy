package gui;

import models.Attendance;
import models.Marksheet;
import models.Student;
import models.Subject;
import services.DatabaseManager;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeacherDashboard extends JPanel {
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_GENERATED = "Generated";
    private static final String STATUS_NOT_ELIGIBLE = "Not Eligible";
    private static final String STATUS_ABSENT = "Absent";
    private static final String STATUS_INVALID_MARKS = "Invalid Marks Entered";
    private static final String STATUS_INVALID_DUPLICATE_ID = "Invalid/Duplicate ID";

    private MainApplication app;
    private JTable table;
    private DefaultTableModel tableModel;

    private String institutionName = "Institution Name";
    private String institutionAddress = "Institution Address";
    private String teacherId = "";
    private String teacherDepartment = "";
    private int defaultTotalClasses = 0;
    private int maxMarksPerSubject = 100;
    private boolean isMarksEditable = false;
    private boolean isTotalClassesEditable = false;
    private boolean isAllLocked = false;
    private int editableRow = -1; 
    
    private java.util.Map<String, String> correctionRequests = new java.util.HashMap<>();

    private List<String> currentSubjects = new ArrayList<>();

    public TeacherDashboard(MainApplication app) {
        this.app = app;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(200, 200, 200)); // Grey background

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(200, 200, 200));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel title = new JLabel("Teacher Dashboard - Manage Students");
        title.setFont(new Font("Verdana", Font.BOLD, 20));
        title.setForeground(Color.BLACK);
        topPanel.add(title, BorderLayout.WEST);
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Verdana", Font.BOLD, 13));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setBackground(new Color(220, 220, 220));
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> app.showScreen("Login"));
        topPanel.add(logoutBtn, BorderLayout.EAST);

        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (isAllLocked) return false;
                if (row != editableRow) return false;            
                if (column == getColumnCount() - 1) return false; 
                if (column == 2) {                               
                    return isTotalClassesEditable;
                }
                return isMarksEditable;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 13));
        table.getTableHeader().setForeground(Color.BLACK);
        registerSubjectHeaderEditor();
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    try {
                        String id = (String) table.getModel().getValueAt(row, 1);
                        String status = (String) table.getModel().getValueAt(row, table.getModel().getColumnCount() - 1);
                        int total = Integer.parseInt(table.getModel().getValueAt(row, 2).toString());
                        int attended = Integer.parseInt(table.getModel().getValueAt(row, 3).toString());
                        
                        if (total > 0 && ((double) attended / total) * 100 < 75.0) {
                            c.setBackground(new Color(255, 200, 200)); 
                        } else if (correctionRequests.containsKey(id)) {
                            c.setBackground(new Color(255, 250, 205)); 
                        } else if (STATUS_GENERATED.equals(status)) {
                            c.setBackground(new Color(200, 255, 200)); 
                        } else if (STATUS_INVALID_MARKS.equals(status) || STATUS_INVALID_DUPLICATE_ID.equals(status)) {
                            c.setBackground(new Color(255, 220, 180)); 
                        } else if (STATUS_ABSENT.equals(status)) {
                            c.setBackground(new Color(230, 230, 230)); 
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selected = table.getSelectedRow();
                if (selected >= 0) {
                    editableRow = selected;
                } else {
                    editableRow = -1;
                }
                table.repaint();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(200, 200, 200));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 20, 10, 20),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                "Student Data Entry",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 12), Color.BLACK)
        ));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(200, 200, 200));
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            "Actions",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Verdana", Font.BOLD, 12), Color.BLACK));
        
        JButton addBtn = createStyledButton("Add Row");
        addBtn.setForeground(Color.BLACK);

        JButton delBtn = createStyledButton("Delete Row");
        delBtn.setForeground(Color.BLACK);

        JButton editBtn = createStyledButton("Edit \u25BC");
        JButton statsBtn = createStyledButton("Class Statistics");
        JButton requestsBtn = createStyledButton("View Requests");
        JButton genBtn = createStyledButton("Generate Marksheet");
        JButton viewBtn = createStyledButton("View Marksheet");
        JButton saveBtn = createStyledButton("Save All");

        bottomPanel.add(addBtn);
        bottomPanel.add(delBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(statsBtn);
        bottomPanel.add(requestsBtn);
        bottomPanel.add(genBtn);
        bottomPanel.add(viewBtn);
        bottomPanel.add(saveBtn);

        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(new Color(200, 200, 200));
        bottomWrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JScrollPane actionsScroll = new JScrollPane(bottomPanel,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        actionsScroll.setBorder(null);
        actionsScroll.setBackground(new Color(200, 200, 200));
        actionsScroll.getViewport().setBackground(new Color(200, 200, 200));
        
        bottomWrapper.add(actionsScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomWrapper, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            Object[] rowData = new Object[tableModel.getColumnCount()];
            for(int i=0; i<rowData.length; i++) rowData[i] = "";
            rowData[2] = defaultTotalClasses; 
            rowData[3] = 0; 
            for (int i=0; i<currentSubjects.size(); i++) {
                rowData[4+i] = 0; 
            }
            rowData[rowData.length - 1] = STATUS_PENDING;
            tableModel.addRow(rowData);
        });
        
        delBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this row?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            }
        });

        JPopupMenu editMenu = new JPopupMenu();
        JMenuItem editTotalItem = new JMenuItem("Total Classes");
        JMenuItem editMarksItem = new JMenuItem("Others");
        editMenu.add(editTotalItem);
        editMenu.add(editMarksItem);

        editBtn.addActionListener(e -> {
            editMenu.show(editBtn, 0, editBtn.getHeight());
        });

        editMarksItem.addActionListener(e -> {
            isAllLocked = false;
            isMarksEditable = true;
            isTotalClassesEditable = false;
            setEditableRowFromSelection();
            tableModel.fireTableDataChanged();
            JOptionPane.showMessageDialog(this, "All columns except Total Classes are now unlocked for editing. Select a row to edit it.", "Edit Others", JOptionPane.INFORMATION_MESSAGE);
        });

        editTotalItem.addActionListener(e -> {
            isAllLocked = false;
            isTotalClassesEditable = true;
            isMarksEditable = false;
            setEditableRowFromSelection();
            tableModel.fireTableDataChanged();
            JOptionPane.showMessageDialog(this, "Total Classes column is now unlocked for editing. Select a row to edit it.", "Edit Total Classes", JOptionPane.INFORMATION_MESSAGE);
        });
        
        statsBtn.addActionListener(e -> showStatistics());
        
        requestsBtn.addActionListener(e -> showCorrectionRequests());

        genBtn.addActionListener(e -> generateMarksheets());
        
        viewBtn.addActionListener(e -> viewMarksheet());
        
        saveBtn.addActionListener(e -> saveAll());

        tableModel.addTableModelListener(new TableModelListener() {
            private boolean isUpdating = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdating) return;
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                if (e.getType() == TableModelEvent.UPDATE && row >= 0) {
                    if (col == 3) { 
                        try {
                            int totalClasses = Integer.parseInt(tableModel.getValueAt(row, 2).toString());
                            validateAttendedClasses(row, totalClasses);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            private void validateAttendedClasses(int row, int totalClasses) {
                try {
                    int attended = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
                    if (attended > totalClasses) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(TeacherDashboard.this, 
                                "Attended classes cannot be greater than Total classes.", 
                                "Invalid Input", JOptionPane.WARNING_MESSAGE);
                        });
                        isUpdating = true;
                        tableModel.setValueAt(totalClasses, row, 3);
                        isUpdating = false;
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        updateTableColumns();
    }

    private void updateTableColumns() {
        List<String> cols = new ArrayList<>();
        cols.addAll(List.of("Name", "Student ID", "Total Classes", "Attended"));
        cols.addAll(currentSubjects);
        cols.add("Status");
        
        tableModel.setColumnIdentifiers(cols.toArray());
        tableModel.setRowCount(0);
    }

    private void registerSubjectHeaderEditor() {
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = table.columnAtPoint(e.getPoint());
                if (viewColumn < 0) {
                    return;
                }

                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                int subjectIndex = modelColumn - 4;
                if (subjectIndex >= 0 && subjectIndex < currentSubjects.size()) {
                    editSubjectName(subjectIndex);
                }
            }
        });
    }

    private void editSubjectName(int subjectIndex) {
        String currentName = currentSubjects.get(subjectIndex);
        String newName = JOptionPane.showInputDialog(this, "Edit subject name:", currentName);
        if (newName == null) {
            return;
        }

        newName = newName.trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject name cannot be empty.");
            return;
        }

        currentSubjects.set(subjectIndex, newName);
        refreshTableColumnsKeepingRows();
        saveDepartmentSettings();
    }

    private void refreshTableColumnsKeepingRows() {
        List<Object[]> rows = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Object[] rowData = new Object[tableModel.getColumnCount()];
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                rowData[col] = tableModel.getValueAt(row, col);
            }
            rows.add(rowData);
        }

        updateTableColumns();
        for (Object[] rowData : rows) {
            tableModel.addRow(rowData);
        }
    }

    private void saveDepartmentSettings() {
        DatabaseManager.saveDepartmentSettings(teacherDepartment, currentSubjects, defaultTotalClasses);
    }

    private List<String> findSubjectsSavedForDepartment(List<Marksheet> saved, String department) {
        for (Marksheet ms : saved) {
            if (ms.getStudent() != null
                    && department != null
                    && department.equalsIgnoreCase(ms.getStudent().getDepartment())
                    && !ms.getSubjects().isEmpty()) {
                List<String> subjects = new ArrayList<>();
                for (Subject sub : ms.getSubjects()) {
                    subjects.add(sub.getSubjectName());
                }
                return subjects;
            }
        }
        return new ArrayList<>();
    }



    private boolean promptForInitialSubjects() {
        while (currentSubjects.isEmpty()) {
            String input = JOptionPane.showInputDialog(this, "Welcome! Please enter the subject names for your department separated by commas:");
            if (input == null) {
                return false; 
            }
            if (!input.trim().isEmpty()) {
                String[] subs = input.split(",");
                for (String s : subs) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        String titleCased = java.util.Arrays.stream(s.split("\\s+"))
                            .map(w -> w.isEmpty() ? "" : Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                            .collect(java.util.stream.Collectors.joining(" "));
                        currentSubjects.add(titleCased);
                    }
                }
            }
            if (currentSubjects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must enter at least one subject to proceed.");
            }
        }
        
        while (defaultTotalClasses <= 0) {
            String totalInput = JOptionPane.showInputDialog(this, "Enter Total Classes for the semester:");
            if (totalInput == null) {
                currentSubjects.clear(); 
                return false;
            }
            if (!totalInput.trim().isEmpty()) {
                try {
                    defaultTotalClasses = Integer.parseInt(totalInput.trim());
                    if (defaultTotalClasses <= 0) {
                        JOptionPane.showMessageDialog(this, "Total Classes must be greater than zero.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "You must enter Total Classes to proceed.");
            }
        }
        updateTableColumns();
        saveDepartmentSettings();
        return true;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Verdana", Font.BOLD, 15));
        btn.setBackground(new Color(210, 210, 210)); 
        btn.setForeground(Color.BLACK);
        btn.setMargin(new Insets(6, 14, 6, 14));
        btn.setBorderPainted(false); 
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private void setEditableRowFromSelection() {
        int selected = table.getSelectedRow();
        editableRow = selected >= 0 ? table.convertRowIndexToModel(selected) : -1;
    }

    private String cellText(int row, int column) {
        Object value = tableModel.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private boolean isBlankCell(int row, int column) {
        return cellText(row, column).isEmpty();
    }

    private Integer parseValidMarks(int row, int subjectIndex) {
        String text = cellText(row, 4 + subjectIndex);
        if (text.isEmpty()) {
            return null;
        }

        try {
            int marks = Integer.parseInt(text);
            if (marks < 0 || marks > maxMarksPerSubject) {
                return null;
            }
            return marks;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean hasBlankMarks(int row) {
        for (int j = 0; j < currentSubjects.size(); j++) {
            if (isBlankCell(row, 4 + j)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInvalidMarks(int row) {
        for (int j = 0; j < currentSubjects.size(); j++) {
            String text = cellText(row, 4 + j);
            if (text.isEmpty()) {
                continue;
            }

            try {
                int marks = Integer.parseInt(text);
                if (marks < 0 || marks > maxMarksPerSubject) {
                    return true;
                }
            } catch (NumberFormatException ex) {
                return true;
            }
        }
        return false;
    }

    private Set<Integer> findInvalidOrDuplicateIdRows() {
        Map<String, Integer> firstRowsById = new HashMap<>();
        Set<Integer> invalidRows = new HashSet<>();

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String id = cellText(row, 1);
            if (id.isEmpty()) {
                invalidRows.add(row);
                continue;
            }

            String key = id.toLowerCase();
            Integer firstRow = firstRowsById.putIfAbsent(key, row);
            if (firstRow != null) {
                invalidRows.add(firstRow);
                invalidRows.add(row);
            }
        }

        return invalidRows;
    }

    private String validateRowForMarksheet(int row, Set<Integer> invalidIdRows) {
        if (invalidIdRows.contains(row)) {
            return STATUS_INVALID_DUPLICATE_ID;
        }
        if (hasInvalidMarks(row)) {
            return STATUS_INVALID_MARKS;
        }
        if (hasBlankMarks(row)) {
            return STATUS_ABSENT;
        }
        return "";
    }

    public boolean loadData(String teacherId, String department) {
        this.teacherDepartment = department;
        this.teacherId = teacherId;
        this.defaultTotalClasses = DatabaseManager.getDepartmentTotalClasses(department);
        this.isMarksEditable = false;
        this.isTotalClassesEditable = false;
        this.isAllLocked = false;
        this.editableRow = -1;
        this.correctionRequests.clear();
        this.currentSubjects.clear();

        List<Marksheet> saved = DatabaseManager.readMarksheet();
        tableModel.setRowCount(0);

        currentSubjects.addAll(DatabaseManager.getDepartmentSubjects(department));
        if (currentSubjects.isEmpty()) {
            currentSubjects.addAll(findSubjectsSavedForDepartment(saved, department));
        }

        List<Marksheet> teacherSaved = new ArrayList<>();
        for (Marksheet ms : saved) {
            if (ms.getTeacherId() != null && ms.getTeacherId().equals(teacherId)) {
                teacherSaved.add(ms);
            }
        }

        if (!teacherSaved.isEmpty()) {
            Marksheet first = teacherSaved.get(0);
            if (institutionName.equals("Institution Name") && first.getInstitutionName() != null) {
                institutionName = first.getInstitutionName();
                institutionAddress = first.getInstitutionAddress();
                this.teacherId = first.getTeacherId();
                maxMarksPerSubject = first.getMaxMarksPerSubject();
            }
            if (first.getStudent() != null && first.getStudent().getAttendance() != null) {
                defaultTotalClasses = first.getStudent().getAttendance().getTotalClasses();
                saveDepartmentSettings();
            }
        }

        if (currentSubjects.isEmpty() || defaultTotalClasses <= 0) {
            if (!promptForInitialSubjects()) {
                return false;
            }
        } else {
            saveDepartmentSettings();
            updateTableColumns();
        }

        for (Marksheet ms : teacherSaved) {
            Student st = ms.getStudent();
            if (ms.isCorrectionRequested()) {
                correctionRequests.put(st.getStudentId(), ms.getCorrectionDetails());
            }
            Object[] row = new Object[tableModel.getColumnCount()];
            row[0] = st.getName();
            row[1] = st.getStudentId();
            row[2] = defaultTotalClasses;
            row[3] = st.getAttendance() != null ? st.getAttendance().getAttendedClasses() : 0;
            for (int i = 0; i < currentSubjects.size(); i++) {
                if (STATUS_ABSENT.equals(ms.getStatus())) {
                    row[4 + i] = "";
                } else if (i < ms.getSubjects().size()) {
                    row[4 + i] = ms.getSubjects().get(i).getMarks();
                } else {
                    row[4 + i] = 0;
                }
            }
            row[row.length - 1] = ms.getStatus();
            tableModel.addRow(row);
        }
        return true;
    }

    private int[] calculateHighestMarks() {
        return calculateHighestMarks(null);
    }

    private int[] calculateHighestMarks(Set<Integer> excludedRows) {
        int[] highestMarks = new int[currentSubjects.size()];
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (excludedRows != null && excludedRows.contains(i)) {
                continue;
            }
            for(int j = 0; j < currentSubjects.size(); j++) {
                Integer mark = parseValidMarks(i, j);
                if (mark != null) {
                    if (mark > highestMarks[j]) {
                        highestMarks[j] = mark;
                    }
                }
            }
        }
        return highestMarks;
    }

    private void showStatistics() {
        if (currentSubjects.isEmpty() || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available for statistics.");
            return;
        }
        StringBuilder sb = new StringBuilder("<html><h3>Class Statistics</h3><table border='1'><tr><th>Subject</th><th>Average</th><th>Pass %</th></tr>");
        
        for (int j = 0; j < currentSubjects.size(); j++) {
            int totalMarks = 0;
            int passCount = 0;
            int validCount = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Integer mark = parseValidMarks(i, j);
                if (mark != null) {
                    totalMarks += mark;
                    if (mark >= maxMarksPerSubject * 0.4) { // Assume 40% is pass
                        passCount++;
                    }
                    validCount++;
                }
            }
            double avg = validCount > 0 ? (double) totalMarks / validCount : 0;
            double passRatio = validCount > 0 ? ((double) passCount / validCount) * 100 : 0;
            sb.append(String.format("<tr><td>%s</td><td>%.2f</td><td>%.1f%%</td></tr>", currentSubjects.get(j), avg, passRatio));
        }
        sb.append("</table></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCorrectionRequests() {
        if (correctionRequests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active correction requests.");
            return;
        }
        StringBuilder sb = new StringBuilder("Pending Correction Requests:\n\n");
        for (java.util.Map.Entry<String, String> entry : correctionRequests.entrySet()) {
            sb.append("Student ID: ").append(entry.getKey()).append("\nReason: ").append(entry.getValue()).append("\n\n");
        }
        int option = JOptionPane.showConfirmDialog(this, sb.toString() + "\nDo you want to clear these requests?", "Correction Requests", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            correctionRequests.clear();
            List<Marksheet> marksheets = DatabaseManager.readMarksheet();
            for (Marksheet ms : marksheets) {
                if (ms.getTeacherId() != null && ms.getTeacherId().equals(this.teacherId)) {
                    ms.setCorrectionRequested(false);
                    ms.setCorrectionDetails("");
                }
            }
            DatabaseManager.saveMarksheet(marksheets);
            table.repaint();
            JOptionPane.showMessageDialog(this, "Requests cleared. Please ensure you update the marks accordingly.");
        }
    }

    private void generateMarksheets() {
        if (!promptForMarksheetDetails()) {
            return; 
        }

        Set<Integer> invalidIdRows = findInvalidOrDuplicateIdRows();
        int[] highestMarks = calculateHighestMarks(invalidIdRows);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String validationStatus = validateRowForMarksheet(i, invalidIdRows);
                if (!validationStatus.isEmpty()) {
                    tableModel.setValueAt(validationStatus, i, tableModel.getColumnCount() - 1);
                    continue;
                }

                String name = (String) tableModel.getValueAt(i, 0);
                String id = cellText(i, 1);
                String dept = this.teacherDepartment;
                int totalClasses = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                int attended = Integer.parseInt(tableModel.getValueAt(i, 3).toString());

                Attendance attendance = new Attendance(totalClasses, attended);
                Student student = new Student(id, name, id, dept);
                student.setAttendance(attendance);

                Marksheet marksheet = new Marksheet(student);
                marksheet.setMaxMarksPerSubject(maxMarksPerSubject);

                if (!attendance.isEligible()) {
                    marksheet.setStatus(STATUS_NOT_ELIGIBLE);
                    tableModel.setValueAt(STATUS_NOT_ELIGIBLE, i, tableModel.getColumnCount() - 1);
                } else {
                    for (int j = 0; j < currentSubjects.size(); j++) {
                        Integer marks = parseValidMarks(i, j);
                        Subject sub = new Subject(currentSubjects.get(j), marks);
                        sub.setHighestMarks(highestMarks[j]);
                        marksheet.addSubject(sub);
                    }
                    marksheet.setStatus(STATUS_GENERATED);
                    tableModel.setValueAt(STATUS_GENERATED, i, tableModel.getColumnCount() - 1);
                }
            } catch (Exception ex) {
                tableModel.setValueAt("Error", i, tableModel.getColumnCount() - 1);
            }
        }
        JOptionPane.showMessageDialog(this, "Marksheets processed!");
    }

    private boolean promptForMarksheetDetails() {
        JTextField instNameField = new JTextField(institutionName);
        JTextField instAddrField = new JTextField(institutionAddress);
        JTextField teacherIdField = new JTextField(teacherId);
        teacherIdField.setEditable(false);
        JTextField maxMarksField = new JTextField(String.valueOf(maxMarksPerSubject));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Institution Name:"));
        panel.add(instNameField);
        panel.add(new JLabel("Institution Address:"));
        panel.add(instAddrField);
        panel.add(new JLabel("Teacher ID:"));
        panel.add(teacherIdField);
        panel.add(new JLabel("Max Marks per Subject:"));
        panel.add(maxMarksField);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Enter Marksheet Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                institutionName = instNameField.getText().trim();
                institutionAddress = instAddrField.getText().trim();
                teacherId = teacherIdField.getText().trim();
                maxMarksPerSubject = Integer.parseInt(maxMarksField.getText().trim());
                if (maxMarksPerSubject <= 0) throw new NumberFormatException();
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid Max Marks. Please enter a valid positive number.");
                return false;
            }
        }
        return false;
    }

    private void viewMarksheet() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to view their marksheet.");
            return;
        }

        String status = (String) tableModel.getValueAt(selectedRow, tableModel.getColumnCount() - 1);
        if (!STATUS_GENERATED.equals(status)) {
            JOptionPane.showMessageDialog(this, "Marksheet has not been generated for this student.");
            return;
        }

        try {
            Set<Integer> invalidIdRows = findInvalidOrDuplicateIdRows();
            String validationStatus = validateRowForMarksheet(selectedRow, invalidIdRows);
            if (!validationStatus.isEmpty()) {
                tableModel.setValueAt(validationStatus, selectedRow, tableModel.getColumnCount() - 1);
                JOptionPane.showMessageDialog(this, "Marksheet has not been generated for this student.");
                return;
            }

            String name = (String) tableModel.getValueAt(selectedRow, 0);
            String id = cellText(selectedRow, 1);
            String dept = this.teacherDepartment;
            int totalClasses = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString());
            int attended = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString());

            Attendance attendance = new Attendance(totalClasses, attended);
            Student student = new Student(id, name, id, dept);
            student.setAttendance(attendance);

            Marksheet marksheet = new Marksheet(student);
            marksheet.setInstitutionName(institutionName);
            marksheet.setInstitutionAddress(institutionAddress);
            marksheet.setTeacherId(teacherId);
            marksheet.setMaxMarksPerSubject(maxMarksPerSubject);
            
            int[] highestMarks = calculateHighestMarks();
            for (int j = 0; j < currentSubjects.size(); j++) {
                Integer marks = parseValidMarks(selectedRow, j);
                Subject sub = new Subject(currentSubjects.get(j), marks);
                sub.setHighestMarks(highestMarks[j]);
                marksheet.addSubject(sub);
            }
            marksheet.setStatus(STATUS_GENERATED);

            showMarksheetDialog(marksheet);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error displaying marksheet data.");
        }
    }

    private void showMarksheetDialog(Marksheet marksheet) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Marksheet - " + marksheet.getStudent().getName(), true);
        dialog.setSize(650, 700);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.BLACK, 2)
        ));
        outerPanel.setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel instPanel = new JPanel(new BorderLayout(15, 0));
        instPanel.setBackground(Color.WHITE);
        
        try {
            File logoFile = new File("res/logo.png");
            if (logoFile.exists()) {
                Image logoImg = ImageIO.read(logoFile).getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                instPanel.add(new JLabel(new ImageIcon(logoImg)), BorderLayout.WEST);
            }
        } catch (Exception e) {}
        
        JPanel textsPanel = new JPanel(new GridLayout(2, 1));
        textsPanel.setBackground(Color.WHITE);
        JLabel instNameLabel = new JLabel(marksheet.getInstitutionName(), SwingConstants.CENTER);
        instNameLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        instNameLabel.setForeground(Color.BLACK);
        JLabel instAddrLabel = new JLabel(marksheet.getInstitutionAddress(), SwingConstants.CENTER);
        instAddrLabel.setFont(new Font("Georgia", Font.ITALIC, 13));
        instAddrLabel.setForeground(Color.BLACK);
        textsPanel.add(instNameLabel);
        textsPanel.add(instAddrLabel);
        instPanel.add(textsPanel, BorderLayout.CENTER);

        JSeparator msSep = new JSeparator();
        msSep.setForeground(Color.DARK_GRAY);

        JPanel headerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        String[] infoKeys = {"Student Name", "Student ID", "Department", "Attendance"};
        String[] infoVals = {
            marksheet.getStudent().getName(),
            marksheet.getStudent().getStudentId(),
            marksheet.getStudent().getDepartment(),
            String.format("%.1f%%", marksheet.getStudent().getAttendance().calculatePercentage())
        };
        for (int k = 0; k < infoKeys.length; k++) {
            JLabel lbl = new JLabel(infoKeys[k] + ": " + infoVals[k]);
            lbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
            lbl.setForeground(Color.BLACK);
            headerPanel.add(lbl);
        }

        JPanel topCombined = new JPanel(new BorderLayout());
        topCombined.setBackground(Color.WHITE);
        topCombined.add(instPanel, BorderLayout.NORTH);
        topCombined.add(msSep,    BorderLayout.CENTER);
        topCombined.add(headerPanel, BorderLayout.SOUTH);

        JPanel marksPanel = new JPanel(new GridLayout(0, 3, 10, 8));
        marksPanel.setBackground(Color.WHITE);
        marksPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        // Header row
        marksPanel.add(mkMsHeader("Subject",          SwingConstants.LEFT));
        marksPanel.add(mkMsHeader("Marks Obtained",   SwingConstants.CENTER));
        marksPanel.add(mkMsHeader("Highest in Class", SwingConstants.RIGHT));
        for (Subject sub : marksheet.getSubjects()) {
            marksPanel.add(mkMsCell(sub.getSubjectName(),                 SwingConstants.LEFT));
            marksPanel.add(mkMsCell(String.valueOf(sub.getMarks()),       SwingConstants.CENTER));
            marksPanel.add(mkMsCell(String.valueOf(sub.getHighestMarks()),SwingConstants.RIGHT));
        }
        
        JScrollPane marksScroll = new JScrollPane(marksPanel);
        marksScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100,100,100), 1),
            "Academic Performance",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Verdana", Font.BOLD, 12), Color.BLACK));
        marksScroll.setBackground(Color.WHITE);
        marksScroll.getViewport().setBackground(Color.WHITE);
        marksScroll.setPreferredSize(new Dimension(600, 250));

        JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JPanel totalsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        totalsPanel.setBackground(Color.WHITE);
        int maxTotal = marksheet.getSubjects().size() * marksheet.getMaxMarksPerSubject();
        JLabel totalLabel = new JLabel("Total Score: " + marksheet.calculateTotal() + " / " + maxTotal);
        totalLabel.setFont(new Font("Verdana", Font.BOLD, 15));
        totalLabel.setForeground(Color.BLACK);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel gradeLabel = new JLabel("Final Grade: " + marksheet.calculateGrade());
        gradeLabel.setFont(new Font("Verdana", Font.BOLD, 15));
        gradeLabel.setForeground(Color.BLACK);
        gradeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        totalsPanel.add(totalLabel);
        totalsPanel.add(gradeLabel);

        JLabel teacherLabel = new JLabel("Teacher ID: " + marksheet.getTeacherId());
        teacherLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
        teacherLabel.setForeground(Color.BLACK);
        teacherLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel signaturePanel = new JPanel();
        signaturePanel.setLayout(new BoxLayout(signaturePanel, BoxLayout.Y_AXIS));
        signaturePanel.setBackground(Color.WHITE);
        signaturePanel.add(teacherLabel);
        signaturePanel.add(Box.createVerticalStrut(5));
        
        JLabel sigImage;
        try {
            File sigFile = new File("res/signature.png");
            if (sigFile.exists()) {
                Image sImg = ImageIO.read(sigFile).getScaledInstance(150, 50, Image.SCALE_SMOOTH);
                sigImage = new JLabel(new ImageIcon(sImg));
            } else {
                sigImage = new JLabel("<Digital Signature Placeholder>");
                sigImage.setFont(new Font("Brush Script MT", Font.ITALIC, 18));
            }
        } catch (Exception e) {
            sigImage = new JLabel("<Digital Signature Placeholder>");
            sigImage.setFont(new Font("Brush Script MT", Font.ITALIC, 18));
        }
        sigImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        signaturePanel.add(sigImage);
        
        footerPanel.add(signaturePanel, BorderLayout.SOUTH);
        footerPanel.add(totalsPanel, BorderLayout.CENTER);

        panel.add(topCombined, BorderLayout.NORTH);
        panel.add(marksScroll, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        outerPanel.add(panel, BorderLayout.CENTER);
        
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setBackground(Color.WHITE);
        JButton downloadBtn = new JButton("Download Image");
        downloadBtn.addActionListener(e -> {
            try {
                BufferedImage image = new BufferedImage(outerPanel.getWidth(), outerPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                outerPanel.paint(g2);
                g2.dispose();
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(image, "png", file);
                    JOptionPane.showMessageDialog(dialog, "Marksheet saved successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving image: " + ex.getMessage());
            }
        });
        actionsPanel.add(downloadBtn);
        
        dialog.add(outerPanel, BorderLayout.CENTER);
        dialog.add(actionsPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel mkMsHeader(String text, int align) {
        JLabel lbl = new JLabel(text, align);
        lbl.setFont(new Font("Verdana", Font.BOLD, 12));
        lbl.setForeground(Color.BLACK);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        return lbl;
    }

    private JLabel mkMsCell(String text, int align) {
        JLabel lbl = new JLabel(text, align);
        lbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lbl.setForeground(Color.BLACK);
        return lbl;
    }

    private void saveAll() {
        List<Marksheet> allMarksheets = DatabaseManager.readMarksheet();
        List<Marksheet> marksheetsToKeep = new ArrayList<>();
        for (Marksheet ms : allMarksheets) {
            if (ms.getTeacherId() == null || !ms.getTeacherId().equals(this.teacherId)) {
                marksheetsToKeep.add(ms);
            }
        }

        Set<Integer> invalidIdRows = findInvalidOrDuplicateIdRows();
        int[] highestMarks = calculateHighestMarks(invalidIdRows);
        int skippedRows = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String validationStatus = validateRowForMarksheet(i, invalidIdRows);
                if (STATUS_INVALID_DUPLICATE_ID.equals(validationStatus)
                        || STATUS_INVALID_MARKS.equals(validationStatus)) {
                    tableModel.setValueAt(validationStatus, i, tableModel.getColumnCount() - 1);
                    skippedRows++;
                    continue;
                }

                String name = (String) tableModel.getValueAt(i, 0);
                String id = cellText(i, 1);
                String dept = this.teacherDepartment;
                int totalClasses = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                int attended = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
                
                Attendance attendance = new Attendance(totalClasses, attended);
                Student student = new Student(id, name, id, dept);
                student.setAttendance(attendance);
                Marksheet marksheet = new Marksheet(student);
                marksheet.setInstitutionName(institutionName);
                marksheet.setInstitutionAddress(institutionAddress);
                marksheet.setTeacherId(teacherId);
                marksheet.setMaxMarksPerSubject(maxMarksPerSubject);
                
                if (STATUS_ABSENT.equals(validationStatus)) {
                    marksheet.setStatus(STATUS_ABSENT);
                    tableModel.setValueAt(STATUS_ABSENT, i, tableModel.getColumnCount() - 1);
                } else if (!attendance.isEligible()) {
                    marksheet.setStatus(STATUS_NOT_ELIGIBLE);
                    tableModel.setValueAt(STATUS_NOT_ELIGIBLE, i, tableModel.getColumnCount() - 1);
                } else {
                    for (int j = 0; j < currentSubjects.size(); j++) {
                        Integer marks = parseValidMarks(i, j);
                        Subject sub = new Subject(currentSubjects.get(j), marks);
                        sub.setHighestMarks(highestMarks[j]);
                        marksheet.addSubject(sub);
                    }
                    String status = (String) tableModel.getValueAt(i, tableModel.getColumnCount() - 1);
                    if (!STATUS_GENERATED.equals(status)) {
                        status = STATUS_PENDING;
                    }
                    marksheet.setStatus(status);
                }
                marksheetsToKeep.add(marksheet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        DatabaseManager.saveMarksheet(marksheetsToKeep);
        isMarksEditable = false;
        isTotalClassesEditable = false;
        isAllLocked = true;
        String message = "Records Saved Successfully! Click Edit to unlock a selected row again.";
        if (skippedRows > 0) {
            message += "\n" + skippedRows + " invalid row(s) were not saved.";
        }
        JOptionPane.showMessageDialog(this, message);
    }
}
