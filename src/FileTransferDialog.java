import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

/**
 * Dialog hiển thị tiến trình truyền file
 * Changed to JFrame to support minimize button
 */
public class FileTransferDialog extends JFrame {
    private JPanel transfersPanel;
    private Map<String, TransferItem> transferItems;
    
    public FileTransferDialog(Frame parent) {
        super("File Transfers");
        transferItems = new HashMap<>();
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JLabel headerLabel = new JLabel("File Transfers", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(headerLabel, BorderLayout.NORTH);
        
        // Transfers panel
        transfersPanel = new JPanel();
        transfersPanel.setLayout(new BoxLayout(transfersPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(transfersPanel);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> setVisible(false));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void addTransfer(String fileId, String fileName, long fileSize, boolean isSending) {
        SwingUtilities.invokeLater(() -> {
            TransferItem item = new TransferItem(fileName, fileSize, isSending);
            transferItems.put(fileId, item);
            transfersPanel.add(item);
            transfersPanel.revalidate();
            transfersPanel.repaint();
            
            if (!isVisible()) {
                setVisible(true);
            }
        });
    }
    
    public void updateProgress(String fileId, int progress) {
        SwingUtilities.invokeLater(() -> {
            TransferItem item = transferItems.get(fileId);
            if (item != null) {
                item.updateProgress(progress);
            }
        });
    }
    
    public void completeTransfer(String fileId, String message) {
        SwingUtilities.invokeLater(() -> {
            TransferItem item = transferItems.get(fileId);
            if (item != null) {
                item.complete(message);
            }
        });
    }
    
    public void failTransfer(String fileId, String reason) {
        SwingUtilities.invokeLater(() -> {
            TransferItem item = transferItems.get(fileId);
            if (item != null) {
                item.fail(reason);
            }
        });
    }
    
    /**
     * Panel hiển thị một file transfer
     */
    private static class TransferItem extends JPanel {
        private JLabel fileNameLabel;
        private JLabel statusLabel;
        private JProgressBar progressBar;
        
        public TransferItem(String fileName, long fileSize, boolean isSending) {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.GRAY)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            
            // Icon and info
            JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
            
            Icon icon = UIManager.getIcon("FileView.fileIcon");
            JLabel iconLabel = new JLabel(icon);
            infoPanel.add(iconLabel, BorderLayout.WEST);
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            fileNameLabel = new JLabel(fileName);
            fileNameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            
            statusLabel = new JLabel(isSending ? "Sending..." : "Receiving...");
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            statusLabel.setForeground(Color.GRAY);
            
            textPanel.add(fileNameLabel);
            textPanel.add(statusLabel);
            infoPanel.add(textPanel, BorderLayout.CENTER);
            
            add(infoPanel, BorderLayout.NORTH);
            
            // Progress bar
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setValue(0);
            add(progressBar, BorderLayout.CENTER);
        }
        
        public void updateProgress(int progress) {
            progressBar.setValue(progress);
            statusLabel.setText(progress + "% completed");
        }
        
        public void complete(String message) {
            progressBar.setValue(100);
            statusLabel.setText("✓ " + message);
            statusLabel.setForeground(new Color(0, 128, 0));
        }
        
        public void fail(String reason) {
            statusLabel.setText("✗ Failed: " + reason);
            statusLabel.setForeground(Color.RED);
        }
    }
}
