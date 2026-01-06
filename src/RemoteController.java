import java.awt.*;
import java.awt.event.KeyEvent;

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
            } else if (keyData.getEventType().equals("TYPED")) {
                // Xử lý ký tự Unicode bằng cách type trực tiếp
                typeCharacter(keyData.getKeyChar());
            }
        }
    }
    

    private void typeCharacter(char character) {
        if (character == '\0') return;
        
        try {
            // Đối với ký tự đặc biệt và Unicode, sử dụng Clipboard
            if (character > 127 || !Character.isDefined(character)) {
                typeUnicodeCharacter(character);
            } else {
                // Ký tự ASCII chuẩn
                boolean needShift = Character.isUpperCase(character);
                int keyCode = getKeyCode(character);
                
                if (keyCode != -1) {
                    if (needShift) {
                        robot.keyPress(KeyEvent.VK_SHIFT);
                    }
                    robot.keyPress(keyCode);
                    robot.delay(10);
                    robot.keyRelease(keyCode);
                    if (needShift) {
                        robot.keyRelease(KeyEvent.VK_SHIFT);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi type ký tự: " + e.getMessage());
        }
    }
    
    /**
     * Type Unicode character sử dụng Clipboard
     */
    private void typeUnicodeCharacter(char character) {
        try {
            // Sử dụng Clipboard để paste ký tự Unicode
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(String.valueOf(character));
            java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(stringSelection, null);
            
            // Ctrl+V để paste
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.delay(10);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (Exception e) {
            System.err.println("Lỗi khi paste ký tự Unicode: " + e.getMessage());
        }
    }
    
    private int getKeyCode(char character) {
        // Chuyển về chữ hoa để mapping
        char upper = Character.toUpperCase(character);
        
        // Số
        if (character >= '0' && character <= '9') {
            return KeyEvent.VK_0 + (character - '0');
        }
        
        // Chữ cái
        if (upper >= 'A' && upper <= 'Z') {
            return KeyEvent.VK_A + (upper - 'A');
        }
        
        // Ký tự đặc biệt
        switch (character) {
            case ' ': return KeyEvent.VK_SPACE;
            case '!': return KeyEvent.VK_1; // Shift+1
            case '@': return KeyEvent.VK_2; // Shift+2
            case '#': return KeyEvent.VK_3;
            case '$': return KeyEvent.VK_4;
            case '%': return KeyEvent.VK_5;
            case '^': return KeyEvent.VK_6;
            case '&': return KeyEvent.VK_7;
            case '*': return KeyEvent.VK_8;
            case '(': return KeyEvent.VK_9;
            case ')': return KeyEvent.VK_0;
            case '-': return KeyEvent.VK_MINUS;
            case '_': return KeyEvent.VK_MINUS; // Shift+Minus
            case '=': return KeyEvent.VK_EQUALS;
            case '+': return KeyEvent.VK_EQUALS; // Shift+Equals
            case '[': return KeyEvent.VK_OPEN_BRACKET;
            case '{': return KeyEvent.VK_OPEN_BRACKET; // Shift
            case ']': return KeyEvent.VK_CLOSE_BRACKET;
            case '}': return KeyEvent.VK_CLOSE_BRACKET; // Shift
            case '\\': return KeyEvent.VK_BACK_SLASH;
            case '|': return KeyEvent.VK_BACK_SLASH; // Shift
            case ';': return KeyEvent.VK_SEMICOLON;
            case ':': return KeyEvent.VK_SEMICOLON; // Shift
            case '\'': return KeyEvent.VK_QUOTE;
            case '"': return KeyEvent.VK_QUOTE; // Shift
            case ',': return KeyEvent.VK_COMMA;
            case '<': return KeyEvent.VK_COMMA; // Shift
            case '.': return KeyEvent.VK_PERIOD;
            case '>': return KeyEvent.VK_PERIOD; // Shift
            case '/': return KeyEvent.VK_SLASH;
            case '?': return KeyEvent.VK_SLASH; // Shift
            case '`': return KeyEvent.VK_BACK_QUOTE;
            case '~': return KeyEvent.VK_BACK_QUOTE; // Shift
            default: return -1;
        }
    }
}
