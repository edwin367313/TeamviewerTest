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
