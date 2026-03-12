# Sử dụng bản python slim để nhẹ máy T480s
FROM python:3.12-slim

# Cài đặt ffmpeg (trên Linux không dùng file .exe)
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

# Tạo thư mục làm việc
WORKDIR /app

# Copy file requirements và cài đặt
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ code vào container
COPY . .

# Lệnh chạy bot
CMD ["python", "main.py"]