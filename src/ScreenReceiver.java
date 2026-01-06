import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Class nhận và xử lý dữ liệu màn hình từ server
 */
public class ScreenReceiver implements Runnable {
    private Client client;
    private RemoteDesktopGUI gui;
    private boolean running;
    private FileTransferManager fileTransferManager;
    
    public ScreenReceiver(Client client, RemoteDesktopGUI gui) {
        this.client = client;
        this.gui = gui;
        this.running = false;
    }
    
    public void setFileTransferManager(FileTransferManager fileTransferManager) {
        this.fileTransferManager = fileTransferManager;
    }
    
    @Override
    public void run() {
        running = true;
        
        while (running && client.isConnected()) {
            try {
                // Yêu cầu màn hình mới
                client.requestScreen();
                
                // Nhận dữ liệu màn hình
                Message message = client.receiveMessage();
                
                if (message != null) {
                    if (message.getType().equals("SCREEN_DATA")) {
                        byte[] imageData = (byte[]) message.getData();
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
                        
                        if (image != null) {
                            gui.updateScreen(image);
                        }
                    } else if (message.getType().equals("FILE_TRANSFER")) {
                        // Chỉ xử lý ACCEPT/REJECT response từ Server
                        if (fileTransferManager != null) {
                            FileTransferData data = (FileTransferData) message.getData();
                            String transferType = data.getTransferType();
                            
                            // Client chỉ nhận ACCEPT hoặc REJECT response
                            if ("ACCEPT".equals(transferType) || "REJECT".equals(transferType)) {
                                fileTransferManager.handleFileTransfer(data);
                            }
                            // Bỏ qua START message (không nên nhận được)
                        }
                    }
                }
                
                // Tạm dừng ngắn để không gây quá tải
                Thread.sleep(50); // ~20 FPS
                
            } catch (IOException e) {
                System.err.println("Lỗi nhận màn hình: " + e.getMessage());
                stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop();
            }
        }
    }
    
    public void stop() {
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}
