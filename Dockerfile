# Dockerfile cho TeamViewer 2.0 Server
FROM eclipse-temurin:17-jdk-jammy

# Cài đặt các gói cần thiết cho GUI và X11
RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    x11vnc \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Tạo thư mục làm việc
WORKDIR /app

# Copy tất cả file Java vào container
COPY *.java /app/

# Biên dịch ứng dụng
RUN javac *.java

# Expose cổng 5900
EXPOSE 5900

# Khởi động server
CMD ["java", "Server"]
