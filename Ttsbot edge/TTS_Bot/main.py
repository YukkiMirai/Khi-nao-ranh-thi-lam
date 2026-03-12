import discord
from discord import app_commands
import edge_tts
from gtts import gTTS
import io
import os
import re
import json
import asyncio
from dotenv import load_dotenv

load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')

CONFIG_FILE = 'config.json'
DEFAULT_CONFIG = {
    "text_channel_id": 0,
    "engine": "edge",
    "voice_name": "vi-VN-NamMinhNeural",
    "rate": "+5%",
    "auto_leave_seconds": 300,
    "welcome_text": "Chào mừng"
}

def load_config():
    if os.path.exists(CONFIG_FILE):
        with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
            try:
                return {**DEFAULT_CONFIG, **json.load(f)}
            except:
                return DEFAULT_CONFIG
    return DEFAULT_CONFIG

def save_config(config_data):
    with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
        json.dump(config_data, f, indent=4, ensure_ascii=False)

config = load_config()

class MyBot(discord.Client):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        intents.voice_states = True
        intents.members = True
        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)
        self.last_active = asyncio.get_event_loop().time()

    async def setup_hook(self):
        await self.tree.sync()
        self.loop.create_task(self.check_voice_activity())

    async def on_ready(self):
        print(f'Bot {self.user} đã sẵn sàng. Dùng /setup để cấu hình nhanh!')

    async def check_voice_activity(self):
        while not self.is_closed():
            await asyncio.sleep(30)
            for vc in self.voice_clients:
                real_users = [m for m in vc.channel.members if not m.bot]
                elapsed = asyncio.get_event_loop().time() - self.last_active
                if len(real_users) == 0 or elapsed > config['auto_leave_seconds']:
                    await vc.disconnect()

    async def speak(self, vc, text):
        if not vc or not text.strip(): return
        if vc.is_playing(): vc.stop()
        try:
            audio_buffer = io.BytesIO()
            if config['engine'] == "google":
                tts = gTTS(text=text, lang='vi')
                tts.write_to_fp(audio_buffer)
            else:
                c_rate = "+12%" if len(text) < 150 else config['rate']
                communicate = edge_tts.Communicate(text, config['voice_name'], rate=c_rate)
                async for chunk in communicate.stream():
                    if chunk["type"] == "audio": audio_buffer.write(chunk["data"])
            audio_buffer.seek(0)
            # Kiểm tra hệ điều hành: nếu là Windows (nt) thì dùng .exe, ngược lại dùng lệnh ffmpeg
            exe_path = "ffmpeg.exe" if os.name == "nt" else "ffmpeg"
            
            vc.play(discord.FFmpegPCMAudio(
                audio_buffer, 
                pipe=True, 
                executable=exe_path
            ))
            # --------------------
        except Exception as e:
            print(f"Lỗi phát: {e}")

    async def on_voice_state_update(self, member, before, after):
        if member.bot: return
        if after.channel and before.channel != after.channel:
            vc = member.guild.voice_client
            if vc and vc.channel == after.channel and config['welcome_text']:
                await self.speak(vc, f"{config['welcome_text']} {member.display_name}")

    async def on_message(self, message):
        if message.author == self.user or message.channel.id != config['text_channel_id']:
            return
        self.last_active = asyncio.get_event_loop().time()
        content = re.sub(r'<a?(:.*:)\d+>', r'\1', message.clean_content).replace(":", " ")
        if message.stickers:
            for s in message.stickers: content += f" {s.name}"
        if not content.strip(): return
        vc = message.guild.voice_client
        user_voice = message.author.voice
        if user_voice and user_voice.channel:
            if not vc: vc = await user_voice.channel.connect()
            elif vc.channel != user_voice.channel: await vc.move_to(user_voice.channel)
            await self.speak(vc, content)

client = MyBot()

# --- SIÊU LỆNH SETUP TỔNG HỢP ---

@client.tree.command(name="setup", description="Cấu hình nhanh toàn bộ bot")
@app_commands.describe(
    channel="Kênh text bot sẽ nghe (Hiện tại: " + str(config['text_channel_id']) + ")",
    engine="Chọn bộ đọc (Hiện tại: " + config['engine'] + ")",
    voice="Chọn giọng Edge (Hiện tại: " + config['voice_name'] + ")",
    speed="Tốc độ đọc (Hiện tại: " + config['rate'] + ")",
    welcome="Câu chào mừng (Hiện tại: " + config['welcome_text'] + ")"
)
@app_commands.choices(engine=[
    app_commands.Choice(name="Microsoft Edge (Tự nhiên)", value="edge"),
    app_commands.Choice(name="Google TTS (Chị Google)", value="google")
], voice=[
    app_commands.Choice(name="Nam Minh (Nam)", value="vi-VN-NamMinhNeural"),
    app_commands.Choice(name="Hoài My (Nữ)", value="vi-VN-HoaiMyNeural")
])
async def setup(
    interaction: discord.Interaction, 
    channel: discord.TextChannel = None,
    engine: app_commands.Choice[str] = None,
    voice: app_commands.Choice[str] = None,
    speed: str = None,
    welcome: str = None
):
    global config
    changes = []

    if channel:
        config['text_channel_id'] = channel.id
        changes.append(f"Kênh: {channel.mention}")
    
    if engine:
        config['engine'] = engine.value
        changes.append(f"Engine: {engine.name}")

    if voice:
        config['voice_name'] = voice.value
        changes.append(f"Giọng: {voice.name}")

    if speed:
        if re.match(r'^[+-]\d+%$', speed):
            config['rate'] = speed
            changes.append(f"Tốc độ: {speed}")
        else:
            return await interaction.response.send_message("Tốc độ sai định dạng (+5%, -10%...)", ephemeral=True)

    if welcome is not None:
        config['welcome_text'] = welcome
        changes.append(f"Chào mừng: {welcome if welcome else 'Đã tắt'}")

    if not changes:
        return await interaction.response.send_message("Bạn chưa thay đổi gì!", ephemeral=True)

    save_config(config)
    
    summary = "\n".join([f"✅ {item}" for item in changes])
    await interaction.response.send_message(f"**Đã cập nhật cấu hình:**\n{summary}", ephemeral=True)

client.run(TOKEN)