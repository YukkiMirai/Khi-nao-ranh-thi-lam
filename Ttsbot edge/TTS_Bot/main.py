import discord
from discord import app_commands
import edge_tts
import io
import os
import re
import asyncio
from dotenv import load_dotenv

# 1. LOAD CẤU HÌNH
load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')
TEXT_CHANNEL_ID = int(os.getenv('TEXT_CHANNEL_ID'))
VOICE_CHANNEL_ID = int(os.getenv('VOICE_CHANNEL_ID'))
VOICE_NAME = os.getenv('VOICE_NAME', 'vi-VN-NamMinhNeural')

class MyBot(discord.Client):
    def __init__(self):
        # Bật các quyền cần thiết (Phải bật Message Content trong Dev Portal)
        intents = discord.Intents.default()
        intents.message_content = True 
        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)

    async def setup_hook(self):
        # Đồng bộ Slash Commands nếu cần dùng sau này
        await self.tree.sync()

    async def on_ready(self):
        print(f'--- BOT ĐÃ SẴN SÀNG ---')
        print(f'Logged in as: {self.user}')
        print(f'Monitoring Channel: {TEXT_CHANNEL_ID}')
        
        # Tự động kết nối Voice khi khởi động
        channel = self.get_channel(VOICE_CHANNEL_ID)
        if channel:
            try:
                await channel.connect()
                print(f'Đã kết nối vào Voice Channel: {channel.name}')
            except Exception as e:
                print(f'Lỗi kết nối Voice ban đầu: {e}')

    async def on_message(self, message):
        # Không xử lý tin nhắn từ chính bot hoặc sai channel
        if message.author == self.user or message.channel.id != TEXT_CHANNEL_ID:
            return

        # --- XỬ LÝ NỘI DUNG VĂN BẢN ---
        # Lấy nội dung sạch (đã chuyển @User thành tên)
        raw_content = message.clean_content

        # Regex xóa ID Emoji: <:haha:123456789> -> haha
        # Lọc cả emoji động <a:name:id> và tĩnh <:name:id>
        clean_content = re.sub(r'<a?(:.*:)\d+>', r'\1', raw_content)
        
        # Xóa dấu hai chấm để đọc mượt hơn
        clean_content = clean_content.replace(":", " ")

        # Đọc tên Sticker nếu có
        if message.stickers:
            for sticker in message.stickers:
                clean_content += f"{sticker.name}"

        # Nếu tin nhắn không có chữ (chỉ có ảnh/file) thì bỏ qua
        if not clean_content.strip():
            return

        # --- XỬ LÝ VOICE CLIENT ---
        vc = message.guild.voice_client
        if not vc:
            channel = self.get_channel(VOICE_CHANNEL_ID)
            if channel:
                vc = await channel.connect()
            else:
                return

        # Nếu đang đọc dở thì dừng để đọc tin mới ngay (Ưu tiên realtime)
        if vc.is_playing():
            vc.stop()

        # --- STREAMING TTS QUA RAM ---
        try:
            print(f"Đang đọc từ {message.author.display_name}: {clean_content}")
            
            # Khởi tạo luồng Streaming từ Edge-TTS
            communicate = edge_tts.Communicate(clean_content, VOICE_NAME, rate="+5%")
            audio_buffer = io.BytesIO()
            
            # Vừa tải vừa ghi vào RAM buffer
            async for chunk in communicate.stream():
                if chunk["type"] == "audio":
                    audio_buffer.write(chunk["data"])
            
            # Đưa con trỏ về đầu để FFmpeg đọc từ đầu
            audio_buffer.seek(0)

            # Phát qua FFmpeg bằng Pipe (Không tạo file rác trên ổ cứng)
            # executable="ffmpeg.exe" dùng cho Windows. 
            # Khi lên Linux T480s, hãy đổi thành executable="ffmpeg"
            source = discord.FFmpegPCMAudio(
                audio_buffer, 
                pipe=True, 
                executable="ffmpeg.exe" 
            )
            
            vc.play(source)

        except Exception as e:
            print(f"Lỗi khi xử lý TTS: {e}")

# Chạy Bot
if __name__ == "__main__":
    client = MyBot()
    client.run(TOKEN)