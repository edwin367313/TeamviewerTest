import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Class quản lý việc gửi và nhận file
 */
public class FileTransferManager {
    private Client client;
    private Map<String, FileReceiveInfo> receivingFiles;
    private Map<String, FileSendInfo> sendingFiles;
    private FileTransferListener listener;
    
    public FileTransferManager(Client client) {
        this.client = client;
        this.receivingFiles = new ConcurrentHashMap<>();
        this.sendingFiles = new ConcurrentHashMap<>();
    }
    
    public void setListener(FileTransferListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gửi file
     */
    public void sendFile(File file) {
        new Thread(() -> {
            try {
                String fileId = UUID.randomUUID().toString();
                long fileSize = file.length();
                String fileName = file.getName();
                
                // Gửi thông báo bắt đầu
                FileTransferData startData = new FileTransferData("START", fileName, fileSize, fileId);
                Message startMsg = new Message("FILE_TRANSFER", startData);
                client.sendMessage(startMsg);
                
                // Đợi chấp nhận
                FileSendInfo sendInfo = new FileSendInfo(file, fileId);
                sendingFiles.put(fileId, sendInfo);
                
                if (listener != null) {
                    listener.onTransferStarted(fileId, fileName, fileSize, true);
                }
                
                // Chờ phản hồi trong 30 giây
                long startTime = System.currentTimeMillis();
                while (!sendInfo.isAccepted() && !sendInfo.isRejected() && 
                       System.currentTimeMillis() - startTime < 30000) {
                    Thread.sleep(100);
                }
                
                if (sendInfo.isRejected()) {
                    if (listener != null) {
                        listener.onTransferFailed(fileId, "Người nhận từ chối");
                    }
                    sendingFiles.remove(fileId);
                    return;
                }
                
                if (!sendInfo.isAccepted()) {
                    if (listener != null) {
                        listener.onTransferFailed(fileId, "Timeout - không nhận được phản hồi");
                    }
                    sendingFiles.remove(fileId);
                    return;
                }
                
                // Gửi file theo chunks
                final int CHUNK_SIZE = 64 * 1024; // 64KB
                byte[] buffer = new byte[CHUNK_SIZE];
                int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
                int chunkIndex = 0;
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                        FileTransferData chunkData = new FileTransferData(
                            "CHUNK", fileId, chunk, chunkIndex, totalChunks);
                        Message chunkMsg = new Message("FILE_TRANSFER", chunkData);
                        client.sendMessage(chunkMsg);
                        
                        chunkIndex++;
                        
                        if (listener != null) {
                            int progress = (int) ((chunkIndex * 100.0) / totalChunks);
                            listener.onTransferProgress(fileId, progress);
                        }
                        
                        // Tránh gửi quá nhanh
                        Thread.sleep(10);
                    }
                }
                
                // Gửi thông báo kết thúc
                FileTransferData endData = new FileTransferData("END", fileId);
                Message endMsg = new Message("FILE_TRANSFER", endData);
                client.sendMessage(endMsg);
                
                if (listener != null) {
                    listener.onTransferCompleted(fileId, fileName);
                }
                
                sendingFiles.remove(fileId);
                
            } catch (Exception e) {
                if (listener != null) {
                    listener.onTransferFailed(file.getName(), e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Xử lý dữ liệu file nhận được
     */
    public void handleFileTransfer(FileTransferData data) {
        String fileId = data.getFileId();
        
        switch (data.getTransferType()) {
            case "START":
                handleFileStart(data);
                break;
                
            case "CHUNK":
                handleFileChunk(data);
                break;
                
            case "END":
                handleFileEnd(data);
                break;
                
            case "ACCEPT":
                handleFileAccept(fileId);
                break;
                
            case "REJECT":
                handleFileReject(fileId);
                break;
        }
    }
    
    private void handleFileStart(FileTransferData data) {
        String fileId = data.getFileId();
        String fileName = data.getFileName();
        long fileSize = data.getFileSize();
        
        // Hỏi người dùng có muốn nhận không
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(null,
                "Nhận file '" + fileName + "' (" + data.getFileSizeFormatted() + ")?",
                "File Transfer Request",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // Chọn nơi lưu - Mặc định lưu vào Downloads folder
                JFileChooser fileChooser = new JFileChooser();
                String userHome = System.getProperty("user.home");
                File downloadsFolder = new File(userHome, "Downloads");
                if (downloadsFolder.exists()) {
                    fileChooser.setCurrentDirectory(downloadsFolder);
                }
                fileChooser.setSelectedFile(new File(fileName));
                fileChooser.setDialogTitle("Save File - " + fileName);
                
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fileChooser.getSelectedFile();
                    try {
                        FileReceiveInfo receiveInfo = new FileReceiveInfo(saveFile, fileSize);
                        receivingFiles.put(fileId, receiveInfo);
                        
                        // Gửi ACCEPT
                        FileTransferData acceptData = new FileTransferData("ACCEPT", fileId);
                        Message acceptMsg = new Message("FILE_TRANSFER", acceptData);
                        client.sendMessage(acceptMsg);
                        
                        if (listener != null) {
                            listener.onTransferStarted(fileId, fileName, fileSize, false);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                            "Không thể tạo file: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        // Gửi REJECT
                        FileTransferData rejectData = new FileTransferData("REJECT", fileId);
                        Message rejectMsg = new Message("FILE_TRANSFER", rejectData);
                        client.sendMessage(rejectMsg);
                    }
                } else {
                    // Gửi REJECT
                    FileTransferData rejectData = new FileTransferData("REJECT", fileId);
                    Message rejectMsg = new Message("FILE_TRANSFER", rejectData);
                    client.sendMessage(rejectMsg);
                }
            } else {
                // Gửi REJECT
                FileTransferData rejectData = new FileTransferData("REJECT", fileId);
                Message rejectMsg = new Message("FILE_TRANSFER", rejectData);
                client.sendMessage(rejectMsg);
            }
        });
    }
    
    private void handleFileChunk(FileTransferData data) {
        String fileId = data.getFileId();
        FileReceiveInfo receiveInfo = receivingFiles.get(fileId);
        
        if (receiveInfo != null) {
            try {
                receiveInfo.writeChunk(data.getFileData());
                
                int progress = (int) ((data.getChunkIndex() * 100.0) / data.getTotalChunks());
                if (listener != null) {
                    listener.onTransferProgress(fileId, progress);
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onTransferFailed(fileId, e.getMessage());
                }
                receivingFiles.remove(fileId);
            }
        }
    }
    
    private void handleFileEnd(FileTransferData data) {
        String fileId = data.getFileId();
        FileReceiveInfo receiveInfo = receivingFiles.get(fileId);
        
        if (receiveInfo != null) {
            try {
                receiveInfo.close();
                if (listener != null) {
                    listener.onTransferCompleted(fileId, receiveInfo.getFile().getName());
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onTransferFailed(fileId, e.getMessage());
                }
            } finally {
                receivingFiles.remove(fileId);
            }
        }
    }
    
    private void handleFileAccept(String fileId) {
        FileSendInfo sendInfo = sendingFiles.get(fileId);
        if (sendInfo != null) {
            sendInfo.setAccepted(true);
        }
    }
    
    private void handleFileReject(String fileId) {
        FileSendInfo sendInfo = sendingFiles.get(fileId);
        if (sendInfo != null) {
            sendInfo.setRejected(true);
        }
    }
    
    /**
     * Interface để lắng nghe sự kiện file transfer
     */
    public interface FileTransferListener {
        void onTransferStarted(String fileId, String fileName, long fileSize, boolean isSending);
        void onTransferProgress(String fileId, int progress);
        void onTransferCompleted(String fileId, String fileName);
        void onTransferFailed(String fileId, String reason);
    }
    
    /**
     * Class lưu thông tin file đang gửi
     */
    private static class FileSendInfo {
        private boolean accepted;
        private boolean rejected;
        
        public FileSendInfo(File file, String fileId) {
        }
        
        public boolean isAccepted() { return accepted; }
        public void setAccepted(boolean accepted) { this.accepted = accepted; }
        public boolean isRejected() { return rejected; }
        public void setRejected(boolean rejected) { this.rejected = rejected; }
    }
    
    /**
     * Class lưu thông tin file đang nhận
     */
    private static class FileReceiveInfo {
        private File file;
        private FileOutputStream fos;
        
        public FileReceiveInfo(File file, long totalSize) throws IOException {
            this.file = file;
            this.fos = new FileOutputStream(file);
        }
        
        public void writeChunk(byte[] data) throws IOException {
            fos.write(data);
        }
        
        public void close() throws IOException {
            fos.close();
        }
        
        public File getFile() {
            return file;
        }
    }
}
