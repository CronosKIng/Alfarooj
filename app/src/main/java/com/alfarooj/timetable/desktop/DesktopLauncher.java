package com.alfarooj.timetable.desktop;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class DesktopLauncher {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea logArea;
    private JTextArea historyArea;
    private JDialog historyDialog;
    private Connection connection;
    private String currentLanguage = "en";
    private String currentUser = null;
    private JLabel statusLabel;
    private JComboBox<String> langCombo;
    
    private Map<String, Map<String, String>> translations;
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        EventQueue.invokeLater(() -> {
            try {
                DesktopLauncher window = new DesktopLauncher();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error starting application: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    public DesktopLauncher() {
        loadTranslations();
        initialize();
        initDatabase();
    }
    
    private void loadTranslations() {
        translations = new HashMap<>();
        
        // ENGLISH
        Map<String, String> en = new HashMap<>();
        en.put("title", "AL FAROOJ AL SHAMI TIME TABLE");
        en.put("username", "Username:");
        en.put("password", "Password:");
        en.put("login", "LOGIN");
        en.put("clear", "CLEAR");
        en.put("attendance", "ATTENDANCE");
        en.put("sign_in", "SIGN IN");
        en.put("sign_out", "SIGN OUT");
        en.put("break_start", "BREAK START");
        en.put("break_end", "BREAK END");
        en.put("delivery_go", "DELIVERY GO");
        en.put("delivery_return", "DELIVERY RETURN");
        en.put("history", "HISTORY");
        en.put("language", "Language");
        en.put("welcome", "Welcome");
        en.put("login_success", "Login successful!");
        en.put("login_failed", "Login failed! Invalid credentials");
        en.put("app_started", "Application Started");
        en.put("default_admin", "Default Admin: admin / 097321494");
        en.put("status_ready", "Ready");
        en.put("recorded", "recorded");
        en.put("close", "Close");
        en.put("attendance_history", "Attendance History");
        translations.put("en", en);
        
        // KISWAHILI
        Map<String, String> sw = new HashMap<>();
        sw.put("title", "AL FAROOJ AL SHAMI RATIBA YA MUDA");
        sw.put("username", "Jina la mtumiaji:");
        sw.put("password", "Nywila:");
        sw.put("login", "INGIA");
        sw.put("clear", "FUTA");
        sw.put("attendance", "HUDHURIO");
        sw.put("sign_in", "INGIA KAZINI");
        sw.put("sign_out", "TOKA KAZINI");
        sw.put("break_start", "ANZA MAPUMZIKO");
        sw.put("break_end", "MALIZA MAPUMZIKO");
        sw.put("delivery_go", "NENDA DELIVERY");
        sw.put("delivery_return", "RUDI DELIVERY");
        sw.put("history", "HISTORIA");
        sw.put("language", "Lugha");
        sw.put("welcome", "Karibu");
        sw.put("login_success", "Kuingia kumefanikiwa!");
        sw.put("login_failed", "Kuingia kumeshindwa!");
        sw.put("app_started", "Programu Imeanza");
        sw.put("default_admin", "Admin msingi: admin / 097321494");
        sw.put("status_ready", "Tayari");
        sw.put("recorded", "imerekodiwa");
        sw.put("close", "Funga");
        sw.put("attendance_history", "Historia ya Hudhurio");
        translations.put("sw", sw);
        
        // ARABIC
        Map<String, String> ar = new HashMap<>();
        ar.put("title", "الجدول الزمني للفاروق الشامي");
        ar.put("username", "اسم المستخدم:");
        ar.put("password", "كلمة المرور:");
        ar.put("login", "تسجيل الدخول");
        ar.put("clear", "مسح");
        ar.put("attendance", "الحضور");
        ar.put("sign_in", "تسجيل الدخول");
        ar.put("sign_out", "تسجيل الخروج");
        ar.put("break_start", "بدء الاستراحة");
        ar.put("break_end", "إنهاء الاستراحة");
        ar.put("delivery_go", "الذهاب للتوصيل");
        ar.put("delivery_return", "العودة من التوصيل");
        ar.put("history", "السجل");
        ar.put("language", "اللغة");
        ar.put("welcome", "مرحباً");
        ar.put("login_success", "تم تسجيل الدخول بنجاح!");
        ar.put("login_failed", "فشل تسجيل الدخول!");
        ar.put("app_started", "تم بدء التطبيق");
        ar.put("default_admin", "المدير الافتراضي: admin / 097321494");
        ar.put("status_ready", "جاهز");
        ar.put("recorded", "تم التسجيل");
        ar.put("close", "إغلاق");
        ar.put("attendance_history", "سجل الحضور");
        translations.put("ar", ar);
        
        // HINDI
        Map<String, String> hi = new HashMap<>();
        hi.put("title", "अल फारूज अल शामी टाइम टेबल");
        hi.put("username", "उपयोगकर्ता नाम:");
        hi.put("password", "पासवर्ड:");
        hi.put("login", "लॉगिन");
        hi.put("clear", "साफ़ करें");
        hi.put("attendance", "उपस्थिति");
        hi.put("sign_in", "साइन इन करें");
        hi.put("sign_out", "साइन आउट करें");
        hi.put("break_start", "ब्रेक शुरू करें");
        hi.put("break_end", "ब्रेक समाप्त करें");
        hi.put("delivery_go", "डिलीवरी जाएँ");
        hi.put("delivery_return", "डिलीवरी से लौटें");
        hi.put("history", "इतिहास");
        hi.put("language", "भाषा");
        hi.put("welcome", "स्वागत है");
        hi.put("login_success", "लॉगिन सफल!");
        hi.put("login_failed", "लॉगिन विफल!");
        hi.put("app_started", "एप्लिकेशन प्रारंभ हुआ");
        hi.put("default_admin", "डिफ़ॉल्ट एडमिन: admin / 097321494");
        hi.put("status_ready", "तैयार");
        hi.put("recorded", "रिकॉर्ड किया गया");
        hi.put("close", "बंद करें");
        hi.put("attendance_history", "उपस्थिति इतिहास");
        translations.put("hi", hi);
        
        // CHINESE
        Map<String, String> zh = new HashMap<>();
        zh.put("title", "阿尔法鲁克阿尔沙米时间表");
        zh.put("username", "用户名:");
        zh.put("password", "密码:");
        zh.put("login", "登录");
        zh.put("clear", "清除");
        zh.put("attendance", "考勤");
        zh.put("sign_in", "签到");
        zh.put("sign_out", "签退");
        zh.put("break_start", "开始休息");
        zh.put("break_end", "结束休息");
        zh.put("delivery_go", "去送货");
        zh.put("delivery_return", "送货返回");
        zh.put("history", "历史记录");
        zh.put("language", "语言");
        zh.put("welcome", "欢迎");
        zh.put("login_success", "登录成功！");
        zh.put("login_failed", "登录失败！");
        zh.put("app_started", "应用程序已启动");
        zh.put("default_admin", "默认管理员: admin / 097321494");
        zh.put("status_ready", "准备就绪");
        zh.put("recorded", "已记录");
        zh.put("close", "关闭");
        zh.put("attendance_history", "考勤历史");
        translations.put("zh", zh);
    }
    
    private String t(String key) {
        Map<String, String> lang = translations.get(currentLanguage);
        if (lang != null && lang.containsKey(key)) {
            return lang.get(key);
        }
        return translations.get("en").getOrDefault(key, key);
    }
    
    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = System.getProperty("user.home") + File.separator + "alfarooj.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Statement stmt = connection.createStatement();
            
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT," +
                "username TEXT UNIQUE," +
                "password TEXT," +
                "role TEXT," +
                "department TEXT)");
            
            // Create attendance logs table
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "event_name TEXT," +
                "timestamp TEXT," +
                "username TEXT," +
                "full_name TEXT)");
            
            // Insert default admin if not exists
            stmt.execute("INSERT OR IGNORE INTO users (full_name, username, password, role) VALUES ('AL FAROOJ AL SHAMI MUWAILEH', 'admin', '097321494', 'super_admin')");
            
            stmt.close();
            logMessage(t("app_started"));
            logMessage(t("default_admin"));
        } catch (Exception e) {
            logMessage("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initialize() {
        frame = new JFrame(t("title"));
        frame.setBounds(100, 100, 900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        
        // Top Panel with Language Switcher
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 150, 243));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(t("title"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        langPanel.setOpaque(false);
        langPanel.add(new JLabel(t("language") + ":"));
        
        String[] languages = {"English", "Kiswahili", "العربية", "हिन्दी", "中文"};
        String[] langCodes = {"en", "sw", "ar", "hi", "zh"};
        langCombo = new JComboBox<>(languages);
        langCombo.addActionListener(e -> {
            currentLanguage = langCodes[langCombo.getSelectedIndex()];
            refreshUI();
        });
        langPanel.add(langCombo);
        topPanel.add(langPanel, BorderLayout.EAST);
        
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Login Panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(33, 150, 243)), t("login")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel(t("username")), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel(t("password")), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        JButton loginButton = new JButton(t("login"));
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> performLogin());
        loginPanel.add(loginButton, gbc);
        
        gbc.gridx = 1;
        JButton clearButton = new JButton(t("clear"));
        clearButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
        });
        loginPanel.add(clearButton, gbc);
        
        mainPanel.add(loginPanel, BorderLayout.NORTH);
        
        // Attendance Panel
        JPanel attendancePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        attendancePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(33, 150, 243)), t("attendance")));
        
        JButton signInBtn = createAttendanceButton(t("sign_in"), new Color(76, 175, 80));
        signInBtn.addActionListener(e -> logAttendance(t("sign_in")));
        attendancePanel.add(signInBtn);
        
        JButton breakStartBtn = createAttendanceButton(t("break_start"), new Color(255, 152, 0));
        breakStartBtn.addActionListener(e -> logAttendance(t("break_start")));
        attendancePanel.add(breakStartBtn);
        
        JButton breakEndBtn = createAttendanceButton(t("break_end"), new Color(255, 152, 0));
        breakEndBtn.addActionListener(e -> logAttendance(t("break_end")));
        attendancePanel.add(breakEndBtn);
        
        JButton signOutBtn = createAttendanceButton(t("sign_out"), new Color(244, 67, 54));
        signOutBtn.addActionListener(e -> logAttendance(t("sign_out")));
        attendancePanel.add(signOutBtn);
        
        JButton deliveryGoBtn = createAttendanceButton(t("delivery_go"), new Color(33, 150, 243));
        deliveryGoBtn.addActionListener(e -> logAttendance(t("delivery_go")));
        attendancePanel.add(deliveryGoBtn);
        
        JButton deliveryReturnBtn = createAttendanceButton(t("delivery_return"), new Color(33, 150, 243));
        deliveryReturnBtn.addActionListener(e -> logAttendance(t("delivery_return")));
        attendancePanel.add(deliveryReturnBtn);
        
        mainPanel.add(attendancePanel, BorderLayout.CENTER);
        
        // Right Panel - History Button
        JPanel rightPanel = new JPanel(new BorderLayout());
        JButton historyBtn = new JButton(t("history"));
        historyBtn.setBackground(new Color(156, 39, 176));
        historyBtn.setForeground(Color.WHITE);
        historyBtn.setFont(new Font("Arial", Font.BOLD, 14));
        historyBtn.addActionListener(e -> showHistory());
        rightPanel.add(historyBtn, BorderLayout.NORTH);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Log Area
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(880, 200));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        
        statusLabel = new JLabel(t("status_ready"));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JButton createAttendanceButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 50));
        return button;
    }
    
    private void refreshUI() {
        frame.setTitle(t("title"));
        
        // Update all components
        Component[] components = frame.getContentPane().getComponents();
        for (Component comp : components) {
            frame.getContentPane().remove(comp);
        }
        
        initialize();
        frame.revalidate();
        frame.repaint();
        
        logMessage(t("app_started"));
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUser = username;
                logMessage(t("login_success") + " " + t("welcome") + " " + rs.getString("full_name"));
                logMessage("Role: " + rs.getString("role"));
                statusLabel.setText(t("welcome") + " " + username);
                JOptionPane.showMessageDialog(frame, t("login_success"), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                logMessage(t("login_failed"));
                JOptionPane.showMessageDialog(frame, t("login_failed"), "Error", JOptionPane.ERROR_MESSAGE);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logMessage("Database error: " + e.getMessage());
        }
    }
    
    private void logAttendance(String action) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "Please login first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        logMessage(action + " " + t("recorded") + " at " + timestamp);
        statusLabel.setText(action + " - " + timestamp);
        
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO attendance_logs (event_name, timestamp, username) VALUES (?, ?, ?)");
            pstmt.setString(1, action);
            pstmt.setString(2, timestamp);
            pstmt.setString(3, currentUser);
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(frame, action + " " + t("recorded") + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            logMessage("Error saving to database: " + e.getMessage());
        }
    }
    
    private void logMessage(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void showHistory() {
        if (historyDialog != null && historyDialog.isVisible()) {
            historyDialog.toFront();
            return;
        }
        
        historyDialog = new JDialog(frame, t("attendance_history"), true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(frame);
        
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        
        JPanel buttonPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadHistory());
        JButton closeBtn = new JButton(t("close"));
        closeBtn.addActionListener(e -> historyDialog.dispose());
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);
        
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        loadHistory();
        historyDialog.setVisible(true);
    }
    
    private void loadHistory() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM attendance_logs ORDER BY id DESC LIMIT 200");
            StringBuilder sb = new StringBuilder();
            sb.append("=".repeat(80)).append("\n");
            sb.append(String.format("%-20s %-30s %-20s\n", "TIMESTAMP", "EVENT", "USERNAME"));
            sb.append("=".repeat(80)).append("\n");
            
            while (rs.next()) {
                sb.append(String.format("%-20s %-30s %-20s\n", 
                    rs.getString("timestamp"), 
                    rs.getString("event_name"),
                    rs.getString("username")));
            }
            rs.close();
            stmt.close();
            
            if (sb.length() <= 80) {
                sb.append("No records found.\n");
            }
            
            historyArea.setText(sb.toString());
            historyArea.setCaretPosition(0);
        } catch (SQLException e) {
            historyArea.setText("Error loading history: " + e.getMessage());
        }
    }
}
