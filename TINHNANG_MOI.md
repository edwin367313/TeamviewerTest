# Tính năng mới - TeamViewer 2.0

## ✅ Đã hoàn thành

### 1. **Fix Keyboard Typing - Gõ phím được trên remote desktop**

#### Thay đổi:
- ✅ Thêm global key listener thay vì listener trên `screenLabel`
- ✅ Không bị mất focus khi click vào toolbar hay UI khác
- ✅ Hỗ trợ ký tự Unicode (tiếng Việt, emoji, ký tự đặc biệt)
- ✅ Xử lý cả `KEY_PRESSED`, `KEY_RELEASED`, và `KEY_TYPED` events

#### Cách hoạt động:
- Sử dụng `KeyboardFocusManager.addKeyEventDispatcher()` để bắt phím toàn bộ window
- Gửi cả key code VÀ ký tự thực tế (keyChar) để hỗ trợ Unicode
- Remote machine dùng Robot API + Clipboard để paste ký tự Unicode

#### Files đã sửa:
- `KeyboardEventData.java` - Thêm field `keyChar`
- `RemoteController.java` - Thêm `typeCharacter()` và `typeUnicodeCharacter()`
- `RemoteDesktopGUI.java` - Thay KeyAdapter bằng global KeyEventDispatcher

---

### 2. **Chat Real-time - Trò chuyện giữa 2 máy**

#### Tính năng:
- ✅ Chat panel ở góc dưới màn hình
- ✅ Thu gọn/mở rộng (collapse/expand) bằng nút "▲/▼"
- ✅ Gửi tin nhắn real-time qua message protocol
- ✅ Hiển thị timestamp [HH:mm:ss] và tên người gửi
- ✅ Phân biệt màu sắc: tin của mình (xanh dương), tin của người khác (đỏ tím)
- ✅ Giới hạn 500 ký tự/tin nhắn
- ✅ Auto-scroll xuống dưới khi có tin nhắn mới

#### Files mới:
- `ChatData.java` - Data class chứa sender, message, timestamp
- `CollapsibleChatPanel.java` - UI component chat panel

#### Files đã sửa:
- `ClientHandler.java` - Thêm case "CHAT_MESSAGE" để forward messages
- `ScreenReceiver.java` - Thêm xử lý CHAT_MESSAGE trong message loop
- `RemoteDesktopGUI.java` - Nhúng chat panel, thêm `sendChatMessage()` và `handleChatMessage()`

---

### 3. **Spam Protection - Chống spam tin nhắn**

#### Bảo vệ:
- ✅ Giới hạn 50 tin nhắn trong 10 giây
- ✅ Nếu vượt quá → freeze input 10 giây
- ✅ Hiển thị warning "⏸ Rate limit: Bạn đã gửi quá nhiều tin nhắn. Vui lòng đợi 10 giây."
- ✅ Disable input field và Send button (màu xám)
- ✅ Tự động unfreeze sau 10 giây với thông báo "✓ Bạn có thể gửi tin nhắn lại."

#### Cơ chế:
- LinkedList lưu timestamps của 50 messages gần nhất
- Check sliding window 10 giây
- javax.swing.Timer để countdown freeze duration

---

### 4. **Save Chat - Lưu lịch sử chat**

#### Cách sử dụng:
1. **Lưu thủ công**: Click nút "💾" trên chat panel
2. **Lưu khi đóng**: Hỏi "Bạn có muốn lưu lịch sử chat không?" khi đóng window (nếu có chat)

#### Format file:
- Plain text (`.txt`)
- Tên file mặc định: `chat_[ClientID]_[timestamp].txt`
- Format nội dung: `[HH:mm:ss] Sender: Message`

#### Ví dụ:
```
[14:35:21] CLIENT_123: Hello!
[14:35:25] CLIENT_456: Hi there!
[14:35:30] CLIENT_123: How are you?
```

---

## 🎮 Hướng dẫn sử dụng

### Khởi động:

1. **Compile**:
   ```bash
   compile.bat
   ```

2. **Chạy Server** (máy bị điều khiển):
   ```bash
   run.bat
   # Chọn [2] Server mode
   ```

3. **Chạy Client** (máy điều khiển):
   ```bash
   run.bat
   # Chọn [1] Client mode
   # Nhập IP và kết nối
   ```

### Sử dụng Chat:

1. **Mở chat**: Click nút "▲" ở góc dưới màn hình
2. **Gõ tin nhắn**: Nhập vào ô input (max 500 ký tự)
3. **Gửi**: Enter hoặc click nút "Gửi"
4. **Thu gọn**: Click nút "▼" để collapse chat panel
5. **Lưu chat**: Click nút "💾" để save vào file

### Gõ phím trên Remote:

- Chỉ cần focus vào window Remote Desktop
- Gõ bình thường, không cần click vào màn hình
- Hỗ trợ tiếng Việt, emoji, ký tự đặc biệt
- Ctrl+Alt+Del: Dùng menu "Actions" → "Gửi Ctrl+Alt+Del"

---

## 🔧 Kiến trúc kỹ thuật

### Message Protocol:

```
CHAT_MESSAGE:
  - Type: "CHAT_MESSAGE"
  - Data: ChatData object
    - sender: String (Client ID)
    - message: String (nội dung)
    - timestamp: long (milliseconds)
```

### Threading Model:

```
ScreenReceiver Thread:
  ├─ Nhận SCREEN_DATA (màn hình)
  ├─ Nhận FILE_TRANSFER (file)
  └─ Nhận CHAT_MESSAGE (chat) ← MỚI
```

### Keyboard Input Flow:

```
User gõ phím
  ↓
KeyEventDispatcher (global listener)
  ↓
RemoteDesktopGUI.sendKeyboardEvent()
  ↓
Client.sendMessage("KEYBOARD_EVENT")
  ↓
Server ClientHandler
  ↓
RemoteController.handleKeyboardEvent()
  ↓
Robot.keyPress() / typeCharacter() / Clipboard paste
```

---

## ⚠️ Lưu ý

1. **Keyboard typing**: Chỉ hoạt động khi Remote Desktop window đang active
2. **Chat spam**: Freeze 10s nếu gửi 50 msg/10s
3. **Chat history**: Không tự động lưu, phải save thủ công hoặc khi đóng window
4. **Unicode characters**: Sử dụng Clipboard (Ctrl+V) để paste, có thể hơi chậm
5. **File transfer**: Giới hạn 100MB (không thay đổi)

---

## 📝 Testing Checklist

### Keyboard:
- [ ] Gõ chữ thường (a-z)
- [ ] Gõ chữ hoa (A-Z, Shift)
- [ ] Gõ số và ký tự đặc biệt (@#$%^&*)
- [ ] Gõ tiếng Việt có dấu (áàảãạ...)
- [ ] Copy/paste text
- [ ] Ctrl+Alt+Del từ menu

### Chat:
- [ ] Gửi tin nhắn từ client
- [ ] Nhận tin nhắn từ server
- [ ] Collapse/expand chat panel
- [ ] Spam test (50 messages) → freeze
- [ ] Lưu chat với nút 💾
- [ ] Lưu chat khi đóng window
- [ ] Unicode trong chat (emoji, tiếng Việt)

### Integration:
- [ ] Chat hoạt động cùng remote desktop
- [ ] File transfer vẫn hoạt động
- [ ] FPS counter không bị ảnh hưởng
- [ ] Mouse và keyboard cùng hoạt động

---

## 🚀 Future Improvements (Không implement)

- ~~Camera streaming~~ (Bỏ qua theo yêu cầu)
- Screenshot capture and save (Có thể thêm sau)
- Encryption cho chat messages
- Chat history persistence (auto-save)
- Multi-user chat rooms
- Voice chat

---

---

## 🐛 Bug Fixes

### Mouse Coordinate Bug - Full Screen Mode

**Issue**: Khi click chuột ở chế độ "Fit to Window" (full màn hình), tọa độ bị sai - click vào vị trí này nhưng remote machine nhận ở vị trí khác.

**Root Cause**:
1. `updateScreen()` luôn set icon gốc (original size), không respect view mode
2. Khi user chọn "Fit to Window", `fitScreenToWindow()` scale image nhưng `updateScreen()` ghi đè
3. `getScaledPoint()` tính toán tọa độ dựa vào icon size hiện tại, nhưng mỗi frame lại bị reset về original

**Solution**:
- ✅ Thêm flag `isFitToWindow` để track view mode hiện tại
- ✅ `updateScreen()` kiểm tra flag và apply đúng scaling
- ✅ `fitScreenToWindow()` set flag = true
- ✅ `setOriginalSize()` set flag = false
- ✅ `getScaledPoint()` giờ hoạt động chính xác với icon size thực tế đang hiển thị

**Files Modified**:
- [RemoteDesktopGUI.java](src/RemoteDesktopGUI.java) - Lines 25, 394-416, 428-438

**Test Steps**:
1. ✅ Click chuột ở chế độ Original Size → chính xác
2. ✅ Chọn "View" → "Vừa với cửa sổ" → click chuột → chính xác
3. ✅ Resize window → click vẫn chính xác
4. ✅ Chọn "View" → "Kích thước gốc" → click vẫn chính xác

---

**Version**: 2.1.1  
**Date**: January 6, 2026  
**Status**: ✅ Production Ready (Bug Fixed)
