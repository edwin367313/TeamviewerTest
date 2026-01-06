import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String clientId;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ScreenCapture screenCapture;
    private RemoteController remoteController;
    private boolean connected;
    private Map<String, FileReceiveInfo> receivingFiles;
    
    public ClientHandler(Socket socket, String clientId, Server server) {
        this.socket = socket;
        this.clientId = clientId;
        this.server = server;
        this.connected = true;
        this.screenCapture = new ScreenCapture();
        this.remoteController = new RemoteController();
        this.receivingFiles = new HashMap<>();
    }
    
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Gửi client ID
            out.writeObject(new Message("CONNECTION", clientId));
            out.flush();
            
            while (connected) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi xử lý client " + clientId + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(Message message) throws IOException {
        switch (message.getType()) {
            case "REQUEST_SCREEN":
                sendScreenCapture();
                break;
                
            case "MOUSE_EVENT":
                remoteController.handleMouseEvent(message.getData());
                break;
                
            case "KEYBOARD_EVENT":
                remoteController.handleKeyboardEvent(message.getData());
                break;
                
            case "FILE_TRANSFER":
                // Xử lý FILE_TRANSFER trên Server
                handleFileTransfer((FileTransferData) message.getData());
                break;
                
            case "DISCONNECT":
                disconnect();
                break;
                
            default:
                System.out.println("Loại message không xác định: " + message.getType());
        }
    }
    
    private void sendScreenCapture() throws IOException {
        byte[] imageData = screenCapture.captureScreen();
        Message response = new Message("SCREEN_DATA", imageData);
        out.writeObject(response);
        out.flush();
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Lỗi khi ngắt kết nối: " + e.getMessage());
        }
        server.removeClient(clientId);
    }
    
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Xử lý FILE_TRANSFER trên Server side
     */
    private void handleFileTransfer(FileTransferData data) throws IOException {
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
            case "REJECT":
                // Forward response về Client
                Message response = new Message("FILE_TRANSFER", data);
                out.writeObject(response);
                out.flush();
                break;
        }
    }
    
    /**
     * Server nhận request gửi file từ Client
     */
    private void handleFileStart(FileTransferData data) {
        String fileId = data.getFileId();
        String fileName = data.getFileName();
        long fileSize = data.getFileSize();
        
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(null,
                "Nhận file '" + fileName + "' (" + data.getFileSizeFormatted() + ") từ Client?",
                "File Transfer Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Chọn nơi lưu
                JFileChooser fileChooser = new JFileChooser();
                String userHome = System.getProperty("user.home");
                File downloadsFolder = new File(userHome, "Downloads");
                if (downloadsFolder.exists()) {
                    fileChooser.setCurrentDirectory(downloadsFolder);
                }
                fileChooser.setSelectedFile(new File(fileName));
                fileChooser.setDialogTitle("Lưu file - " + fileName);
                
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fileChooser.getSelectedFile();
                    try {
                        FileReceiveInfo receiveInfo = new FileReceiveInfo(saveFile, fileSize);
                        receivingFiles.put(fileId, receiveInfo);
                        
                        // Gửi ACCEPT về Client
                        FileTransferData acceptData = new FileTransferData("ACCEPT", fileId);
                        Message acceptMsg = new Message("FILE_TRANSFER", acceptData);
                        out.writeObject(acceptMsg);
                        out.flush();
                        
                        System.out.println("Chấp nhận nhận file: " + fileName);
                        
                    } catch (IOException ex) {
                        sendReject(fileId);
                        JOptionPane.showMessageDialog(null,
                            "Không thể tạo file: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    sendReject(fileId);
                }
            } else {
                sendReject(fileId);
            }
        });
    }
    
    private void sendReject(String fileId) {
        try {
            FileTransferData rejectData = new FileTransferData("REJECT", fileId);
            Message rejectMsg = new Message("FILE_TRANSFER", rejectData);
            out.writeObject(rejectMsg);
            out.flush();
            System.out.println("Từ chối nhận file: " + fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void handleFileChunk(FileTransferData data) {
        String fileId = data.getFileId();
        FileReceiveInfo receiveInfo = receivingFiles.get(fileId);
        
        if (receiveInfo != null) {
            try {
                receiveInfo.writeChunk(data.getFileData());
                int progress = (int) ((data.getChunkIndex() * 100.0) / data.getTotalChunks());
                System.out.println("Đang nhận file: " + progress + "%");
            } catch (IOException e) {
                System.err.println("Lỗi ghi file chunk: " + e.getMessage());
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
                System.out.println("Nhận file thành công: " + receiveInfo.getFile().getAbsolutePath());
                
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "Nhận file thành công!\nĐã lưu tại: " + receiveInfo.getFile().getAbsolutePath(),
                        "Hoàn thành",
                        JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (IOException e) {
                System.err.println("Lỗi đóng file: " + e.getMessage());
            } finally {
                receivingFiles.remove(fileId);
            }
        }
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
