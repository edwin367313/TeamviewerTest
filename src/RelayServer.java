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

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Relay Server dang chay tren port " + PORT);
            System.out.println("Cho ket noi tu cac may Client...");

            while (true) {
                Socket socket = serverSocket.accept();
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
                System.out.println("Host da dang ky ID: " + id);
                out.writeUTF("WAITING"); // Báo Host đứng chờ
                // Socket này sẽ được giữ sống trong Map
            } 
            else if ("CONNECT".equals(action)) {
                // Máy Client muốn điều khiển máy Host có ID kia
                Socket hostSocket = waitingHosts.get(id);
                
                if (hostSocket != null && !hostSocket.isClosed()) {
                    System.out.println("Ket noi Client -> Host ID: " + id);
                    waitingHosts.remove(id); // Xóa khỏi hàng chờ
                    
                    // Báo cho cả 2 biết thành công
                    out.writeUTF("OK");
                    DataOutputStream hostOut = new DataOutputStream(hostSocket.getOutputStream());
                    hostOut.writeUTF("OK");

                    // Bắt đầu cầu nối dữ liệu
                    bridgeConnections(socket, hostSocket);
                } else {
                    out.writeUTF("ERROR");
                    System.out.println("Khong tim thay Host ID: " + id);
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Loi handshake: " + e.getMessage());
        }
    }

    private static void bridgeConnections(Socket s1, Socket s2) {
        // Luồng 1: s1 -> s2
        new Thread(() -> copyStream(s1, s2)).start();
        // Luồng 2: s2 -> s1
        new Thread(() -> copyStream(s2, s1)).start();
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
