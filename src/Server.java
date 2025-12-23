import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Server cho ứng dụng TeamViewer 2.0
 * Quản lý kết nối từ client và xử lý các yêu cầu điều khiển từ xa
 */
public class Server {
    private static final int PORT = 5900;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> connectedClients;
    private ExecutorService threadPool;
    private boolean running;
    
    public Server() {
        connectedClients = new ConcurrentHashMap<>();
        threadPool = Executors.newCachedThreadPool();
        running = false;
    }
    
    public void start() throws IOException {
        // Mặc định chạy mode cũ (LAN)
        startLAN();
    }

    public void startLAN() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;
        System.out.println("Server đã khởi động trên port " + PORT);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientId = UUID.randomUUID().toString().substring(0, 8);
                System.out.println("Client mới kết nối: " + clientId);
                
                ClientHandler handler = new ClientHandler(clientSocket, clientId, this);
                connectedClients.put(clientId, handler);
                threadPool.execute(handler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Lỗi chấp nhận kết nối: " + e.getMessage());
                }
            }
        }
    }

    public void startRelay(String relayIp, int relayPort, String myId) {
        running = true;
        System.out.println("Đang kết nối tới Relay Server " + relayIp + ":" + relayPort + " với ID: " + myId);
        
        while (running) {
            try {
                Socket socket = new Socket(relayIp, relayPort);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                // Gửi lệnh đăng ký
                out.writeUTF("REGISTER " + myId);
                
                // Chờ phản hồi
                String response = in.readUTF();
                if ("WAITING".equals(response)) {
                    System.out.println("Đang chờ Client kết nối...");
                    
                    // Block ở đây chờ Client tới
                    // Khi Client tới, Relay sẽ gửi "OK" (hoặc bridge stream luôn)
                    // Tuy nhiên, theo logic RelayServer của ta:
                    // Relay gửi "OK" cho cả 2 khi ghép đôi thành công.
                    
                    String signal = in.readUTF(); // Chờ tín hiệu OK
                    if ("OK".equals(signal)) {
                        System.out.println("Client đã kết nối qua Relay!");
                        
                        // Chuyển socket này cho ClientHandler xử lý
                        // Lưu ý: ClientHandler sẽ tạo ObjectStreams, nên thứ tự rất quan trọng
                        ClientHandler handler = new ClientHandler(socket, myId, this);
                        connectedClients.put(myId, handler);
                        threadPool.execute(handler);
                        
                        // Trong mô hình này, sau khi xử lý xong 1 client, ta lại vòng lại
                        // để kết nối lại Relay (nếu Relay đóng kết nối sau khi xong)
                        // Hoặc nếu muốn hỗ trợ nhiều client cùng lúc, ta cần nhiều connection tới Relay
                        // Để đơn giản, ta chờ handler xử lý xong (hoặc chạy song song nhưng cần logic reconnect)
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Lỗi kết nối Relay: " + e.getMessage());
                    try { Thread.sleep(5000); } catch (InterruptedException ex) {} // Thử lại sau 5s
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler handler : connectedClients.values()) {
                handler.disconnect();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Lỗi khi dừng server: " + e.getMessage());
        }
    }
    
    public void removeClient(String clientId) {
        connectedClients.remove(clientId);
        System.out.println("Client đã ngắt kết nối: " + clientId);
    }
    
    public Map<String, ClientHandler> getConnectedClients() {
        return connectedClients;
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Không thể khởi động server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
