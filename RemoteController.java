import java.awt.*;

/**
 * Class điều khiển chuột và bàn phím từ xa
 */
public class RemoteController {
    private Robot robot;
    
    public RemoteController() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Không thể khởi tạo Robot: " + e.getMessage());
        }
    }
    
    
    public void handleMouseEvent(Object data) {
        if (data instanceof MouseEventData) {
            MouseEventData mouseData = (MouseEventData) data;
            
            switch (mouseData.getEventType()) {
                case "MOVE":
                    robot.mouseMove(mouseData.getX(), mouseData.getY());
                    break;
                    
                case "PRESS":
                    robot.mouseMove(mouseData.getX(), mouseData.getY());
                    robot.mousePress(mouseData.getButton());
                    break;
                    
                case "RELEASE":
                    robot.mouseRelease(mouseData.getButton());
                    break;
                    
                case "CLICK":
                    robot.mouseMove(mouseData.getX(), mouseData.getY());
                    robot.mousePress(mouseData.getButton());
                    robot.mouseRelease(mouseData.getButton());
                    break;
                    
                case "WHEEL":
                    robot.mouseWheel(mouseData.getWheelRotation());
                    break;
            }
        }
    }
    
   
    public void handleKeyboardEvent(Object data) {
        if (data instanceof KeyboardEventData) {
            KeyboardEventData keyData = (KeyboardEventData) data;
            
            if (keyData.getEventType().equals("PRESS")) {
                robot.keyPress(keyData.getKeyCode());
            } else if (keyData.getEventType().equals("RELEASE")) {
                robot.keyRelease(keyData.getKeyCode());
            }
        }
    }
}
