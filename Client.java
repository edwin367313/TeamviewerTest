import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientId;
    private boolean connected;
    
    public Client() {
        connected = false;
    }
    
    /**
     * Kết nối đến server
     */
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Nhận client ID từ server
            Message connectionMsg = (Message) in.readObject();
            if (connectionMsg.getType().equals("CONNECTION")) {
                clientId = (String) connectionMsg.getData();
                connected = true;
                System.out.println("Đã kết nối đến server. Client ID: " + clientId);
                return true;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Không thể kết nối đến server: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Yêu cầu màn hình từ server
     */
    public void requestScreen() {
        if (!connected) return;
        
        try {
            Message request = new Message("REQUEST_SCREEN", null);
            out.writeObject(request);
            out.flush();
        } catch (IOException e) {
            System.err.println("Lỗi yêu cầu màn hình: " + e.getMessage());
        }
    }
    
    /**
     * Gửi sự kiện chuột
     */
    public void sendMouseEvent(MouseEventData mouseData) {
        if (!connected) return;
        
        try {
            Message message = new Message("MOUSE_EVENT", mouseData);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Lỗi gửi sự kiện chuột: " + e.getMessage());
        }
    }
    
    /**
     * Gửi sự kiện bàn phím
     */
    public void sendKeyboardEvent(KeyboardEventData keyData) {
        if (!connected) return;
        
        try {
            Message message = new Message("KEYBOARD_EVENT", keyData);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Lỗi gửi sự kiện bàn phím: " + e.getMessage());
        }
    }
    
    /**
     * Nhận dữ liệu màn hình
     */
    public Message receiveMessage() {
        if (!connected) return null;
        
        try {
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi nhận message: " + e.getMessage());
            disconnect();
        }
        return null;
    }
    
    /**
     * Ngắt kết nối
     */
    public void disconnect() {
        if (!connected) return;
        
        try {
            Message disconnectMsg = new Message("DISCONNECT", null);
            out.writeObject(disconnectMsg);
            out.flush();
            
            connected = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            System.out.println("Đã ngắt kết nối");
        } catch (IOException e) {
            System.err.println("Lỗi khi ngắt kết nối: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public ObjectInputStream getInputStream() {
        return in;
    }
}
