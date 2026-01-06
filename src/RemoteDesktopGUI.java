import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Giao diện hiển thị và điều khiển màn hình từ xa
 */
public class RemoteDesktopGUI extends JFrame {
    private Client client;
    private JPanel screenPanel;
    private JLabel screenLabel;
    private BufferedImage currentScreen;
    private ScreenReceiver screenReceiver;
    private Thread receiverThread;
    private JLabel statusLabel;
    private JLabel fpsLabel;
    private long lastUpdateTime;
    private int frameCount;
    private double currentFps;
    private FileTransferManager fileTransferManager;
    private FileTransferDialog fileTransferDialog;
    private KeyEventDispatcher globalKeyListener;
    private CollapsibleChatPanel chatPanel;
    private boolean isFitToWindow = false;
    
    public RemoteDesktopGUI(Client client) {
        this.client = client;
        this.lastUpdateTime = System.currentTimeMillis();
        this.frameCount = 0;
        this.currentFps = 0;
        this.fileTransferManager = new FileTransferManager(client);
        this.fileTransferDialog = new FileTransferDialog(this);
        
        setTitle("TeamViewer 2.0 - Remote Desktop");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Setup file transfer listener
        setupFileTransferListener();
        
        initComponents();
        startScreenReceiver();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }
    
    private void handleWindowClosing() {
        // Kiểm tra nếu có chat history, hỏi có muốn save không
        if (chatPanel != null && chatPanel.hasChatContent()) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn lưu lịch sử chat không?",
                "Lưu lịch sử chat",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (option == JOptionPane.YES_OPTION) {
                saveChatBeforeClose();
            }
        }
        
        // Dọn dẹp resources
        stopScreenReceiver();
        removeGlobalKeyListener();
    }
    
    private void saveChatBeforeClose() {
        JFileChooser fileChooser = new JFileChooser();
        
        // Tên file mặc định
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultFileName = "chat_" + client.getClientId() + "_" + 
                                sdf.format(new java.util.Date()) + ".txt";
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Thêm .txt nếu chưa có extension
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
                writer.write(chatPanel.getChatHistory());
                JOptionPane.showMessageDialog(this,
                    "Đã lưu lịch sử chat vào:\n" + file.getAbsolutePath(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu file: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Toolbar
        JToolBar toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);
        
        // Screen panel
        screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBackground(Color.BLACK);
        
        screenLabel = new JLabel();
        screenLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(screenLabel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        screenPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Chat panel ở góc dưới
        chatPanel = new CollapsibleChatPanel(client.getClientId(), 
            message -> sendChatMessage(message));
        screenPanel.add(chatPanel, BorderLayout.SOUTH);
        
        add(screenPanel, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
        
        // Thêm mouse và keyboard listeners
        addInputListeners();
    }
    
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // Actions button
        JButton actionsBtn = new JButton("Actions");
        actionsBtn.addActionListener(e -> showActionsMenu(actionsBtn));
        toolbar.add(actionsBtn);
        
        toolbar.addSeparator();
        
        // View button
        JButton viewBtn = new JButton("View");
        viewBtn.addActionListener(e -> showViewMenu(viewBtn));
        toolbar.add(viewBtn);
        
        toolbar.addSeparator();
        
        // Send File button
        JButton sendFileBtn = new JButton("📁 Send File");
        sendFileBtn.addActionListener(e -> sendFile());
        toolbar.add(sendFileBtn);
        
        toolbar.addSeparator();
        
        // File Transfers button
        JButton transfersBtn = new JButton("📊 Transfers");
        transfersBtn.addActionListener(e -> fileTransferDialog.setVisible(true));
        toolbar.add(transfersBtn);
        
        toolbar.addSeparator();
        
        // 
        toolbar.addSeparator();
        
        // Close button
        JButton closeBtn = new JButton("Đóng kết nối");
        closeBtn.addActionListener(e -> dispose());
        toolbar.add(closeBtn);
        
        return toolbar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        statusLabel = new JLabel("Đã kết nối");
        statusLabel.setForeground(new Color(0, 128, 0));
        statusBar.add(statusLabel);
        
        statusBar.add(new JLabel(" | "));
        
        fpsLabel = new JLabel("FPS: 0");
        statusBar.add(fpsLabel);
        
        return statusBar;
    }
    
    private void showActionsMenu(JButton button) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem sendCtrlAltDel = new JMenuItem("Gửi Ctrl+Alt+Del");
        sendCtrlAltDel.addActionListener(e -> sendCtrlAltDel());
        menu.add(sendCtrlAltDel);
        
        JMenuItem refresh = new JMenuItem("Làm mới");
        refresh.addActionListener(e -> client.requestScreen());
        menu.add(refresh);
        
        menu.show(button, 0, button.getHeight());
    }
    
    private void showViewMenu(JButton button) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem fitToWindow = new JMenuItem("Vừa với cửa sổ");
        fitToWindow.addActionListener(e -> fitScreenToWindow());
        menu.add(fitToWindow);
        
        JMenuItem originalSize = new JMenuItem("Kích thước gốc");
        originalSize.addActionListener(e -> setOriginalSize());
        menu.add(originalSize);
        
        menu.show(button, 0, button.getHeight());
    }
    
    private void addInputListeners() {
        // Mouse listener
        screenLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Request focus để bàn phím hoạt động
                screenLabel.requestFocusInWindow();
                
                if (currentScreen != null) {
                    Point scaledPoint = getScaledPoint(e.getPoint());
                    int button = convertMouseButton(e.getButton());
                    MouseEventData mouseData = new MouseEventData("PRESS", 
                        scaledPoint.x, scaledPoint.y, button);
                    client.sendMouseEvent(mouseData);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentScreen != null) {
                    Point scaledPoint = getScaledPoint(e.getPoint());
                    int button = convertMouseButton(e.getButton());
                    MouseEventData mouseData = new MouseEventData("RELEASE", 
                        scaledPoint.x, scaledPoint.y, button);
                    client.sendMouseEvent(mouseData);
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                // Request focus khi chuột vào vùng hiển thị
                screenLabel.requestFocusInWindow();
            }
        });
        
        // Mouse motion listener
        screenLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentScreen != null) {
                    Point scaledPoint = getScaledPoint(e.getPoint());
                    MouseEventData mouseData = new MouseEventData("MOVE", 
                        scaledPoint.x, scaledPoint.y, 0);
                    client.sendMouseEvent(mouseData);
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentScreen != null) {
                    Point scaledPoint = getScaledPoint(e.getPoint());
                    MouseEventData mouseData = new MouseEventData("MOVE", 
                        scaledPoint.x, scaledPoint.y, 0);
                    client.sendMouseEvent(mouseData);
                }
            }
        });
        
        // Mouse wheel listener
        screenLabel.addMouseWheelListener(e -> {
            if (currentScreen != null) {
                MouseEventData mouseData = new MouseEventData("WHEEL", 
                    e.getWheelRotation());
                client.sendMouseEvent(mouseData);
            }
        });
        
        screenLabel.requestFocusInWindow();
    }
    
    private void setupGlobalKeyListener() {
        // Global key listener để bắt phím toàn bộ window
        globalKeyListener = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Chỉ xử lý nếu window đang active và connected
                if (!RemoteDesktopGUI.this.isActive() || !client.isConnected()) {
                    return false;
                }
                
                int id = e.getID();
                
                if (id == KeyEvent.KEY_PRESSED) {
                    KeyboardEventData keyData = new KeyboardEventData("PRESS", e.getKeyCode());
                    client.sendKeyboardEvent(keyData);
                    return false; // Không consume event (để UI vẫn hoạt động)
                } else if (id == KeyEvent.KEY_RELEASED) {
                    KeyboardEventData keyData = new KeyboardEventData("RELEASE", e.getKeyCode());
                    client.sendKeyboardEvent(keyData);
                    return false;
                } else if (id == KeyEvent.KEY_TYPED) {
                    // Gửi ký tự đã type (hỗ trợ Unicode)
                    char typedChar = e.getKeyChar();
                    if (typedChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(typedChar)) {
                        KeyboardEventData keyData = new KeyboardEventData("TYPED", 0, typedChar);
                        client.sendKeyboardEvent(keyData);
                    }
                    return false;
                }
                
                return false;
            }
        };
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(globalKeyListener);
    }
    
    private void removeGlobalKeyListener() {
        if (globalKeyListener != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(globalKeyListener);
        }
    }
    
    private Point getScaledPoint(Point labelPoint) {
        if (currentScreen == null) return labelPoint;
        
        Icon icon = screenLabel.getIcon();
        if (icon == null) return labelPoint;
        
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        int labelWidth = screenLabel.getWidth();
        int labelHeight = screenLabel.getHeight();
        
        // Tính toán offset (icon được căn giữa trong label)
        int offsetX = (labelWidth - iconWidth) / 2;
        int offsetY = (labelHeight - iconHeight) / 2;
        
        // Điều chỉnh tọa độ với offset
        int adjustedX = labelPoint.x - offsetX;
        int adjustedY = labelPoint.y - offsetY;
        
        // Kiểm tra nếu click ngoài vùng icon
        if (adjustedX < 0 || adjustedX >= iconWidth || 
            adjustedY < 0 || adjustedY >= iconHeight) {
            return new Point(0, 0);
        }
        
        // Scale về kích thước thực của màn hình
        double scaleX = (double) currentScreen.getWidth() / iconWidth;
        double scaleY = (double) currentScreen.getHeight() / iconHeight;
        
        int realX = (int) Math.round(adjustedX * scaleX);
        int realY = (int) Math.round(adjustedY * scaleY);
        
        // Đảm bảo tọa độ trong phạm vi màn hình
        realX = Math.max(0, Math.min(realX, currentScreen.getWidth() - 1));
        realY = Math.max(0, Math.min(realY, currentScreen.getHeight() - 1));
        
        return new Point(realX, realY);
    }
    
    private int convertMouseButton(int button) {
        switch (button) {
            case MouseEvent.BUTTON1:
                return InputEvent.BUTTON1_DOWN_MASK;
            case MouseEvent.BUTTON2:
                return InputEvent.BUTTON2_DOWN_MASK;
            case MouseEvent.BUTTON3:
                return InputEvent.BUTTON3_DOWN_MASK;
            default:
                return InputEvent.BUTTON1_DOWN_MASK;
        }
    }
    
    public void updateScreen(BufferedImage screen) {
        this.currentScreen = screen;
        
        // Tính FPS
        frameCount++;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        
        if (elapsed >= 1000) {
            currentFps = frameCount / (elapsed / 1000.0);
            fpsLabel.setText(String.format("FPS: %.1f", currentFps));
            frameCount = 0;
            lastUpdateTime = currentTime;
        }
        
        // Hiển thị màn hình theo view mode hiện tại
        if (isFitToWindow) {
            fitScreenToWindow();
        } else {
            ImageIcon icon = new ImageIcon(screen);
            screenLabel.setIcon(icon);
            screenLabel.revalidate();
        }
    }
    
    private void fitScreenToWindow() {
        isFitToWindow = true;
        if (currentScreen != null) {
            int panelWidth = screenPanel.getWidth();
            int panelHeight = screenPanel.getHeight();
            
            double scaleX = (double) panelWidth / currentScreen.getWidth();
            double scaleY = (double) panelHeight / currentScreen.getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            int scaledWidth = (int) (currentScreen.getWidth() * scale);
            int scaledHeight = (int) (currentScreen.getHeight() * scale);
            
            Image scaledImage = currentScreen.getScaledInstance(
                scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            screenLabel.setIcon(new ImageIcon(scaledImage));
        }
    }
    
    private void setOriginalSize() {
        isFitToWindow = false;
        if (currentScreen != null) {
            screenLabel.setIcon(new ImageIcon(currentScreen));
        }
    }
    
    private void sendCtrlAltDel() {
        // Gửi Ctrl+Alt+Del (mô phỏng)
        client.sendKeyboardEvent(new KeyboardEventData("PRESS", KeyEvent.VK_CONTROL));
        client.sendKeyboardEvent(new KeyboardEventData("PRESS", KeyEvent.VK_ALT));
        client.sendKeyboardEvent(new KeyboardEventData("PRESS", KeyEvent.VK_DELETE));
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        client.sendKeyboardEvent(new KeyboardEventData("RELEASE", KeyEvent.VK_DELETE));
        client.sendKeyboardEvent(new KeyboardEventData("RELEASE", KeyEvent.VK_ALT));
        client.sendKeyboardEvent(new KeyboardEventData("RELEASE", KeyEvent.VK_CONTROL));
    }
    
    private void startScreenReceiver() {
        screenReceiver = new ScreenReceiver(client, this);
        screenReceiver.setFileTransferManager(fileTransferManager);
        receiverThread = new Thread(screenReceiver);
        receiverThread.start();
        statusLabel.setText("Đang nhận màn hình...");
        
        setupGlobalKeyListener();
    }
    
    private void stopScreenReceiver() {
        if (screenReceiver != null) {
            screenReceiver.stop();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }
    
    private void setupFileTransferListener() {
        fileTransferManager.setListener(new FileTransferManager.FileTransferListener() {
            @Override
            public void onTransferStarted(String fileId, String fileName, long fileSize, boolean isSending) {
                fileTransferDialog.addTransfer(fileId, fileName, fileSize, isSending);
            }
            
            @Override
            public void onTransferProgress(String fileId, int progress) {
                fileTransferDialog.updateProgress(fileId, progress);
            }
            
            @Override
            public void onTransferCompleted(String fileId, String fileName) {
                fileTransferDialog.completeTransfer(fileId, "Completed: " + fileName);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(RemoteDesktopGUI.this,
                        "File transfer completed: " + fileName,
                        "Transfer Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
            @Override
            public void onTransferFailed(String fileId, String reason) {
                fileTransferDialog.failTransfer(fileId, reason);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(RemoteDesktopGUI.this,
                        "File transfer failed: " + reason,
                        "Transfer Failed",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Send");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Kiểm tra kích thước file (giới hạn 100MB)
            if (file.length() > 100 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                    "File quá lớn! Giới hạn 100MB.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Send file: " + file.getName() + " (" + formatFileSize(file.length()) + ")?",
                "Confirm Send",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                fileTransferManager.sendFile(file);
            }
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * Gửi chat message
     */
    private void sendChatMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        ChatData chatData = new ChatData(client.getClientId(), message, System.currentTimeMillis());
        Message chatMessage = new Message("CHAT_MESSAGE", chatData);
        client.sendMessage(chatMessage);
        
        // Hiển thị message của chính mình
        handleChatMessage(chatData);
    }
    
    /**
     * Xử lý chat message nhận được
     */
    public void handleChatMessage(ChatData chatData) {
        if (chatPanel != null) {
            chatPanel.appendMessage(chatData);
        }
    }
}
