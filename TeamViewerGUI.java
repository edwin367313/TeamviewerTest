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
    private JLabel yourIdLabel;
    private JLabel statusLabel;
    private JPanel mainPanel;
    private Client client;
    private Server server;
    private Thread serverThread;
    
    public TeamViewerGUI() {
        setTitle("TeamViewer 2.0");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        initServer();
    }
    
    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel trên - Cho phép điều khiển từ xa
        JPanel controlPanel = createControlPanel();
        
        // Panel dưới - Chờ kết nối
        JPanel waitPanel = createWaitPanel();
        
        // Tách đôi màn hình
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controlPanel, waitPanel);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.4);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setForeground(new Color(0, 128, 0));
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
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
        
        // Kết nối trong thread riêng
        new Thread(() -> {
            client = new Client();
            if (client.connect(partnerId, 5900)) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Đã kết nối!");
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
    
    private void toggleServer() {
        if (server == null) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        serverThread = new Thread(() -> {
            server = new Server();
            try {
                SwingUtilities.invokeLater(() -> {
                    startServerButton.setText("Dừng Server");
                    startServerButton.setBackground(new Color(200, 0, 0));
                    statusLabel.setText("Server đang chạy, chờ kết nối...");
                });
                
                server.start();
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Không thể khởi động server: " + e.getMessage(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Lỗi khởi động server");
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
        
        // Ẩn cửa sổ chính
        this.setVisible(false);
        
        // Khi đóng remote desktop, hiện lại cửa sổ chính
        remoteGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                client.disconnect();
                client = null;
                connectButton.setEnabled(true);
                connectButton.setText("Kết nối");
                statusLabel.setText("Đã ngắt kết nối");
                TeamViewerGUI.this.setVisible(true);
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
