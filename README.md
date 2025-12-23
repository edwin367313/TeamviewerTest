# TeamViewer 2.0 (Java Socket Project)

Dự án mô phỏng phần mềm điều khiển máy tính từ xa TeamViewer, sử dụng Java Socket và mô hình Relay Server.

## Tính năng
- Điều khiển chuột và bàn phím từ xa.
- Xem màn hình thời gian thực (Screen Sharing).
- Hỗ trợ 2 chế độ kết nối:
  1. **LAN Mode (P2P):** Kết nối trực tiếp qua IP.
  2. **Relay Mode (Docker):** Kết nối qua Server trung gian, hỗ trợ vượt tường lửa/NAT.

## Yêu cầu hệ thống
- Java Development Kit (JDK) 8 trở lên.
- Docker (nếu muốn chạy Relay Server).

## Cấu trúc dự án
- `src/`: Chứa mã nguồn Java (`Client.java`, `Server.java`, `RelayServer.java`, ...).
- `bin/`: Chứa các file `.class` sau khi biên dịch.
- `compile.bat`: Script biên dịch.
- `run.bat`: Script chạy ứng dụng.
- `docker-run.bat`: Script chạy Relay Server trên Docker.
- `Dockerfile`: Cấu hình Docker cho Relay Server.

## Hướng dẫn cài đặt & Chạy

### Bước 1: Biên dịch mã nguồn
Chạy file `compile.bat` để biên dịch toàn bộ project.

### Bước 2: Chạy Relay Server (Khuyên dùng)
Để các máy có thể kết nối với nhau dễ dàng mà không cần biết IP, hãy chạy Relay Server trên Docker.
1. Cài đặt Docker Desktop.
2. Chạy file `docker-run.bat`.
3. Server sẽ lắng nghe tại port `5900`.

### Bước 3: Chạy ứng dụng TeamViewer
Chạy file `run.bat` trên cả 2 máy tính.

#### Kịch bản 1: Máy bị điều khiển (Host)
1. Mở ứng dụng.
2. Tích chọn **"Sử dụng Relay Server (Docker)"**.
3. Nhập IP của máy chạy Docker (nếu chạy cùng máy thì để `localhost`).
4. Nhấn **"Khởi động Server"**.
5. Gửi **ID** (6 số) hiện trên màn hình cho đối tác.

#### Kịch bản 2: Máy điều khiển (Client)
1. Mở ứng dụng.
2. Tích chọn **"Sử dụng Relay Server (Docker)"**.
3. Nhập IP của máy chạy Docker.
4. Nhập **ID đối tác** vào ô bên trên.
5. Nhấn **"Kết nối"**.

## Lưu ý
- Nếu chạy trong mạng LAN mà không có Docker, bạn có thể bỏ chọn "Sử dụng Relay Server" và nhập IP trực tiếp của máy kia để kết nối.
- Hiệu năng truyền hình ảnh phụ thuộc vào tốc độ mạng.
