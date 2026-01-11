/*
 * ╔═══════════════════════════════════════════════════════════════════════════════╗
 * ║            CTLauncher v1.0.4 - TLauncher Style Edition (Java)                 ║
 * ║              Minecraft Launcher with Blue/Gray Theme                           ║
 * ║                    Team Flames / Samsoft / Cat OS                              ║
 * ╠═══════════════════════════════════════════════════════════════════════════════╣
 * ║  FIX: Now properly downloads ALL libraries and builds full classpath!          ║
 * ╚═══════════════════════════════════════════════════════════════════════════════╝
 *
 * Compile: javac CTLauncher.java
 * Run: java CTLauncher
 */

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.List;
import javax.net.ssl.*;

public class CTLauncher extends JFrame {
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════
    private static final String CTLAUNCHER_DIR = System.getProperty("user.home") + "/.ctlauncher";
    private static final String VERSIONS_DIR = CTLAUNCHER_DIR + "/versions";
    private static final String LIBRARIES_DIR = CTLAUNCHER_DIR + "/libraries";
    private static final String JAVA_DIR = CTLAUNCHER_DIR + "/java";
    private static final String ASSETS_DIR = CTLAUNCHER_DIR + "/assets";
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    
    private static final int MAX_RETRIES = 5;
    private static final int DOWNLOAD_TIMEOUT = 60000;
    private static final int RATE_LIMIT_DELAY = 100;
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // THEME COLORS
    // ═══════════════════════════════════════════════════════════════════════════════
    private Map<String, Color> darkTheme = new HashMap<>();
    private Map<String, Color> lightTheme = new HashMap<>();
    private Map<String, Color> currentTheme;
    private String currentThemeMode = "Dark";
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // UI COMPONENTS
    // ═══════════════════════════════════════════════════════════════════════════════
    private JTextField usernameInput;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> versionCombo;
    private JComboBox<String> themeCombo;
    private JSlider ramSlider;
    private JLabel ramLabel;
    private JLabel statusLabel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private CTButton playButton;
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════════════════════════
    private Map<String, String> versions = new HashMap<>();
    private Map<String, List<String>> versionCategories = new LinkedHashMap<>();
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════════════
    public CTLauncher() {
        initThemes();
        currentTheme = darkTheme;
        
        initVersionCategories();
        initUI();
        
        // Fetch versions after UI is ready
        SwingUtilities.invokeLater(this::fetchVersions);
    }
    
    private void initThemes() {
        // Dark theme (TLauncher style)
        darkTheme.put("bg", new Color(0x2b2b2b));
        darkTheme.put("bgDarker", new Color(0x1e1e1e));
        darkTheme.put("sidebar", new Color(0x333333));
        darkTheme.put("panel", new Color(0x3c3c3c));
        darkTheme.put("accent", new Color(0x4a90d9));
        darkTheme.put("accentHover", new Color(0x5ba0e9));
        darkTheme.put("accentDark", new Color(0x3a7fc9));
        darkTheme.put("grayBtn", new Color(0x555555));
        darkTheme.put("grayBtnHover", new Color(0x666666));
        darkTheme.put("grayBtnDark", new Color(0x444444));
        darkTheme.put("text", new Color(0xffffff));
        darkTheme.put("textSecondary", new Color(0xaaaaaa));
        darkTheme.put("textDim", new Color(0x777777));
        darkTheme.put("inputBg", new Color(0x404040));
        darkTheme.put("inputBorder", new Color(0x555555));
        darkTheme.put("headerBg", new Color(0x252525));
        darkTheme.put("border", new Color(0x4a4a4a));
        darkTheme.put("success", new Color(0x4caf50));
        darkTheme.put("warning", new Color(0xff9800));
        darkTheme.put("error", new Color(0xf44336));
        
        // Light theme
        lightTheme.put("bg", new Color(0xf5f5f5));
        lightTheme.put("bgDarker", new Color(0xe8e8e8));
        lightTheme.put("sidebar", new Color(0xffffff));
        lightTheme.put("panel", new Color(0xffffff));
        lightTheme.put("accent", new Color(0x4a90d9));
        lightTheme.put("accentHover", new Color(0x5ba0e9));
        lightTheme.put("accentDark", new Color(0x3a7fc9));
        lightTheme.put("grayBtn", new Color(0xe0e0e0));
        lightTheme.put("grayBtnHover", new Color(0xd0d0d0));
        lightTheme.put("grayBtnDark", new Color(0xc0c0c0));
        lightTheme.put("text", new Color(0x333333));
        lightTheme.put("textSecondary", new Color(0x666666));
        lightTheme.put("textDim", new Color(0x999999));
        lightTheme.put("inputBg", new Color(0xffffff));
        lightTheme.put("inputBorder", new Color(0xcccccc));
        lightTheme.put("headerBg", new Color(0x4a90d9));
        lightTheme.put("border", new Color(0xdddddd));
        lightTheme.put("success", new Color(0x4caf50));
        lightTheme.put("warning", new Color(0xff9800));
        lightTheme.put("error", new Color(0xf44336));
    }
    
    private void initVersionCategories() {
        versionCategories.put("Latest Release", new ArrayList<>());
        versionCategories.put("Latest Snapshot", new ArrayList<>());
        versionCategories.put("Release", new ArrayList<>());
        versionCategories.put("Snapshot", new ArrayList<>());
        versionCategories.put("Old Beta", new ArrayList<>());
        versionCategories.put("Old Alpha", new ArrayList<>());
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // UI INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════════
    private void initUI() {
        setTitle("CTLauncher v1.0.4");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(currentTheme.get("bg"));
        
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(currentTheme.get("bg"));
        centerPanel.add(createSidebar(), BorderLayout.WEST);
        centerPanel.add(createMainContent(), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(currentTheme.get("headerBg"));
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("⬛");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        iconLabel.setForeground(currentTheme.get("accent"));
        logoPanel.add(iconLabel);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("CTLauncher");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(currentTheme.get("text"));
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Minecraft Launcher");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        subtitleLabel.setForeground(currentTheme.get("textDim"));
        titlePanel.add(subtitleLabel);
        
        logoPanel.add(titlePanel);
        header.add(logoPanel, BorderLayout.WEST);
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlsPanel.setOpaque(false);
        
        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        themeLabel.setForeground(currentTheme.get("textSecondary"));
        controlsPanel.add(themeLabel);
        
        themeCombo = new JComboBox<>(new String[]{"Dark", "Light"});
        themeCombo.setSelectedItem(currentThemeMode);
        themeCombo.setPreferredSize(new Dimension(80, 25));
        themeCombo.addActionListener(e -> changeTheme());
        controlsPanel.add(themeCombo);
        
        JLabel versionLabel = new JLabel("v1.0.4");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        versionLabel.setForeground(currentTheme.get("textDim"));
        controlsPanel.add(versionLabel);
        
        header.add(controlsPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(currentTheme.get("sidebar"));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Profile section
        JPanel profilePanel = createSectionPanel("PROFILE");
        
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        userLabel.setForeground(currentTheme.get("textDim"));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.add(userLabel);
        profilePanel.add(Box.createVerticalStrut(2));
        
        usernameInput = new JTextField("Player");
        usernameInput.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        usernameInput.setBackground(currentTheme.get("inputBg"));
        usernameInput.setForeground(currentTheme.get("text"));
        usernameInput.setCaretColor(currentTheme.get("text"));
        usernameInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.get("inputBorder")),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        usernameInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        usernameInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.add(usernameInput);
        
        sidebar.add(profilePanel);
        sidebar.add(Box.createVerticalStrut(10));
        
        // Version section
        JPanel versionPanel = createSectionPanel("VERSION");
        
        JLabel catLabel = new JLabel("Category");
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        catLabel.setForeground(currentTheme.get("textDim"));
        catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionPanel.add(catLabel);
        versionPanel.add(Box.createVerticalStrut(2));
        
        categoryCombo = new JComboBox<>(versionCategories.keySet().toArray(new String[0]));
        categoryCombo.setSelectedItem("Latest Release");
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        categoryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryCombo.addActionListener(e -> updateVersionList());
        versionPanel.add(categoryCombo);
        versionPanel.add(Box.createVerticalStrut(8));
        
        JLabel verLabel = new JLabel("Version");
        verLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        verLabel.setForeground(currentTheme.get("textDim"));
        verLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionPanel.add(verLabel);
        versionPanel.add(Box.createVerticalStrut(2));
        
        versionCombo = new JComboBox<>();
        versionCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        versionCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionPanel.add(versionCombo);
        
        sidebar.add(versionPanel);
        sidebar.add(Box.createVerticalStrut(10));
        
        // RAM section
        JPanel ramPanel = createSectionPanel("MEMORY");
        
        ramLabel = new JLabel("RAM: 4 GB");
        ramLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        ramLabel.setForeground(currentTheme.get("text"));
        ramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ramPanel.add(ramLabel);
        ramPanel.add(Box.createVerticalStrut(2));
        
        ramSlider = new JSlider(1, 16, 4);
        ramSlider.setBackground(currentTheme.get("panel"));
        ramSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        ramSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        ramSlider.addChangeListener(e -> ramLabel.setText("RAM: " + ramSlider.getValue() + " GB"));
        ramPanel.add(ramSlider);
        
        sidebar.add(ramPanel);
        sidebar.add(Box.createVerticalGlue());
        
        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        CTButton settingsBtn = new CTButton("⚙ Settings", false, currentTheme);
        settingsBtn.addActionListener(e -> showSettings());
        btnPanel.add(settingsBtn);
        
        CTButton refreshBtn = new CTButton("↻ Refresh", false, currentTheme);
        refreshBtn.addActionListener(e -> fetchVersions());
        btnPanel.add(refreshBtn);
        
        sidebar.add(btnPanel);
        
        return sidebar;
    }
    
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(currentTheme.get("panel"));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLabel.setForeground(currentTheme.get("textSecondary"));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        return panel;
    }
    
    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(currentTheme.get("bg"));
        content.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel mcTitle = new JLabel("MINECRAFT");
        mcTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        mcTitle.setForeground(currentTheme.get("text"));
        mcTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(mcTitle);
        
        JLabel mcSubtitle = new JLabel("Java Edition");
        mcSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mcSubtitle.setForeground(currentTheme.get("textSecondary"));
        mcSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(mcSubtitle);
        
        centerPanel.add(Box.createVerticalStrut(30));
        
        playButton = new CTButton("▶  PLAY", true, currentTheme);
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.setMaximumSize(new Dimension(200, 50));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> prepareAndLaunch());
        centerPanel.add(playButton);
        
        centerPanel.add(Box.createVerticalStrut(30));
        
        progressLabel = new JLabel("");
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        progressLabel.setForeground(currentTheme.get("textDim"));
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(progressLabel);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(300, 20));
        progressBar.setMaximumSize(new Dimension(300, 20));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        centerPanel.add(progressBar);
        
        content.add(Box.createVerticalGlue());
        content.add(centerPanel);
        content.add(Box.createVerticalGlue());
        
        JLabel footerLabel = new JLabel("CTLauncher • Team Flames / Samsoft");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        footerLabel.setForeground(currentTheme.get("textDim"));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(footerLabel);
        
        return content;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusBar.setBackground(currentTheme.get("bgDarker"));
        statusBar.setPreferredSize(new Dimension(0, 28));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        statusLabel.setForeground(currentTheme.get("textDim"));
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // VERSION MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════════
    private void fetchVersions() {
        statusLabel.setText("Fetching versions...");
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    setupSSL();
                    
                    URL url = URI.create(VERSION_MANIFEST_URL).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    parseVersionManifest(response.toString());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        JOptionPane.showMessageDialog(CTLauncher.this, 
                            "Failed to fetch versions:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                updateVersionList();
                statusLabel.setText("Loaded " + versions.size() + " versions");
            }
        }.execute();
    }
    
    private void setupSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };
        
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
    
    private void parseVersionManifest(String json) {
        versions.clear();
        for (List<String> list : versionCategories.values()) {
            list.clear();
        }
        
        String latestRelease = extractJsonValue(json, "release");
        String latestSnapshot = extractJsonValue(json, "snapshot");
        
        int versionsStart = json.indexOf("\"versions\"");
        if (versionsStart == -1) return;
        
        int arrayStart = json.indexOf("[", versionsStart);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        String versionsArray = json.substring(arrayStart, arrayEnd + 1);
        
        int pos = 0;
        while ((pos = versionsArray.indexOf("{", pos)) != -1) {
            int objEnd = findMatchingBrace(versionsArray, pos);
            String versionObj = versionsArray.substring(pos, objEnd + 1);
            
            String id = extractJsonValue(versionObj, "id");
            String type = extractJsonValue(versionObj, "type");
            String versionUrl = extractJsonValue(versionObj, "url");
            
            if (id != null && versionUrl != null) {
                versions.put(id, versionUrl);
                
                if (id.equals(latestRelease)) {
                    versionCategories.get("Latest Release").add(id);
                }
                if (id.equals(latestSnapshot)) {
                    versionCategories.get("Latest Snapshot").add(id);
                }
                
                if ("release".equals(type)) {
                    versionCategories.get("Release").add(id);
                } else if ("snapshot".equals(type)) {
                    versionCategories.get("Snapshot").add(id);
                } else if ("old_beta".equals(type)) {
                    versionCategories.get("Old Beta").add(id);
                } else if ("old_alpha".equals(type)) {
                    versionCategories.get("Old Alpha").add(id);
                }
            }
            
            pos = objEnd + 1;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyPos = json.indexOf(pattern);
        if (keyPos == -1) return null;
        
        int colonPos = json.indexOf(":", keyPos);
        if (colonPos == -1) return null;
        
        int valueStart = colonPos + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return null;
        
        if (json.charAt(valueStart) == '"') {
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd == -1) return null;
            return json.substring(valueStart + 1, valueEnd);
        }
        
        return null;
    }
    
    private int findMatchingBracket(String s, int start) {
        int count = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '[') count++;
            else if (s.charAt(i) == ']') count--;
            if (count == 0) return i;
        }
        return s.length() - 1;
    }
    
    private int findMatchingBrace(String s, int start) {
        int count = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '{') count++;
            else if (s.charAt(i) == '}') count--;
            if (count == 0) return i;
        }
        return s.length() - 1;
    }
    
    private void updateVersionList() {
        String category = (String) categoryCombo.getSelectedItem();
        if (category == null) return;
        
        List<String> vers = versionCategories.get(category);
        versionCombo.removeAllItems();
        
        if (vers != null) {
            for (String v : vers) {
                versionCombo.addItem(v);
            }
            if (!vers.isEmpty()) {
                versionCombo.setSelectedIndex(0);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // LAUNCH LOGIC
    // ═══════════════════════════════════════════════════════════════════════════════
    private void prepareAndLaunch() {
        createGameDirectories();
        downloadAndLaunch();
    }
    
    private void createGameDirectories() {
        new File(CTLAUNCHER_DIR).mkdirs();
        new File(VERSIONS_DIR).mkdirs();
        new File(LIBRARIES_DIR).mkdirs();
        new File(ASSETS_DIR).mkdirs();
        new File(JAVA_DIR).mkdirs();
    }
    
    private void downloadAndLaunch() {
        String version = (String) versionCombo.getSelectedItem();
        if (version == null || version.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No version selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String username = validateUsername(usernameInput.getText());
        int ram = ramSlider.getValue();
        String versionUrl = versions.get(version);
        
        if (versionUrl == null) {
            JOptionPane.showMessageDialog(this, "Version URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        playButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressLabel.setText("Downloading " + version + "...");
        
        new SwingWorker<Boolean, Integer>() {
            private String versionJson = null;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                return downloadVersionFiles(version, versionUrl);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        String nativesDir = VERSIONS_DIR + "/" + version + "/natives";
                        launchGame(version, username, ram, nativesDir);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CTLauncher.this,
                        "Failed to launch:\n" + e.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    playButton.setEnabled(true);
                }
            }
        }.execute();
    }
    
    private boolean downloadVersionFiles(String version, String versionUrl) {
        String versionDir = VERSIONS_DIR + "/" + version;
        new File(versionDir).mkdirs();
        new File(versionDir + "/natives").mkdirs();
        
        try {
            setupSSL();
            
            // Download version JSON
            String jsonPath = versionDir + "/" + version + ".json";
            if (!new File(jsonPath).exists()) {
                SwingUtilities.invokeLater(() -> progressLabel.setText("Downloading version info..."));
                downloadFile(versionUrl, jsonPath);
            }
            
            SwingUtilities.invokeLater(() -> progressBar.setValue(10));
            
            // Read version data
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonPath)));
            
            // Download client JAR
            String jarUrl = extractNestedJsonValue(jsonContent, "downloads", "client", "url");
            String jarPath = versionDir + "/" + version + ".jar";
            if (jarUrl != null && !new File(jarPath).exists()) {
                SwingUtilities.invokeLater(() -> progressLabel.setText("Downloading " + version + ".jar..."));
                downloadFile(jarUrl, jarPath);
            }
            
            SwingUtilities.invokeLater(() -> progressBar.setValue(30));
            
            // Download ALL libraries - THIS IS THE KEY FIX!
            SwingUtilities.invokeLater(() -> progressLabel.setText("Downloading libraries..."));
            downloadLibraries(jsonContent);
            
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(100);
                progressLabel.setText("Download complete!");
            });
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                progressLabel.setText("Error: " + e.getMessage());
                JOptionPane.showMessageDialog(CTLauncher.this,
                    "Failed to download:\n" + e.getMessage(),
                    "Download Error", JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // LIBRARY DOWNLOAD - THE KEY FIX!
    // ═══════════════════════════════════════════════════════════════════════════════
    private void downloadLibraries(String jsonContent) {
        // Find libraries array
        int libStart = jsonContent.indexOf("\"libraries\"");
        if (libStart == -1) return;
        
        int arrayStart = jsonContent.indexOf("[", libStart);
        if (arrayStart == -1) return;
        int arrayEnd = findMatchingBracket(jsonContent, arrayStart);
        String librariesArray = jsonContent.substring(arrayStart, arrayEnd + 1);
        
        // Count libraries for progress
        List<String[]> libsToDownload = new ArrayList<>();
        
        int pos = 0;
        while ((pos = librariesArray.indexOf("{", pos)) != -1) {
            int objEnd = findMatchingBrace(librariesArray, pos);
            String libObj = librariesArray.substring(pos, objEnd + 1);
            
            // Check for downloads.artifact
            int downloadsPos = libObj.indexOf("\"downloads\"");
            if (downloadsPos != -1) {
                int artifactPos = libObj.indexOf("\"artifact\"", downloadsPos);
                if (artifactPos != -1) {
                    String artifactPath = extractJsonValue(libObj.substring(artifactPos), "path");
                    String artifactUrl = extractJsonValue(libObj.substring(artifactPos), "url");
                    
                    if (artifactPath != null && artifactUrl != null) {
                        libsToDownload.add(new String[]{artifactPath, artifactUrl});
                    }
                }
                
                // Also check for classifiers (natives)
                int classifiersPos = libObj.indexOf("\"classifiers\"", downloadsPos);
                if (classifiersPos != -1) {
                    // Find natives for current OS
                    String osName = getOsName();
                    String nativeKey = "natives-" + osName;
                    
                    int nativePos = libObj.indexOf("\"" + nativeKey + "\"", classifiersPos);
                    if (nativePos != -1) {
                        String nativePath = extractJsonValue(libObj.substring(nativePos), "path");
                        String nativeUrl = extractJsonValue(libObj.substring(nativePos), "url");
                        
                        if (nativePath != null && nativeUrl != null) {
                            libsToDownload.add(new String[]{nativePath, nativeUrl});
                        }
                    }
                }
            }
            
            pos = objEnd + 1;
        }
        
        // Download libraries
        int total = libsToDownload.size();
        int current = 0;
        
        for (String[] lib : libsToDownload) {
            String path = lib[0];
            String url = lib[1];
            String fullPath = LIBRARIES_DIR + "/" + path;
            
            File libFile = new File(fullPath);
            if (!libFile.exists()) {
                try {
                    libFile.getParentFile().mkdirs();
                    downloadFile(url, fullPath);
                    Thread.sleep(RATE_LIMIT_DELAY); // Rate limit
                } catch (Exception e) {
                    System.err.println("Failed to download: " + path + " - " + e.getMessage());
                }
            }
            
            current++;
            final int progress = 30 + (60 * current / total);
            final int cur = current;
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress);
                progressLabel.setText("Downloading libraries... (" + cur + "/" + total + ")");
            });
        }
    }
    
    private String getOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "osx";
        return "linux";
    }
    
    private void downloadFile(String urlStr, String destPath) throws Exception {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(DOWNLOAD_TIMEOUT);
        conn.setReadTimeout(DOWNLOAD_TIMEOUT);
        conn.setRequestProperty("User-Agent", "CTLauncher/1.0");
        
        new File(destPath).getParentFile().mkdirs();
        
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destPath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    private String extractNestedJsonValue(String json, String... keys) {
        String current = json;
        for (int i = 0; i < keys.length - 1; i++) {
            int keyPos = current.indexOf("\"" + keys[i] + "\"");
            if (keyPos == -1) return null;
            int bracePos = current.indexOf("{", keyPos);
            if (bracePos == -1) return null;
            int braceEnd = findMatchingBrace(current, bracePos);
            current = current.substring(bracePos, braceEnd + 1);
        }
        return extractJsonValue(current, keys[keys.length - 1]);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // LAUNCH GAME
    // ═══════════════════════════════════════════════════════════════════════════════
    private void launchGame(String version, String username, int ram, String nativesDir) {
        try {
            String versionDir = VERSIONS_DIR + "/" + version;
            String jsonPath = versionDir + "/" + version + ".json";
            String jarPath = versionDir + "/" + version + ".jar";
            
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonPath)));
            String mainClass = extractJsonValue(jsonContent, "mainClass");
            if (mainClass == null) mainClass = "net.minecraft.client.main.Main";
            
            // Build FULL classpath with ALL libraries
            List<String> classpathList = new ArrayList<>();
            classpathList.add(jarPath);
            
            // Add all libraries to classpath
            addLibrariesToClasspath(jsonContent, classpathList);
            
            String sep = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";
            String classpath = String.join(sep, classpathList);
            
            // Build command
            List<String> cmd = new ArrayList<>();
            cmd.add("java");
            cmd.add("-Xmx" + ram + "G");
            
            // JVM args from version JSON
            addJvmArgs(jsonContent, cmd, nativesDir);
            
            // Offline mode
            cmd.add("-Dminecraft.api.auth.host=http://0.0.0.0");
            cmd.add("-Dminecraft.api.account.host=http://0.0.0.0");
            cmd.add("-Dminecraft.api.session.host=http://0.0.0.0");
            cmd.add("-Dminecraft.api.services.host=http://0.0.0.0");
            
            // macOS specific
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                if (!cmd.contains("-XstartOnFirstThread")) {
                    cmd.add("-XstartOnFirstThread");
                }
            }
            
            // Classpath and main class
            cmd.add("-cp");
            cmd.add(classpath);
            cmd.add(mainClass);
            
            // Game arguments
            String uuid = generateOfflineUUID(username);
            String assetIndex = extractNestedJsonValue(jsonContent, "assetIndex", "id");
            if (assetIndex == null) assetIndex = "legacy";
            
            cmd.add("--username"); cmd.add(username);
            cmd.add("--version"); cmd.add(version);
            cmd.add("--gameDir"); cmd.add(CTLAUNCHER_DIR);
            cmd.add("--assetsDir"); cmd.add(ASSETS_DIR);
            cmd.add("--assetIndex"); cmd.add(assetIndex);
            cmd.add("--uuid"); cmd.add(uuid);
            cmd.add("--accessToken"); cmd.add("0");
            cmd.add("--userType"); cmd.add("legacy");
            
            statusLabel.setText("Launching Minecraft " + version + "...");
            System.out.println("🚀 Launching: " + String.join(" ", cmd));
            
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File(CTLAUNCHER_DIR));
            pb.inheritIO();
            pb.start();
            
            progressLabel.setText("Game launched!");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error launching:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addLibrariesToClasspath(String jsonContent, List<String> classpathList) {
        int libStart = jsonContent.indexOf("\"libraries\"");
        if (libStart == -1) return;
        
        int arrayStart = jsonContent.indexOf("[", libStart);
        if (arrayStart == -1) return;
        int arrayEnd = findMatchingBracket(jsonContent, arrayStart);
        String librariesArray = jsonContent.substring(arrayStart, arrayEnd + 1);
        
        int pos = 0;
        while ((pos = librariesArray.indexOf("{", pos)) != -1) {
            int objEnd = findMatchingBrace(librariesArray, pos);
            String libObj = librariesArray.substring(pos, objEnd + 1);
            
            // Check for downloads.artifact
            int downloadsPos = libObj.indexOf("\"downloads\"");
            if (downloadsPos != -1) {
                int artifactPos = libObj.indexOf("\"artifact\"", downloadsPos);
                if (artifactPos != -1) {
                    String artifactPath = extractJsonValue(libObj.substring(artifactPos), "path");
                    if (artifactPath != null) {
                        String fullPath = LIBRARIES_DIR + "/" + artifactPath;
                        if (new File(fullPath).exists()) {
                            classpathList.add(fullPath);
                        }
                    }
                }
            }
            
            pos = objEnd + 1;
        }
    }
    
    private void addJvmArgs(String jsonContent, List<String> cmd, String nativesDir) {
        // Check for arguments.jvm
        int argsPos = jsonContent.indexOf("\"arguments\"");
        if (argsPos != -1) {
            int jvmPos = jsonContent.indexOf("\"jvm\"", argsPos);
            if (jvmPos != -1) {
                int arrayStart = jsonContent.indexOf("[", jvmPos);
                if (arrayStart != -1) {
                    int arrayEnd = findMatchingBracket(jsonContent, arrayStart);
                    String jvmArray = jsonContent.substring(arrayStart + 1, arrayEnd);
                    
                    // Simple extraction of string arguments
                    int strPos = 0;
                    while ((strPos = jvmArray.indexOf("\"", strPos)) != -1) {
                        int strEnd = jvmArray.indexOf("\"", strPos + 1);
                        if (strEnd == -1) break;
                        
                        String arg = jvmArray.substring(strPos + 1, strEnd);
                        
                        // Skip if it's a key name or special value
                        if (!arg.isEmpty() && (arg.startsWith("-") || arg.startsWith("$"))) {
                            // Replace placeholders
                            arg = arg.replace("${natives_directory}", nativesDir);
                            arg = arg.replace("${launcher_name}", "CTLauncher");
                            arg = arg.replace("${launcher_version}", "1.0.4");
                            arg = arg.replace("${classpath}", ""); // Skip this one
                            
                            if (!arg.contains("${") && !arg.isEmpty() && !arg.equals("-cp")) {
                                cmd.add(arg);
                            }
                        }
                        
                        strPos = strEnd + 1;
                    }
                }
            }
        }
        
        // Ensure native library path is set
        boolean hasNativePath = false;
        for (String arg : cmd) {
            if (arg.contains("-Djava.library.path=")) {
                hasNativePath = true;
                break;
            }
        }
        if (!hasNativePath) {
            cmd.add("-Djava.library.path=" + nativesDir);
        }
    }
    
    private String validateUsername(String username) {
        if (username == null || !username.matches("^[a-zA-Z0-9_]+$")) {
            return "Player";
        }
        return username;
    }
    
    private String generateOfflineUUID(String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(("OfflinePlayer:" + username).getBytes());
            
            digest[6] = (byte) ((digest[6] & 0x0f) | 0x30);
            digest[8] = (byte) ((digest[8] & 0x3f) | 0x80);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hex = sb.toString();
            
            return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" +
                   hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" +
                   hex.substring(20, 32);
                   
        } catch (Exception e) {
            return "00000000-0000-0000-0000-000000000000";
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // THEME & SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════════
    private void changeTheme() {
        String newTheme = (String) themeCombo.getSelectedItem();
        if (newTheme != null && !newTheme.equals(currentThemeMode)) {
            currentThemeMode = newTheme;
            currentTheme = "Dark".equals(newTheme) ? darkTheme : lightTheme;
            
            getContentPane().removeAll();
            initUI();
            revalidate();
            repaint();
        }
    }
    
    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Settings panel coming soon!", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // CUSTOM BUTTON CLASS
    // ═══════════════════════════════════════════════════════════════════════════════
    static class CTButton extends JButton {
        private boolean isBlue;
        private Map<String, Color> theme;
        private boolean isHovered = false;
        private boolean isPressed = false;
        
        public CTButton(String text, boolean isBlue, Map<String, Color> theme) {
            super(text);
            this.isBlue = isBlue;
            this.theme = theme;
            
            setFont(new Font("Segoe UI", Font.BOLD, isBlue ? 12 : 10));
            setForeground(theme.get("text"));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (!isBlue) {
                setPreferredSize(new Dimension(120, 32));
            }
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
                
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color bg;
            if (isBlue) {
                if (isPressed) bg = theme.get("accentDark");
                else if (isHovered) bg = theme.get("accentHover");
                else bg = theme.get("accent");
            } else {
                if (isPressed) bg = theme.get("grayBtnDark");
                else if (isHovered) bg = theme.get("grayBtnHover");
                else bg = theme.get("grayBtn");
            }
            
            if (!isEnabled()) {
                bg = theme.get("grayBtnDark");
            }
            
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            
            g2.setColor(isEnabled() ? getForeground() : theme.get("textDim"));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║       CTLauncher v1.0.4 - TLauncher Style (Java)              ║");
        System.out.println("║              Team Flames / Samsoft / Cat OS                   ║");
        System.out.println("║  FIX: Now downloads ALL libraries for full classpath!        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default
        }
        
        SwingUtilities.invokeLater(() -> {
            CTLauncher launcher = new CTLauncher();
            launcher.setVisible(true);
        });
    }
}
