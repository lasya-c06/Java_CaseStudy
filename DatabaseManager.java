package services;

import models.Attendance;
import models.Marksheet;
import models.Student;
import models.Subject;
import models.Teacher;
import models.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DatabaseManager {

    private static final Properties DB_CONFIG = loadDatabaseConfig();
    private static final String DEFAULT_DB_URL =
            "jdbc:mysql://localhost:3306/marksheet_system"
                    + "?createDatabaseIfNotExist=true"
                    + "&useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC";
    private static final String DB_URL  = getConfig("marksheet.db.url", "MARKSHEET_DB_URL", DEFAULT_DB_URL);
    private static final String DB_USER = getConfig("marksheet.db.user", "MARKSHEET_DB_USER", "marksheet_user");
    private static final String DB_PASS = getConfig("marksheet.db.password", "MARKSHEET_DB_PASSWORD", "marksheet123");

    private static Connection connection    = null;
    private static boolean    tablesCreated = false;   // BUG 6 fix
    private static boolean    databaseAvailable = false;
    private static String     lastConnectionError = "";

    private DatabaseManager() {}

    public static boolean connectDatabase() {
        try {
            getConnection();
            databaseAvailable = true;
            lastConnectionError = "";
            return true;
        } catch (SQLException e) {
            databaseAvailable = false;
            lastConnectionError = buildConnectionError(e);
            System.err.println(lastConnectionError);
            return false;
        }
    }

    public static boolean isDatabaseAvailable() {
        return databaseAvailable;
    }

    public static String getLastConnectionError() {
        if (lastConnectionError == null || lastConnectionError.trim().isEmpty()) {
            return "Database is not connected.";
        }
        return lastConnectionError;
    }

    public static void closeDatabase() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
            } finally {
                connection    = null;
                tablesCreated = false;
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "MySQL JDBC Driver not found. Add mysql-connector-j to the build path.", e);
            }
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            if (DB_PASS != null && !DB_PASS.isEmpty()) {
                props.setProperty("password", DB_PASS);
            }
            connection = DriverManager.getConnection(DB_URL, props);
            if (!tablesCreated) {          
                createTables();
                tablesCreated = true;
            }
            databaseAvailable = true;
            lastConnectionError = "";
        }
        return connection;
    }

    private static String getConfig(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        String fileValue = DB_CONFIG.getProperty(propertyName);
        if (fileValue != null && !fileValue.trim().isEmpty()) {
            return fileValue.trim();
        }

        return defaultValue;
    }

    private static Properties loadDatabaseConfig() {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream("database.properties")) {
            properties.load(input);
        } catch (IOException ignored) {
            try (InputStream input = DatabaseManager.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                if (input != null) {
                    properties.load(input);
                }
            } catch (IOException ignoredAgain) {
            }
        }

        return properties;
    }

    private static String buildConnectionError(SQLException e) {
        return "Could not connect to MySQL database. Check that MySQL is running and that the "
                + "database credentials are correct. Current settings: url=" + DB_URL
                + ", user=" + DB_USER
                + ". Update database.properties, or configure -Dmarksheet.db.url, "
                + "-Dmarksheet.db.user, -Dmarksheet.db.password / MARKSHEET_DB_URL, "
                + "MARKSHEET_DB_USER, MARKSHEET_DB_PASSWORD. Original error: "
                + e.getMessage();
    }

    private static void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  username          VARCHAR(100) PRIMARY KEY," +
                "  password          VARCHAR(255) NOT NULL," +
                "  name              VARCHAR(150) NOT NULL DEFAULT ''," +
                "  role              VARCHAR(30)  NOT NULL," +
                "  teacher_id        VARCHAR(100)," +
                "  department        VARCHAR(100)," +
                "  student_id        VARCHAR(100)," +
                "  security_question VARCHAR(255)," +
                "  security_answer   VARCHAR(255)" +
                ")"
            );
            ensureColumn("users", "name",              "VARCHAR(150) DEFAULT ''");
            ensureColumn("users", "teacher_id",        "VARCHAR(100)");
            ensureColumn("users", "department",        "VARCHAR(100)");
            ensureColumn("users", "student_id",        "VARCHAR(100)");
            ensureColumn("users", "security_question", "VARCHAR(255)");
            ensureColumn("users", "security_answer",   "VARCHAR(255)");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS marksheets (" +
                "  student_id            VARCHAR(100) PRIMARY KEY," +
                "  username              VARCHAR(100)," +
                "  student_name          VARCHAR(150) NOT NULL," +
                "  department            VARCHAR(100)," +
                "  total_classes         INT     DEFAULT 0," +
                "  attended_classes      INT     DEFAULT 0," +
                "  status                VARCHAR(50)," +
                "  remarks               TEXT," +
                "  institution_name      VARCHAR(255)," +
                "  institution_address   VARCHAR(255)," +
                "  teacher_id            VARCHAR(100)," +
                "  max_marks_per_subject INT     DEFAULT 100," +
                "  correction_requested  BOOLEAN DEFAULT FALSE," +
                "  correction_details    TEXT" +
                ")"
            );
            ensureColumn("marksheets", "username", "VARCHAR(100)");   

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS marksheet_subjects (" +
                "  student_id    VARCHAR(100) NOT NULL," +
                "  subject_index INT          NOT NULL," +
                "  subject_name  VARCHAR(150) NOT NULL," +
                "  marks         INT DEFAULT 0," +
                "  highest_marks INT DEFAULT 0," +
                "  PRIMARY KEY (student_id, subject_index)," +
                "  FOREIGN KEY (student_id) REFERENCES marksheets(student_id) ON DELETE CASCADE" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS department_settings (" +
                "  department    VARCHAR(100) PRIMARY KEY," +
                "  total_classes INT DEFAULT 0" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS department_subjects (" +
                "  department    VARCHAR(100) NOT NULL," +
                "  subject_index INT          NOT NULL," +
                "  subject_name  VARCHAR(150) NOT NULL," +
                "  PRIMARY KEY (department, subject_index)," +
                "  FOREIGN KEY (department) REFERENCES department_settings(department) ON DELETE CASCADE" +
                ")"
            );
        }
    }

    private static void ensureColumn(String table, String column, String definition)
            throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet cols = meta.getColumns(null, null, table, column)) {
            if (!cols.next()) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate(
                        "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                }
            }
        }
    }

    public static void saveUsers(Map<String, String> credentials,
                                 Map<String, User>   userDetails) {
        String sql =
            "REPLACE INTO users" +
            "  (username, password, name, role, teacher_id, department," +
            "   student_id, security_question, security_answer)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                String username = entry.getKey();
                User   user     = userDetails.get(username);
                if (user == null) continue;

                ps.setString(1, username);
                ps.setString(2, entry.getValue());
                ps.setString(3, user.getName());
                ps.setString(4, user.getRole());
                ps.setString(5, getTeacherId(user));
                ps.setString(6, getDepartment(user));
                ps.setString(7, user instanceof Student
                        ? ((Student) user).getStudentId() : null);
                ps.setString(8, user.getSecurityQuestion());
                ps.setString(9, user.getSecurityAnswer());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static void loadUsers(Map<String, String> credentials,
                                 Map<String, User>   userDetails) {
        String sql = "SELECT * FROM users";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String role     = rs.getString("role");
                String dept     = rs.getString("department");

                User user;
                if ("Teacher".equalsIgnoreCase(role)) {
                    String teacherId = rs.getString("teacher_id");
                    if (teacherId == null || teacherId.trim().isEmpty()) teacherId = username;
                    user = new Teacher(username, rs.getString("name"), teacherId, dept);
                } else {
                    String studentId = rs.getString("student_id");
                    if (studentId == null || studentId.trim().isEmpty()) studentId = username;
                    user = new Student(username, rs.getString("name"), studentId, dept);
                }
                user.setSecurityQuestion(rs.getString("security_question"));
                user.setSecurityAnswer  (rs.getString("security_answer"));
                credentials.put(username, rs.getString("password"));
                userDetails.put(username, user);
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public static void deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }

    public static List<String> getDepartmentSubjects(String department) {
        List<String> subjects = new ArrayList<>();
        if (department == null || department.trim().isEmpty()) return subjects;
        String sql =
            "SELECT subject_name FROM department_subjects" +
            " WHERE department = ? ORDER BY subject_index";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) subjects.add(rs.getString("subject_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading department subjects: " + e.getMessage());
        }
        return subjects;
    }

    public static int getDepartmentTotalClasses(String department) {
        if (department == null || department.trim().isEmpty()) return 0;
        String sql = "SELECT total_classes FROM department_settings WHERE department = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt("total_classes");
                    return v > 0 ? v : 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading department total classes: " + e.getMessage());
        }
        return 0;
    }

    public static void saveDepartmentSettings(String department,
                                              List<String> subjects,
                                              int totalClasses) {
        if (department == null || department.trim().isEmpty()
                || subjects == null || subjects.isEmpty()) return;

        String settingsSql   = "REPLACE INTO department_settings (department, total_classes) VALUES (?, ?)";
        String deleteSubjSql = "DELETE FROM department_subjects WHERE department = ?";
        String insertSubjSql =
            "INSERT INTO department_subjects (department, subject_index, subject_name) VALUES (?, ?, ?)";

        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement settingsPs  = conn.prepareStatement(settingsSql);
                 PreparedStatement deletePs    = conn.prepareStatement(deleteSubjSql);
                 PreparedStatement insertSubjPs= conn.prepareStatement(insertSubjSql)) {

                settingsPs.setString(1, department);
                settingsPs.setInt   (2, totalClasses);
                settingsPs.executeUpdate();

                deletePs.setString(1, department);
                deletePs.executeUpdate();

                for (int i = 0; i < subjects.size(); i++) {
                    insertSubjPs.setString(1, department);
                    insertSubjPs.setInt   (2, i);
                    insertSubjPs.setString(3, subjects.get(i));
                    insertSubjPs.addBatch();
                }
                insertSubjPs.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error saving department settings: " + e.getMessage());
        }
    }
    public static void saveMarksheet(List<Marksheet> marksheets) {
        String marksheetSql =
            "REPLACE INTO marksheets" +
            "  (student_id, username, student_name, department, total_classes," +
            "   attended_classes, status, remarks, institution_name, institution_address," +
            "   teacher_id, max_marks_per_subject, correction_requested, correction_details)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String deleteSubjSql = "DELETE FROM marksheet_subjects WHERE student_id = ?";

        String insertSubjSql =
            "INSERT INTO marksheet_subjects" +
            "  (student_id, subject_index, subject_name, marks, highest_marks)" +
            " VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conn = getConnection();   
            conn.setAutoCommit(false);
            try (PreparedStatement marksheetPs  = conn.prepareStatement(marksheetSql);
                 PreparedStatement deleteSubjPs = conn.prepareStatement(deleteSubjSql);
                 PreparedStatement insertSubjPs = conn.prepareStatement(insertSubjSql)) {

                for (Marksheet marksheet : marksheets) {
                    Student    student    = marksheet.getStudent();
                    Attendance attendance = student.getAttendance();

                    marksheetPs.setString (1,  student.getStudentId());
                    marksheetPs.setString (2,  student.getUsername());   
                    marksheetPs.setString (3,  student.getName());
                    marksheetPs.setString (4,  student.getDepartment());
                    marksheetPs.setInt    (5,  attendance != null ? attendance.getTotalClasses()    : 0);
                    marksheetPs.setInt    (6,  attendance != null ? attendance.getAttendedClasses() : 0);
                    marksheetPs.setString (7,  marksheet.getStatus());
                    marksheetPs.setString (8,  marksheet.getRemarks());
                    marksheetPs.setString (9,  marksheet.getInstitutionName());
                    marksheetPs.setString (10, marksheet.getInstitutionAddress());
                    marksheetPs.setString (11, marksheet.getTeacherId());
                    marksheetPs.setInt    (12, marksheet.getMaxMarksPerSubject());
                    marksheetPs.setBoolean(13, marksheet.isCorrectionRequested());
                    marksheetPs.setString (14, marksheet.getCorrectionDetails());
                    marksheetPs.addBatch();

                    deleteSubjPs.setString(1, student.getStudentId());
                    deleteSubjPs.addBatch();

                    List<Subject> subjects = marksheet.getSubjects();
                    for (int i = 0; i < subjects.size(); i++) {
                        Subject sub = subjects.get(i);
                        insertSubjPs.setString(1, student.getStudentId());
                        insertSubjPs.setInt   (2, i);
                        insertSubjPs.setString(3, sub.getSubjectName());
                        insertSubjPs.setInt   (4, sub.getMarks());
                        insertSubjPs.setInt   (5, sub.getHighestMarks());
                        insertSubjPs.addBatch();
                    }
                }

                marksheetPs.executeBatch();
                deleteSubjPs.executeBatch();
                insertSubjPs.executeBatch();
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error saving marksheets: " + e.getMessage());
        }
    }
    public static List<Marksheet> readMarksheet() {
        List<Marksheet> marksheets = new ArrayList<>();

        String marksheetSql = "SELECT * FROM marksheets ORDER BY student_id";
        String subjectSql   =
            "SELECT * FROM marksheet_subjects WHERE student_id = ? ORDER BY subject_index";

        try {
            Connection conn = getConnection();   // BUG 2 fix

            try (PreparedStatement marksheetPs = conn.prepareStatement(marksheetSql);
                 ResultSet rs = marksheetPs.executeQuery()) {

                while (rs.next()) {
                    String studentId = rs.getString("student_id");

                    String storedUsername = rs.getString("username");
                    if (storedUsername == null || storedUsername.trim().isEmpty()) {
                        storedUsername = studentId;
                    }

                    Student student = new Student(
                        storedUsername,
                        rs.getString("student_name"),
                        studentId,
                        rs.getString("department")
                    );
                    student.setAttendance(new Attendance(
                        rs.getInt("total_classes"),
                        rs.getInt("attended_classes")
                    ));

                    Marksheet marksheet = new Marksheet(student);
                    marksheet.setStatus             (rs.getString ("status"));
                    marksheet.setRemarks            (rs.getString ("remarks"));
                    marksheet.setInstitutionName    (rs.getString ("institution_name"));
                    marksheet.setInstitutionAddress (rs.getString ("institution_address"));
                    marksheet.setTeacherId          (rs.getString ("teacher_id"));
                    marksheet.setMaxMarksPerSubject (rs.getInt    ("max_marks_per_subject"));
                    marksheet.setCorrectionRequested(rs.getBoolean("correction_requested"));
                    marksheet.setCorrectionDetails  (rs.getString ("correction_details"));

                    try (PreparedStatement subjectPs = conn.prepareStatement(subjectSql)) {
                        subjectPs.setString(1, studentId);
                        try (ResultSet subjectRs = subjectPs.executeQuery()) {
                            while (subjectRs.next()) {
                                Subject subject = new Subject(
                                    subjectRs.getString("subject_name"),
                                    subjectRs.getInt   ("marks")
                                );
                                subject.setHighestMarks(subjectRs.getInt("highest_marks"));
                                marksheet.addSubject(subject);
                            }
                        }
                    }
                    marksheets.add(marksheet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading marksheets: " + e.getMessage());
        }
        return marksheets;
    }

    public static void saveMarksheet(Marksheet marksheet) {
        List<Marksheet> marksheets = readMarksheet();
        boolean replaced = false;
        for (int i = 0; i < marksheets.size(); i++) {
            if (marksheets.get(i).getStudent().getStudentId()
                    .equals(marksheet.getStudent().getStudentId())) {
                marksheets.set(i, marksheet);
                replaced = true;
                break;
            }
        }
        if (!replaced) marksheets.add(marksheet);
        saveMarksheet(marksheets);
    }

    public static void deleteMarksheet(String studentId) {
        String sql = "DELETE FROM marksheets WHERE student_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting marksheet: " + e.getMessage());
        }
    }
    public static void addStudent(Student student) {
        String existingPassword = null;
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT password FROM users WHERE username = ?")) {
            ps.setString(1, student.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) existingPassword = rs.getString("password");
            }
        } catch (SQLException e) {
            System.err.println("Error reading existing password: " + e.getMessage());
        }
        String sql =
            "REPLACE INTO users" +
            "  (username, password, name, role, department, student_id," +
            "   security_question, security_answer)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, student.getUsername());
            ps.setString(2, existingPassword != null ? existingPassword : "");
            ps.setString(3, student.getName());
            ps.setString(4, student.getRole());
            ps.setString(5, student.getDepartment());
            ps.setString(6, student.getStudentId());
            ps.setString(7, ((User) student).getSecurityQuestion());
            ps.setString(8, ((User) student).getSecurityAnswer());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
        }
    }

    public static Student getStudent(String studentId) {
        String sql = "SELECT * FROM users WHERE student_id = ? AND role = 'Student'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("student_id"),
                        rs.getString("department")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        return null;
    }

    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Student' ORDER BY student_id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(new Student(
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("student_id"),
                    rs.getString("department")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
        }
        return students;
    }

    private static String getDepartment(User user) {
        if (user instanceof Teacher) return ((Teacher) user).getDepartment();
        if (user instanceof Student) return ((Student) user).getDepartment();
        return null;
    }

    private static String getTeacherId(User user) {
        if (user instanceof Teacher) return ((Teacher) user).getTeacherId();
        return null;
    }
}
