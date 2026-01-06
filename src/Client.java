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
     * Kết nối đến server (Direct IP)
     */
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            return setupStreams();
        } catch (IOException e) {
            System.err.println("Không thể kết nối đến server: " + e.getMessage());
        }
        return false;
    }

    /**
     * Kết nối qua Relay Server
     */
    public boolean connectRelay(String relayIp, int relayPort, String targetId) {
        try {
            socket = new Socket(relayIp, relayPort);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            // Gửi lệnh kết nối
            dos.writeUTF("CONNECT " + targetId);

            // Chờ phản hồi
            String response = dis.readUTF();
            if ("OK".equals(response)) {
                System.out.println("Relay báo kết nối thành công tới " + targetId);
                return setupStreams();
            } else {
                System.err.println("Relay từ chối: " + response);
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối Relay: " + e.getMessage());
        }
        return false;
    }

    private boolean setupStreams() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Nhận client ID từ server (Handshake của ứng dụng)
            Message connectionMsg = (Message) in.readObject();
            if (connectionMsg.getType().equals("CONNECTION")) {
                clientId = (String) connectionMsg.getData();
                connected = true;
                System.out.println("Đã kết nối đến server. Client ID: " + clientId);
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi thiết lập stream: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Yêu cầu màn hình từ server
     */
    public synchronized void requestScreen() {
        if (!connected) return;
        
        try {
            Message request = new Message("REQUEST_SCREEN", null);
            out.writeObject(request);
            out.flush();
            out.reset(); // Tránh cache object
        } catch (IOException e) {
            System.err.println("Lỗi yêu cầu màn hình: " + e.getMessage());
        }
    }
    
    /**
     * Gửi sự kiện chuột
     */
    public synchronized void sendMouseEvent(MouseEventData mouseData) {
        if (!connected) return;
        
        try {
            Message message = new Message("MOUSE_EVENT", mouseData);
            out.writeObject(message);
            out.flush();
            out.reset(); // Tránh cache object
        } catch (IOException e) {
            System.err.println("Lỗi gửi sự kiện chuột: " + e.getMessage());
        }
    }
    
    /**
     * Gửi sự kiện bàn phím
     */
    public synchronized void sendKeyboardEvent(KeyboardEventData keyData) {
        if (!connected) return;
        
        try {
            Message message = new Message("KEYBOARD_EVENT", keyData);
            out.writeObject(message);
            out.flush();
            out.reset(); // Tránh cache object
        } catch (IOException e) {
            System.err.println("Lỗi gửi sự kiện bàn phím: " + e.getMessage());
        }
    }
    
    /**
     * Gửi message tổng quát
     */
    public synchronized void sendMessage(Message message) {
        if (!connected) return;
        
        try {
            out.writeObject(message);
            out.flush();
            out.reset(); // Tránh cache object
        } catch (IOException e) {
            System.err.println("Lỗi gửi message: " + e.getMessage());
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
