import java.io.Serializable;

/**
 * Dữ liệu sự kiện chuột
 */
public class MouseEventData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType; // MOVE, PRESS, RELEASE, CLICK, WHEEL
    private int x;
    private int y;
    private int button; // InputEvent.BUTTON1_DOWN_MASK, etc.
    private int wheelRotation;
    
    public MouseEventData(String eventType, int x, int y, int button) {
        this.eventType = eventType;
        this.x = x;
        this.y = y;
        this.button = button;
        this.wheelRotation = 0;
    }
    
    public MouseEventData(String eventType, int wheelRotation) {
        this.eventType = eventType;
        this.wheelRotation = wheelRotation;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getButton() {
        return button;
    }
    
    public int getWheelRotation() {
        return wheelRotation;
    }
}
