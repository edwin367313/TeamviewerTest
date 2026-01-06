# Cách xem Chat ở máy Server (máy bị điều khiển)

## ⚠️ Vấn đề hiện tại:

Chat **chỉ hiển thị ở RemoteDesktopGUI** (máy Client - người điều khiển).

**Server (máy bị điều khiển) KHÔNG có UI để xem chat!**

## 📝 Chat lưu ở đâu?

- Chat messages được gửi qua message protocol
- Client có chat panel ở góc dưới RemoteDesktopGUI
- Server nhận chat message nhưng **không hiển thị** (chỉ forward qua ClientHandler)

## ✅ Giải pháp tạm thời:

### Khi test trên cùng 1 máy:
1. Chạy 2 TeamViewerGUI instances
2. 1 cái làm Server (click "Khởi động Server")
3. 1 cái làm Client (nhập localhost và "Kết nối")
4. Cả 2 đều thấy chat panel!

### Khi test 2 máy khác nhau:
Server hiện tại **không có chat UI**. Cần làm 1 trong 2:

#### Option A: Server cũng mở TeamViewerGUI
```bash
# Máy Server:
java -cp bin TeamViewerGUI
# Click "Khởi động Server" → có chat panel

# Máy Client:
java -cp bin TeamViewerGUI  
# Nhập IP Server → "Kết nối" → có chat panel
```

#### Option B: Tạo ServerGUI riêng (cần code thêm)
- Tạo file ServerGUI.java
- Có chat panel giống RemoteDesktopGUI
- Lắng nghe CHAT_MESSAGE từ ClientHandler

## 🚀 Workaround nhanh nhất:

**Cả 2 máy đều chạy TeamViewerGUI:**
- Máy 1: Click "Khởi động Server" → có ID + chat panel
- Máy 2: Nhập ID máy 1 → "Kết nối" → có chat panel
- **Cả 2 đều thấy chat!**

## 📊 Tóm tắt:

| Thành phần | Chat UI | Gửi Chat | Nhận Chat |
|---|---|---|---|
| TeamViewerGUI (Server mode) | ✅ Có | ✅ Có | ✅ Có |
| TeamViewerGUI (Client mode) | ✅ Có | ✅ Có | ✅ Có |
| Server.java (standalone) | ❌ Không | ❌ Không | ✅ Nhận (nhưng không hiển thị) |

**Khuyến nghị**: Luôn dùng TeamViewerGUI cho cả 2 máy!
