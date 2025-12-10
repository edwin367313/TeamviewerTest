# TeamViewer 2.0 - Ứng dụng Điều khiển Từ xa

## Mô tả
TeamViewer 2.0 là ứng dụng điều khiển máy tính từ xa được viết bằng Java, sử dụng mô hình Client-Server. Ứng dụng cho phép xem và điều khiển màn hình của máy tính khác qua mạng.

## Tính năng

### 🖥️ Điều khiển từ xa
- Xem màn hình máy tính từ xa theo thời gian thực
- Điều khiển chuột và bàn phím từ xa
- Hỗ trợ chuột trái, phải, giữa và cuộn chuột
- Gửi phím tắt (Ctrl+Alt+Del, v.v.)

### 🔌 Kết nối
- Kết nối dựa trên địa chỉ IP
- Tự động tạo ID cho mỗi máy
- Hỗ trợ nhiều client đồng thời
- Tự động ngắt kết nối khi đóng ứng dụng

### 🎨 Giao diện
- Giao diện đồ họa giống TeamViewer
- Hiển thị FPS (Frames Per Second)
- Tùy chọn thu phóng màn hình
- Thanh công cụ với các chức năng thường dùng

## Cấu trúc dự án

```
teamviewer2.0/
├── Server.java              # Server chính
├── ClientHandler.java       # Xử lý client
├── Client.java              # Client kết nối
├── TeamViewerGUI.java       # Giao diện chính
├── RemoteDesktopGUI.java    # Giao diện điều khiển từ xa
├── ScreenCapture.java       # Chụp màn hình
├── ScreenReceiver.java      # Nhận dữ liệu màn hình
├── RemoteController.java    # Điều khiển chuột/bàn phím
├── Message.java             # Định dạng message
├── MouseEventData.java      # Dữ liệu sự kiện chuột
└── KeyboardEventData.java   # Dữ liệu sự kiện bàn phím
```

## Yêu cầu hệ thống

- Java Development Kit (JDK) 8 trở lên
- Windows/Linux/MacOS
- Kết nối mạng LAN hoặc Internet

## Cách sử dụng

### 1. Biên dịch ứng dụng

```bash
javac *.java
```

### 2. Chạy ứng dụng

**Chạy giao diện chính:**
```bash
java TeamViewerGUI
```

**Hoặc chạy riêng Server:**
```bash
java Server
```

### 3. Kết nối

#### Máy chủ (Máy bị điều khiển):
1. Chạy TeamViewerGUI
2. Nhấn "Khởi động Server"
3. Copy địa chỉ IP hiển thị trong "ID của bạn"
4. Gửi ID này cho người muốn điều khiển

#### Máy khách (Máy điều khiển):
1. Chạy TeamViewerGUI
2. Nhập ID đối tác vào ô "ID đối tác"
3. Nhấn "Kết nối"
4. Cửa sổ điều khiển từ xa sẽ mở ra

## Cổng mạng

Ứng dụng sử dụng **cổng 5900** (cổng VNC tiêu chuẩn)

## Bảo mật

⚠️ **Lưu ý**: Đây là phiên bản demo cho mục đích học tập. Trong môi trường sản xuất, cần thêm:
- Mã hóa dữ liệu (SSL/TLS)
- Xác thực người dùng (password/token)
- Giới hạn quyền truy cập
- Logging và monitoring

## Khắc phục sự cố

### Không kết nối được
- Kiểm tra firewall có chặn cổng 5900 không
- Đảm bảo hai máy trong cùng mạng hoặc có thể ping được nhau
- Kiểm tra địa chỉ IP nhập đúng chưa

### Màn hình lag
- Giảm độ phân giải màn hình
- Cải thiện kết nối mạng
- Đóng các ứng dụng đang chạy không cần thiết

### Lỗi Robot class
- Chạy với quyền administrator/root
- Kiểm tra Java có quyền điều khiển hệ thống không

## Phát triển thêm

Có thể mở rộng với:
- Chat giữa client và server
- Truyền file
- Ghi âm/ghi hình phiên làm việc
- Hỗ trợ nhiều màn hình
- Nén dữ liệu để tăng tốc độ
- Mã hóa kết nối

## Giấy phép

Dự án học tập - Sử dụng tự do cho mục đích giáo dục

## Tác giả

Bài tập Lập trình mạng - TeamViewer 2.0
