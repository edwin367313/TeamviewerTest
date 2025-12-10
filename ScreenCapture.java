import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Class chụp màn hình máy tính
 */
public class ScreenCapture {
    private Robot robot;
    private Rectangle screenRect;
    
    public ScreenCapture() {
        try {
            robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenRect = new Rectangle(screenSize);
        } catch (AWTException e) {
            System.err.println("Không thể khởi tạo Robot: " + e.getMessage());
        }
    }
    
    /**
     * Chụp toàn bộ màn hình và trả về dưới dạng byte array
     */
    public byte[] captureScreen() {
        try {
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Lỗi chụp màn hình: " + e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Chụp một phần màn hình
     */
    public byte[] captureScreen(int x, int y, int width, int height) {
        try {
            Rectangle rect = new Rectangle(x, y, width, height);
            BufferedImage screenshot = robot.createScreenCapture(rect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Lỗi chụp màn hình: " + e.getMessage());
            return new byte[0];
        }
    }
    
    public Dimension getScreenSize() {
        return new Dimension(screenRect.width, screenRect.height);
    }
}
