import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Relay Server - Trung gian kết nối
 * Chạy trên Docker để môi giới giữa 2 máy tính
 */
public class RelayServer {
    private static final int PORT = 5900;
    // Lưu danh sách các máy đang chờ (Host): ID -> Socket
    private static Map<String, Socket> waitingHosts = new ConcurrentHashMap<>();
    private static Map<String, String> activeSessions = new ConcurrentHashMap<>();
    private static int totalConnections = 0;

    public static void main(String[] args) {
        // Thread để in thống kê định kỳ
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // Mỗi 10 giây
                    printStats();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Relay Server dang chay tren port " + PORT);
            System.out.println("Listening on 0.0.0.0:" + PORT);
            System.out.println("Cho ket noi tu cac may Client...");
            System.out.println("=====================================");

            while (true) {
                Socket socket = serverSocket.accept();
                totalConnections++;
                String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                System.out.println("[" + totalConnections + "] Ket noi moi tu: " + clientInfo);
                new Thread(() -> handleHandshake(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleHandshake(Socket socket) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Đọc lệnh: "REGISTER <ID>" hoặc "CONNECT <ID>"
            String command = in.readUTF();
            System.out.println("Nhan lenh: " + command);
            
            String[] parts = command.split(" ");
            String action = parts[0];
            String id = parts.length > 1 ? parts[1] : "";

            if ("REGISTER".equals(action)) {
                // Máy Host đăng ký ID để chờ bị điều khiển
                waitingHosts.put(id, socket);
                String hostInfo = socket.getInetAddress().getHostAddress();
                System.out.println("[REGISTER] Host ID: " + id + " tu " + hostInfo);
                System.out.println("           Tong Host dang cho: " + waitingHosts.size());
                out.writeUTF("WAITING"); // Báo Host đứng chờ
                // Socket này sẽ được giữ sống trong Map
            } 
            else if ("CONNECT".equals(action)) {
                // Máy Client muốn điều khiển máy Host có ID kia
                Socket hostSocket = waitingHosts.get(id);
                
                if (hostSocket != null && !hostSocket.isClosed()) {
                    String clientInfo = socket.getInetAddress().getHostAddress();
                    String hostInfo = hostSocket.getInetAddress().getHostAddress();
                    System.out.println("[CONNECT] Client " + clientInfo + " -> Host ID: " + id + " (" + hostInfo + ")");
                    waitingHosts.remove(id); // Xóa khỏi hàng chờ
                    activeSessions.put(id, clientInfo + " <-> " + hostInfo);
                    System.out.println("          Session active: " + activeSessions.size());
                    
                    // Báo cho cả 2 biết thành công
                    out.writeUTF("OK");
                    DataOutputStream hostOut = new DataOutputStream(hostSocket.getOutputStream());
                    hostOut.writeUTF("OK");

                    // Bắt đầu cầu nối dữ liệu
                    bridgeConnections(socket, hostSocket, id);
                } else {
                    out.writeUTF("ERROR");
                    System.out.println("[ERROR] Khong tim thay Host ID: " + id);
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Loi handshake: " + e.getMessage());
        }
    }

    private static void bridgeConnections(Socket s1, Socket s2, String sessionId) {
        // Luồng 1: s1 -> s2
        new Thread(() -> {
            copyStream(s1, s2);
            activeSessions.remove(sessionId);
            System.out.println("[DISCONNECT] Session " + sessionId + " da ket thuc");
            System.out.println("             Session con lai: " + activeSessions.size());
        }).start();
        // Luồng 2: s2 -> s1
        new Thread(() -> copyStream(s2, s1)).start();
    }

    private static void printStats() {
        System.out.println("===== THONG KE SERVER =====");
        System.out.println("Tong ket noi: " + totalConnections);
        System.out.println("Host dang cho: " + waitingHosts.size());
        if (!waitingHosts.isEmpty()) {
            System.out.println("  Danh sach Host:");
            waitingHosts.forEach((id, socket) -> {
                System.out.println("    - ID: " + id + " (" + socket.getInetAddress().getHostAddress() + ")");
            });
        }
        System.out.println("Session dang hoat dong: " + activeSessions.size());
        if (!activeSessions.isEmpty()) {
            System.out.println("  Danh sach Session:");
            activeSessions.forEach((id, info) -> {
                System.out.println("    - ID: " + id + " | " + info);
            });
        }
        System.out.println("===========================");
    }

    private static void copyStream(Socket input, Socket output) {
        try {
            InputStream in = input.getInputStream();
            OutputStream out = output.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException e) {
            // Một bên ngắt kết nối -> đóng bên kia luôn
            try { input.close(); } catch (Exception ex) {}
            try { output.close(); } catch (Exception ex) {}
        }
    }
}
