import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Giao diện chính của ứng dụng TeamViewer 2.0
 * Tương tự giao diện TeamViewer thật
 */
public class TeamViewerGUI extends JFrame {
    private JTextField partnerIdField;
    private JButton connectButton;
    private JButton startServerButton;
    private JButton sendFileButton;
    private JLabel yourIdLabel;
    private JLabel statusLabel;
    private JPanel mainPanel;
    private Client client;
    private Server server;
    private Thread serverThread;
    private FileTransferManager fileTransferManager;
    
    // Relay Server components
    private JCheckBox useRelayCheckBox;
    private JTextField relayIpField;
    
    public TeamViewerGUI() {
        setTitle("TeamViewer 2.0");
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        initServer();
    }
    
    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel cấu hình Relay (Mới)
        JPanel settingsPanel = createSettingsPanel();
        mainPanel.add(settingsPanel, BorderLayout.NORTH);

        // Center Panel chứa Control và Wait
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Panel điều khiển từ xa
        JPanel controlPanel = createControlPanel();
        centerPanel.add(controlPanel);
        
        // Panel chờ kết nối
        JPanel waitPanel = createWaitPanel();
        centerPanel.add(waitPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setForeground(new Color(0, 128, 0));
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Cấu hình mạng"));
        
        useRelayCheckBox = new JCheckBox("Sử dụng Relay Server (Docker)");
        useRelayCheckBox.addActionListener(e -> updateUIState());
        panel.add(useRelayCheckBox);
        
        panel.add(new JLabel("Relay IP:"));
        relayIpField = new JTextField("localhost", 10);
        relayIpField.setEnabled(false);
        panel.add(relayIpField);
        
        return panel;
    }

    private void updateUIState() {
        boolean useRelay = useRelayCheckBox.isSelected();
        relayIpField.setEnabled(useRelay);
        
        if (useRelay) {
            // Nếu dùng Relay, ID sẽ được sinh ngẫu nhiên khi Start Server
            yourIdLabel.setText("Nhấn Start Server...");
        } else {
            // Nếu dùng LAN, hiện IP máy
            initServer();
        }
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Điều khiển máy tính từ xa"));
        
        // Partner ID section
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("ID đối tác:"));
        partnerIdField = new JTextField(15);
        idPanel.add(partnerIdField);
        
        connectButton = new JButton("Kết nối");
        connectButton.setBackground(new Color(0, 120, 215));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(e -> connectToPartner());
        idPanel.add(connectButton);
        
        panel.add(idPanel);
        
        // File transfer button
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendFileButton = new JButton("📁 Gửi File");
        sendFileButton.setEnabled(false);
        sendFileButton.addActionListener(e -> sendFile());
        filePanel.add(sendFileButton);
        panel.add(filePanel);
        
        // Description
        JLabel descLabel = new JLabel("<html><i>Nhập ID đối tác để kết nối và điều khiển máy tính của họ</i></html>");
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(descLabel);
        
        return panel;
    }
    
    private JPanel createWaitPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Cho phép điều khiển từ xa"));
        
        // Your ID section
        JPanel yourIdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yourIdPanel.add(new JLabel("ID của bạn:"));
        yourIdLabel = new JLabel("Đang khởi động...");
        yourIdLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        yourIdLabel.setForeground(new Color(0, 120, 215));
        yourIdPanel.add(yourIdLabel);
        
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyYourId());
        yourIdPanel.add(copyButton);
        
        panel.add(yourIdPanel);
        
        // Server control
        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startServerButton = new JButton("Khởi động Server");
        startServerButton.setBackground(new Color(0, 128, 0));
        startServerButton.setForeground(Color.WHITE);
        startServerButton.setFocusPainted(false);
        startServerButton.addActionListener(e -> toggleServer());
        serverPanel.add(startServerButton);
        
        panel.add(serverPanel);
        
        // Description
        JLabel descLabel = new JLabel("<html><i>Cung cấp ID của bạn cho đối tác để họ có thể kết nối</i></html>");
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(descLabel);
        
        return panel;
    }
    
    private void initServer() {
        if (useRelayCheckBox != null && useRelayCheckBox.isSelected()) return;
        
        try {
            // Lấy địa chỉ IP local
            String ipAddress = java.net.InetAddress.getLocalHost().getHostAddress();
            yourIdLabel.setText(ipAddress);
            statusLabel.setText("Server sẵn sàng trên: " + ipAddress + ":5900");
        } catch (Exception e) {
            yourIdLabel.setText("Không xác định");
            statusLabel.setText("Lỗi lấy địa chỉ IP");
        }
    }
    
    private void connectToPartner() {
        String partnerId = partnerIdField.getText().trim();
        
        if (partnerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập ID đối tác!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        connectButton.setEnabled(false);
        connectButton.setText("Đang kết nối...");
        statusLabel.setText("Đang kết nối đến " + partnerId + "...");
        
        boolean useRelay = useRelayCheckBox.isSelected();
        String relayIp = relayIpField.getText().trim();

        // Kết nối trong thread riêng
        new Thread(() -> {
            client = new Client();
            boolean success;
            
            if (useRelay) {
                success = client.connectRelay(relayIp, 5900, partnerId);
            } else {
                success = client.connect(partnerId, 5900);
            }

            if (success) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Đã kết nối!");
                    sendFileButton.setEnabled(true);
                    fileTransferManager = new FileTransferManager(client);
                    openRemoteDesktop();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Không thể kết nối");
                    JOptionPane.showMessageDialog(this, 
                        "Không thể kết nối đến " + partnerId, 
                        "Lỗi kết nối", 
                        JOptionPane.ERROR_MESSAGE);
                    connectButton.setEnabled(true);
                    connectButton.setText("Kết nối");
                });
            }
        }).start();
    }
    
    private void sendFile() {
        if (client == null || !client.isConnected()) {
            JOptionPane.showMessageDialog(this,
                "Chưa kết nối đến đối tác!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file để gửi");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            
            if (selectedFile.length() > 100 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                    "File quá lớn! Giới hạn 100MB.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (fileTransferManager != null) {
                fileTransferManager.sendFile(selectedFile);
                statusLabel.setText("Đang gửi file: " + selectedFile.getName());
            }
        }
    }
    
    private void toggleServer() {
        if (server == null) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        boolean useRelay = useRelayCheckBox.isSelected();
        String relayIp = relayIpField.getText().trim();
        
        serverThread = new Thread(() -> {
            server = new Server();
            try {
                SwingUtilities.invokeLater(() -> {
                    startServerButton.setText("Dừng Server");
                    startServerButton.setBackground(new Color(200, 0, 0));
                    statusLabel.setText(useRelay ? "Đang kết nối Relay..." : "Server đang chạy...");
                });
                
                if (useRelay) {
                    // Sinh ID ngẫu nhiên 6 số
                    String myId = String.format("%06d", new java.util.Random().nextInt(999999));
                    SwingUtilities.invokeLater(() -> yourIdLabel.setText(myId));
                    server.startRelay(relayIp, 5900, myId);
                } else {
                    server.startLAN();
                }
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Lỗi server: " + e.getMessage(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Lỗi khởi động server");
                    stopServer(); // Reset UI
                });
            }
        });
        serverThread.start();
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        startServerButton.setText("Khởi động Server");
        startServerButton.setBackground(new Color(0, 128, 0));
        statusLabel.setText("Server đã dừng");
    }
    
    private void openRemoteDesktop() {
        RemoteDesktopGUI remoteGUI = new RemoteDesktopGUI(client);
        remoteGUI.setVisible(true);
        
        // KHÔNG ẨN cửa sổ chính để có thể gửi file
        // this.setVisible(false);
        
        // Khi đóng remote desktop, reset trạng thái
        remoteGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (client != null) {
                    client.disconnect();
                    client = null;
                }
                fileTransferManager = null;
                connectButton.setEnabled(true);
                connectButton.setText("Kết nối");
                sendFileButton.setEnabled(false);
                statusLabel.setText("Đã ngắt kết nối");
                TeamViewerGUI.this.toFront();
            }
        });
    }
    
    private void copyYourId() {
        String id = yourIdLabel.getText();
        java.awt.datatransfer.StringSelection stringSelection = 
            new java.awt.datatransfer.StringSelection(id);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(stringSelection, null);
        
        statusLabel.setText("Đã copy ID: " + id);
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            TeamViewerGUI gui = new TeamViewerGUI();
            gui.setVisible(true);
        });
    }
}
