import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.LinkedList;

/**
 * Panel chat có thể thu gọn/mở rộng với spam protection
 */
public class CollapsibleChatPanel extends JPanel {
    private static final int COLLAPSED_HEIGHT = 45;
    private static final int EXPANDED_HEIGHT = 250;
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int SPAM_MESSAGE_COUNT = 50;
    private static final long SPAM_TIME_WINDOW = 10000; // 10 seconds
    private static final long FREEZE_DURATION = 10000; // 10 seconds
    
    private JTextPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton toggleButton;
    private JButton saveButton;
    private boolean isExpanded = false;
    
    private String clientId;
    private ChatMessageSender messageSender;
    
    // Spam protection
    private LinkedList<Long> messageTimestamps;
    private javax.swing.Timer freezeTimer;
    private boolean isFrozen = false;
    
    public interface ChatMessageSender {
        void sendChatMessage(String message);
    }
    
    public CollapsibleChatPanel(String clientId, ChatMessageSender messageSender) {
        this.clientId = clientId;
        this.messageSender = messageSender;
        this.messageTimestamps = new LinkedList<>();
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        initComponents();
        setCollapsed(true);
    }
    
    private void initComponents() {
        // Top panel - Control buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        topPanel.setBackground(new Color(240, 240, 240));
        
        toggleButton = new JButton("▲");
        toggleButton.setFont(new Font("Arial", Font.BOLD, 12));
        toggleButton.setToolTipText("Mở rộng/Thu gọn chat");
        toggleButton.setPreferredSize(new Dimension(40, 25));
        toggleButton.addActionListener(e -> toggleCollapse());
        topPanel.add(toggleButton);
        
        JLabel titleLabel = new JLabel("Chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(titleLabel);
        
        saveButton = new JButton("💾");
        saveButton.setToolTipText("Lưu lịch sử chat");
        saveButton.setPreferredSize(new Dimension(40, 25));
        saveButton.addActionListener(e -> saveChat());
        topPanel.add(saveButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Chat area (chỉ hiện khi expanded)
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 12));
        inputField.addActionListener(e -> sendMessage());
        
        // Giới hạn độ dài input
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int overLimit = (currentLength + text.length()) - length - MAX_MESSAGE_LENGTH;
                if (overLimit > 0) {
                    text = text.substring(0, text.length() - overLimit);
                }
                if (text.length() > 0) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        sendButton = new JButton("Gửi");
        sendButton.setPreferredSize(new Dimension(60, 25));
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    public void toggleCollapse() {
        setCollapsed(!isExpanded);
    }
    
    public void setCollapsed(boolean collapsed) {
        isExpanded = !collapsed;
        
        if (isExpanded) {
            setPreferredSize(new Dimension(getWidth(), EXPANDED_HEIGHT));
            toggleButton.setText("▼");
            toggleButton.setToolTipText("Thu gọn chat");
            chatArea.setVisible(true);
        } else {
            setPreferredSize(new Dimension(getWidth(), COLLAPSED_HEIGHT));
            toggleButton.setText("▲");
            toggleButton.setToolTipText("Mở rộng chat");
            chatArea.setVisible(false);
        }
        
        revalidate();
        repaint();
        
        // Notify parent container to relayout
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
    
    private void sendMessage() {
        if (isFrozen) {
            return;
        }
        
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Check spam
        if (checkSpam()) {
            freezeInput();
            appendSystemMessage("⏸ Rate limit: Bạn đã gửi quá nhiều tin nhắn. Vui lòng đợi 10 giây.");
            return;
        }
        
        // Gửi message
        if (messageSender != null) {
            messageSender.sendChatMessage(message);
        }
        
        // Clear input
        inputField.setText("");
        
        // Record timestamp
        messageTimestamps.add(System.currentTimeMillis());
    }
    
    private boolean checkSpam() {
        long now = System.currentTimeMillis();
        
        // Xóa timestamps cũ hơn time window
        while (!messageTimestamps.isEmpty() && 
               (now - messageTimestamps.getFirst()) > SPAM_TIME_WINDOW) {
            messageTimestamps.removeFirst();
        }
        
        // Kiểm tra nếu đã gửi >= 50 messages trong 10 giây
        return messageTimestamps.size() >= SPAM_MESSAGE_COUNT;
    }
    
    private void freezeInput() {
        isFrozen = true;
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        inputField.setBackground(Color.LIGHT_GRAY);
        
        // Timer để unfreeze sau 10 giây
        if (freezeTimer != null) {
            freezeTimer.stop();
        }
        
        freezeTimer = new javax.swing.Timer((int) FREEZE_DURATION, e -> {
            unfreezeInput();
        });
        freezeTimer.setRepeats(false);
        freezeTimer.start();
    }
    
    private void unfreezeInput() {
        isFrozen = false;
        inputField.setEnabled(true);
        sendButton.setEnabled(true);
        inputField.setBackground(Color.WHITE);
        messageTimestamps.clear(); // Reset counter
        appendSystemMessage("✓ Bạn có thể gửi tin nhắn lại.");
    }
    
    /**
     * Append chat message từ người dùng
     */
    public void appendMessage(ChatData chatData) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = chatArea.getStyledDocument();
                
                // Style cho timestamp và sender
                Style timeStyle = chatArea.addStyle("TimeStyle", null);
                StyleConstants.setForeground(timeStyle, Color.GRAY);
                StyleConstants.setFontSize(timeStyle, 10);
                
                Style senderStyle = chatArea.addStyle("SenderStyle", null);
                boolean isMe = chatData.getSender().equals(clientId);
                StyleConstants.setForeground(senderStyle, isMe ? new Color(0, 100, 200) : new Color(200, 0, 100));
                StyleConstants.setBold(senderStyle, true);
                
                Style messageStyle = chatArea.addStyle("MessageStyle", null);
                StyleConstants.setForeground(messageStyle, Color.BLACK);
                
                // Append formatted message
                doc.insertString(doc.getLength(), "[" + chatData.getFormattedTime() + "] ", timeStyle);
                doc.insertString(doc.getLength(), chatData.getSender() + ": ", senderStyle);
                doc.insertString(doc.getLength(), chatData.getMessage() + "\n", messageStyle);
                
                // Auto-scroll to bottom
                chatArea.setCaretPosition(doc.getLength());
                
            } catch (BadLocationException e) {
                System.err.println("Lỗi append chat message: " + e.getMessage());
            }
        });
    }
    
    /**
     * Append system message (màu đỏ)
     */
    private void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = chatArea.getStyledDocument();
                
                Style systemStyle = chatArea.addStyle("SystemStyle", null);
                StyleConstants.setForeground(systemStyle, Color.RED);
                StyleConstants.setItalic(systemStyle, true);
                
                doc.insertString(doc.getLength(), "⚠ " + message + "\n", systemStyle);
                chatArea.setCaretPosition(doc.getLength());
                
            } catch (BadLocationException e) {
                System.err.println("Lỗi append system message: " + e.getMessage());
            }
        });
    }
    
    /**
     * Lưu chat history vào file
     */
    private void saveChat() {
        String text = chatArea.getText();
        if (text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không có lịch sử chat để lưu.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        
        // Tên file mặc định
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultFileName = "chat_" + clientId + "_" + sdf.format(new Date()) + ".txt";
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Thêm .txt nếu chưa có extension
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(text);
                JOptionPane.showMessageDialog(this,
                    "Đã lưu lịch sử chat vào:\n" + file.getAbsolutePath(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu file: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Kiểm tra xem có chat content không
     */
    public boolean hasChatContent() {
        return !chatArea.getText().trim().isEmpty();
    }
    
    /**
     * Lấy plain text của chat history
     */
    public String getChatHistory() {
        return chatArea.getText();
    }
}
