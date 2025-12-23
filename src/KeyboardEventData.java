import java.io.Serializable;

/**
 * Dữ liệu sự kiện bàn phím
 */
public class KeyboardEventData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType; // PRESS, RELEASE
    private int keyCode;
    
    public KeyboardEventData(String eventType, int keyCode) {
        this.eventType = eventType;
        this.keyCode = keyCode;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
}
