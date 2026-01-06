import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Màn hình preview cho Server - hiển thị Server đang bị điều khiển
 * Tự động mở full screen khi có Client kết nối
 */
public class ServerPreviewGUI extends JFrame {
    private JLabel statusLabel;
    private JLabel connectionInfoLabel;
    private JButton disconnectButton;
    private JPanel previewPanel;
    private String connectedClientIp;
    private boolean isConnected;
    
    public ServerPreviewGUI() {
        setTitle("TeamViewer Server - Đang bị điều khiển");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true); // Full screen không viền
        
        initComponents();
        
        // Xử lý đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        
        // ESC để thoát full screen
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(
            e -> exitFullScreen(),
            escapeKey,
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Top panel - thông tin kết nối
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 120, 215));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        connectionInfoLabel = new JLabel("⚠️ Đang bị điều khiển từ xa");
        connectionInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        connectionInfoLabel.setForeground(Color.WHITE);
        topPanel.add(connectionInfoLabel, BorderLayout.WEST);
        
        // Nút ngắt kết nối
        disconnectButton = new JButton("🔌 Ngắt kết nối");
        disconnectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        disconnectButton.setFocusPainted(false);
        disconnectButton.setBackground(new Color(232, 17, 35));
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.addActionListener(e -> confirmDisconnect());
        topPanel.add(disconnectButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - preview màn hình
        previewPanel = new JPanel();
        previewPanel.setBackground(new Color(30, 30, 30));
        previewPanel.setLayout(new BorderLayout());
        
        statusLabel = new JLabel("Chờ Client kết nối...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        statusLabel.setForeground(Color.WHITE);
        previewPanel.add(statusLabel, BorderLayout.CENTER);
        
        add(previewPanel, BorderLayout.CENTER);
        
        // Bottom panel - hướng dẫn
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(45, 45, 45));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel helpLabel = new JLabel("Nhấn ESC để thoát chế độ toàn màn hình | Nhấn Ngắt kết nối để dừng điều khiển");
        helpLabel.setForeground(Color.LIGHT_GRAY);
        helpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomPanel.add(helpLabel);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Hiển thị khi có Client kết nối
     */
    public void showConnected(String clientIp) {
        this.connectedClientIp = clientIp;
        this.isConnected = true;
        
        SwingUtilities.invokeLater(() -> {
            connectionInfoLabel.setText("⚠️ Đang bị điều khiển từ xa bởi: " + clientIp);
            statusLabel.setText("✓ Kết nối thành công - Màn hình của bạn đang được xem");
            setVisible(true);
            toFront();
            requestFocus();
        });
    }
    
    /**
     * Ẩn khi ngắt kết nối
     */
    public void showDisconnected() {
        this.isConnected = false;
        this.connectedClientIp = null;
        
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Chờ Client kết nối...");
            connectionInfoLabel.setText("⚠️ Đang bị điều khiển từ xa");
            setVisible(false);
        });
    }
    
    /**
     * Thoát full screen (chuyển về windowed)
     */
    private void exitFullScreen() {
        dispose();
        setUndecorated(false);
        setExtendedState(JFrame.NORMAL);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Xác nhận ngắt kết nối
     */
    private void confirmDisconnect() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn ngắt kết nối với Client?",
            "Xác nhận ngắt kết nối",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // TODO: Thêm logic ngắt kết nối thực sự
            showDisconnected();
        }
    }
    
    /**
     * Xác nhận thoát
     */
    private void confirmExit() {
        if (isConnected) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Client đang kết nối. Bạn có chắc muốn thoát?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }
    
    /**
     * Cập nhật thông tin hoạt động
     */
    public void updateActivity(String activity) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(activity);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerPreviewGUI gui = new ServerPreviewGUI();
            gui.setVisible(true);
            
            // Demo: Giả lập kết nối sau 2 giây
            new Timer(2000, e -> {
                gui.showConnected("192.168.1.100");
                ((Timer) e.getSource()).stop();
            }).start();
        });
    }
}
