import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dữ liệu tin nhắn chat
 */
public class ChatData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sender;
    private String message;
    private long timestamp;
    
    public ChatData(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Format timestamp thành HH:mm:ss
     */
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format đầy đủ để save file: [HH:mm:ss] Sender: Message
     */
    public String toFileFormat() {
        return String.format("[%s] %s: %s", getFormattedTime(), sender, message);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), sender, message);
    }
}
