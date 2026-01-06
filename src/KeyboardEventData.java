import java.io.Serializable;

/**
 * Dữ liệu sự kiện bàn phím
 */
public class KeyboardEventData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType; // PRESS, RELEASE, TYPED
    private int keyCode;
    private char keyChar;
    
    public KeyboardEventData(String eventType, int keyCode) {
        this.eventType = eventType;
        this.keyCode = keyCode;
        this.keyChar = '\0';
    }
    
    public KeyboardEventData(String eventType, int keyCode, char keyChar) {
        this.eventType = eventType;
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
    
    public char getKeyChar() {
        return keyChar;
    }
}
