import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

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
    
    public RemoteDesktopGUI(Client client) {
        this.client = client;
        this.lastUpdateTime = System.currentTimeMillis();
        this.frameCount = 0;
        this.currentFps = 0;
        
        setTitle("TeamViewer 2.0 - Remote Desktop");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        startScreenReceiver();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopScreenReceiver();
            }
        });
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
        
        // Keyboard listener
        screenLabel.setFocusable(true);
        screenLabel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                KeyboardEventData keyData = new KeyboardEventData("PRESS", e.getKeyCode());
                client.sendKeyboardEvent(keyData);
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                KeyboardEventData keyData = new KeyboardEventData("RELEASE", e.getKeyCode());
                client.sendKeyboardEvent(keyData);
            }
        });
        
        screenLabel.requestFocusInWindow();
    }
    
    private Point getScaledPoint(Point labelPoint) {
        if (currentScreen == null) return labelPoint;
        
        Icon icon = screenLabel.getIcon();
        if (icon == null) return labelPoint;
        
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        
        double scaleX = (double) currentScreen.getWidth() / iconWidth;
        double scaleY = (double) currentScreen.getHeight() / iconHeight;
        
        int realX = (int) (labelPoint.x * scaleX);
        int realY = (int) (labelPoint.y * scaleY);
        
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
        
        // Hiển thị màn hình
        ImageIcon icon = new ImageIcon(screen);
        screenLabel.setIcon(icon);
        screenLabel.revalidate();
    }
    
    private void fitScreenToWindow() {
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
        receiverThread = new Thread(screenReceiver);
        receiverThread.start();
        statusLabel.setText("Đang nhận màn hình...");
    }
    
    private void stopScreenReceiver() {
        if (screenReceiver != null) {
            screenReceiver.stop();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }
}
