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
    
    public ScreenReceiver(Client client, RemoteDesktopGUI gui) {
        this.client = client;
        this.gui = gui;
        this.running = false;
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
                
                if (message != null && message.getType().equals("SCREEN_DATA")) {
                    byte[] imageData = (byte[]) message.getData();
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
                    
                    if (image != null) {
                        gui.updateScreen(image);
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
