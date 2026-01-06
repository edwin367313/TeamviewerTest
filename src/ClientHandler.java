import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String clientId;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ScreenCapture screenCapture;
    private RemoteController remoteController;
    private boolean connected;
    
    public ClientHandler(Socket socket, String clientId, Server server) {
        this.socket = socket;
        this.clientId = clientId;
        this.server = server;
        this.connected = true;
        this.screenCapture = new ScreenCapture();
        this.remoteController = new RemoteController();
    }
    
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Gửi client ID
            out.writeObject(new Message("CONNECTION", clientId));
            out.flush();
            
            while (connected) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi xử lý client " + clientId + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(Message message) throws IOException {
        switch (message.getType()) {
            case "REQUEST_SCREEN":
                sendScreenCapture();
                break;
                
            case "MOUSE_EVENT":
                remoteController.handleMouseEvent(message.getData());
                break;
                
            case "KEYBOARD_EVENT":
                remoteController.handleKeyboardEvent(message.getData());
                break;
                
            case "FILE_TRANSFER":
                // Chuyển tiếp message FILE_TRANSFER nguyên vẹn
                out.writeObject(message);
                out.flush();
                break;
                
            case "CHAT_MESSAGE":
                // Chuyển tiếp message CHAT nguyên vẹn
                out.writeObject(message);
                out.flush();
                break;
                
            case "DISCONNECT":
                disconnect();
                break;
                
            default:
                System.out.println("Loại message không xác định: " + message.getType());
        }
    }
    
    private void sendScreenCapture() throws IOException {
        byte[] imageData = screenCapture.captureScreen();
        Message response = new Message("SCREEN_DATA", imageData);
        out.writeObject(response);
        out.flush();
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Lỗi khi ngắt kết nối: " + e.getMessage());
        }
        server.removeClient(clientId);
    }
    
    public String getClientId() {
        return clientId;
    }
}
