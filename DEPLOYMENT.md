# Hướng dẫn Deploy TeamViewer 2.0 lên Internet

## 🌐 3 Cách Deploy lên Internet

### **Cách 1: Docker + Ngrok (Dễ nhất - Khuyên dùng)**

#### Bước 1: Cài đặt yêu cầu
1. **Docker Desktop**: https://www.docker.com/products/docker-desktop
2. **Ngrok**: https://ngrok.com/download

#### Bước 2: Chạy Server trong Docker
```bash
# Build và chạy container
docker-run.bat
```

#### Bước 3: Tạo Ngrok tunnel
```bash
# Tạo tunnel public cho port 5900
ngrok tcp 5900
```

Ngrok sẽ hiển thị địa chỉ như: `tcp://0.tcp.ngrok.io:12345`

#### Bước 4: Client kết nối
- Nhập địa chỉ: `0.tcp.ngrok.io` (bỏ tcp://)
- Sửa port trong code: `12345` (port Ngrok cung cấp)

**✅ Ưu điểm:**
- Miễn phí (có giới hạn)
- Không cần cấu hình router
- Có địa chỉ public ngay lập tức

**❌ Nhược điểm:**
- Địa chỉ đổi mỗi lần khởi động (phiên bản free)
- Giới hạn kết nối đồng thời

---

### **Cách 2: Docker + Port Forwarding**

#### Bước 1: Chạy server trong Docker
```bash
docker-run.bat
```

#### Bước 2: Cấu hình Router (Port Forwarding)
1. Truy cập router: http://192.168.1.1 (hoặc địa chỉ gateway của bạn)
2. Tìm mục "Port Forwarding" hoặc "Virtual Server"
3. Thêm rule:
   - **External Port**: 5900
   - **Internal Port**: 5900
   - **Internal IP**: [IP máy chủ của bạn]
   - **Protocol**: TCP

#### Bước 3: Lấy IP public
```bash
curl https://api.ipify.org
```

#### Bước 4: Client kết nối
- Nhập IP public vừa lấy được

**✅ Ưu điểm:**
- Miễn phí hoàn toàn
- Địa chỉ IP cố định (nếu ISP cung cấp static IP)
- Không giới hạn kết nối

**❌ Nhược điểm:**
- Cần quyền truy cập router
- Phức tạp hơn với các router khác nhau
- IP có thể thay đổi khi reset modem

---

### **Cách 3: Cloud Server (VPS)**

#### Bước 1: Thuê VPS
- **AWS EC2** (Free tier 12 tháng)
- **Google Cloud** (Free tier $300)
- **DigitalOcean** ($5/tháng)
- **Azure** (Free tier $200)

#### Bước 2: Deploy trên VPS
```bash
# SSH vào VPS
ssh user@your-vps-ip

# Clone code
git clone [your-repo]
cd teamviewer2.0

# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Chạy server
docker build -t teamviewer-server .
docker run -d -p 5900:5900 teamviewer-server

# Mở firewall
sudo ufw allow 5900/tcp
```

#### Bước 3: Client kết nối
- Nhập IP của VPS

**✅ Ưu điểm:**
- IP public cố định
- Hiệu năng tốt
- Có thể mở rộng dễ dàng
- Professional

**❌ Nhược điểm:**
- Tốn phí (trừ free tier)
- Cần kiến thức Linux/DevOps

---

## 📝 Sửa Code để hỗ trợ Custom Port

Để kết nối với port khác (như Ngrok), cần sửa một chút:

### Sửa TeamViewerGUI.java
```java
// Thêm trường nhập port
private JTextField portField;

// Trong createControlPanel():
portField = new JTextField(5);
portField.setText("5900"); // Default port
idPanel.add(new JLabel("Port:"));
idPanel.add(portField);

// Trong connectToPartner():
String partnerId = partnerIdField.getText().trim();
int port = Integer.parseInt(portField.getText().trim());

client = new Client();
if (client.connect(partnerId, port)) {
    // ...
}
```

---

## 🚀 Quick Start với Ngrok

### Nhanh nhất (3 bước):
```bash
# 1. Chạy server local
java Server

# 2. Mở terminal mới, chạy ngrok
ngrok tcp 5900

# 3. Client dùng địa chỉ ngrok để kết nối
# Ví dụ: 0.tcp.ngrok.io:12345
```

---

## 🔒 Bảo mật khi Deploy lên Internet

⚠️ **QUAN TRỌNG**: Code hiện tại không có bảo mật!

### Cần thêm:
1. **Authentication** - Password hoặc token
2. **Encryption** - SSL/TLS
3. **Rate limiting** - Chống DDoS
4. **Logging** - Theo dõi truy cập
5. **Whitelist IP** - Giới hạn IP được phép

### File bảo mật cơ bản (tùy chọn):
Tạo file `config.properties`:
```properties
# Authentication
password=your-secure-password

# Allowed IPs (comma separated)
allowed.ips=192.168.1.100,10.0.0.5

# Enable SSL
ssl.enabled=true
ssl.keystore=/path/to/keystore.jks
ssl.password=keystore-password
```

---

## 📊 So sánh các phương án

| Phương án | Độ khó | Chi phí | Tốc độ | Ổn định |
|-----------|--------|---------|--------|---------|
| Ngrok | ⭐ | Free | ⭐⭐⭐ | ⭐⭐⭐ |
| Port Forward | ⭐⭐ | Free | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| VPS | ⭐⭐⭐ | $5/tháng | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🆘 Troubleshooting

### Lỗi "Connection refused"
- Kiểm tra server đang chạy: `docker ps`
- Kiểm tra port có mở: `netstat -an | findstr 5900`
- Kiểm tra firewall: `netsh advfirewall firewall show rule name=all`

### Ngrok không hoạt động
- Đăng ký tài khoản: https://ngrok.com/
- Xác thực: `ngrok authtoken YOUR_TOKEN`
- Kiểm tra kết nối: http://localhost:4040

### Docker không chạy được
- Bật Hyper-V (Windows)
- Cấp quyền cho Docker Desktop
- Kiểm tra logs: `docker logs teamviewer-server`

---

## 📞 Hỗ trợ

Nếu cần giúp deploy, hãy cho biết bạn chọn phương án nào!
