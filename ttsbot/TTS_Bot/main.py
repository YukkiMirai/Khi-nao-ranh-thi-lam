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
SLANG_FILE = 'slang.json'

DEFAULT_CONFIG = {
    "text_channel_id": 0,
    "engine": "edge",
    "voice_name": "vi-VN-NamMinhNeural",
    "rate": "+5%",
    "auto_leave_seconds": 300,
    "welcome_text": "Chào mừng",
    "welcome_enabled": True 
}

def load_json(file_path, default_data):
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            try: return json.load(f)
            except: return default_data
    save_json(file_path, default_data)
    return default_data

def save_json(file_path, data):
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

config = load_json(CONFIG_FILE, DEFAULT_CONFIG)
# Merge any new default keys that may be missing from a previously saved config
_updated = False
for _k, _v in DEFAULT_CONFIG.items():
    if _k not in config:
        config[_k] = _v
        _updated = True
if _updated:
    save_json(CONFIG_FILE, config)
slangs = load_json(SLANG_FILE, {})

class MyBot(discord.Client):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        intents.voice_states = True
        intents.members = True
        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)
        self.last_active = 0.0

    async def setup_hook(self):
        await self.tree.sync()
        self.loop.create_task(self.check_voice_activity())

    async def on_ready(self):
        print(f'Bot {self.user} đã sẵn sàng với bộ lệnh slang đầy đủ!')

    async def check_voice_activity(self):
        while not self.is_closed():
            await asyncio.sleep(30)
            for vc in self.voice_clients:
                real_users = [m for m in vc.channel.members if not m.bot]
                elapsed = asyncio.get_running_loop().time() - self.last_active
                if len(real_users) == 0 or elapsed > config['auto_leave_seconds']:
                    await vc.disconnect()

    def normalize_text(self, text):
        # Tách từ và thay thế dựa trên slang.json
        words = text.split()
        for i in range(len(words)):
            clean_word = re.sub(r'[^\w\s]', '', words[i].lower())
            if clean_word in slangs:
                words[i] = slangs[clean_word]
        return " ".join(words)

    async def speak(self, vc, text):
        if not vc or not text.strip(): return
        if vc.is_playing(): vc.stop()
        processed_text = self.normalize_text(text)
        try:
            audio_buffer = io.BytesIO()
            if config['engine'] == "google":
                tts = gTTS(text=processed_text, lang='vi')
                tts.write_to_fp(audio_buffer)
            else:
                c_rate = "+12%" if len(processed_text) < 150 else config['rate']
                communicate = edge_tts.Communicate(processed_text, config['voice_name'], rate=c_rate)
                async for chunk in communicate.stream():
                    if chunk["type"] == "audio": audio_buffer.write(chunk["data"])
            audio_buffer.seek(0)
            exe_path = "ffmpeg.exe" if os.name == "nt" else "ffmpeg"
            vc.play(discord.FFmpegPCMAudio(audio_buffer, pipe=True, executable=exe_path))
        except Exception as e:
            print(f"Lỗi phát: {e}")

    async def on_voice_state_update(self, member, before, after):
        if member.bot: return
        if config.get("welcome_enabled", True) and after.channel and before.channel != after.channel:
            vc = member.guild.voice_client
            if vc and vc.channel == after.channel and config['welcome_text']:
                await self.speak(vc, f"{config['welcome_text']} {member.display_name}")

    async def on_message(self, message):
        if message.author == self.user or message.channel.id != config['text_channel_id']:
            return
        self.last_active = asyncio.get_running_loop().time()
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

# --- SLASH COMMANDS ---

@client.tree.command(name="setup", description="Cấu hình bot (Kênh, Giọng, Chào mừng)")
@app_commands.choices(engine=[
    app_commands.Choice(name="Edge TTS", value="edge"),
    app_commands.Choice(name="Google TTS", value="google")
], welcome_status=[
    app_commands.Choice(name="Bật", value="on"),
    app_commands.Choice(name="Tắt", value="off")
])
async def setup(
    interaction: discord.Interaction, 
    channel: discord.TextChannel = None,
    engine: app_commands.Choice[str] = None,
    speed: str = None,
    welcome_text: str = None,
    welcome_status: app_commands.Choice[str] = None
):
    global config
    if channel: config['text_channel_id'] = channel.id
    if engine: config['engine'] = engine.value
    if speed and re.match(r'^[+-]\d+%$', speed): config['rate'] = speed
    if welcome_text: config['welcome_text'] = welcome_text
    if welcome_status: config['welcome_enabled'] = (welcome_status.value == "on")
    
    save_json(CONFIG_FILE, config)
    status_str = "Bật" if config['welcome_enabled'] else "Tắt"
    await interaction.response.send_message(f"✅ Đã cập nhật! (Chào mừng hiện tại: {status_str})", ephemeral=True)

@client.tree.command(name="slang_add", description="Thêm hoặc cập nhật từ lóng")
async def slang_add(interaction: discord.Interaction, tu_viet_tat: str, doc_thanh: str):
    global slangs
    slangs[tu_viet_tat.lower().strip()] = doc_thanh.strip()
    save_json(SLANG_FILE, slangs)
    await interaction.response.send_message(f"✅ Đã thêm: `{tu_viet_tat}` -> `{doc_thanh}`", ephemeral=True)

@client.tree.command(name="slang_remove", description="Xóa một từ lóng khỏi danh sách")
async def slang_remove(interaction: discord.Interaction, tu_can_xoa: str):
    global slangs
    key = tu_can_xoa.lower().strip()
    if key in slangs:
        del slangs[key]
        save_json(SLANG_FILE, slangs)
        await interaction.response.send_message(f"❌ Đã xóa từ: `{key}`", ephemeral=True)
    else:
        await interaction.response.send_message(f"⚠️ Không tìm thấy từ `{key}` trong danh sách.", ephemeral=True)

@client.tree.command(name="slang_list", description="Xem toàn bộ danh sách từ lóng hiện có")
async def slang_list(interaction: discord.Interaction):
    if not slangs:
        return await interaction.response.send_message("Danh sách từ lóng đang trống.", ephemeral=True)
    
    msg = "**📚 Danh sách từ lóng hiện có:**\n"
    for k, v in slangs.items():
        msg += f"- `{k}`: {v}\n"
        if len(msg) > 1900: # Tránh quá giới hạn ký tự Discord
            msg += "...và còn tiếp..."
            break
    await interaction.response.send_message(msg, ephemeral=True)

@client.tree.command(name="slang_reload", description="Nạp lại file slang.json từ ổ cứng")
async def slang_reload(interaction: discord.Interaction):
    global slangs
    slangs = load_json(SLANG_FILE, {})
    await interaction.response.send_message(f"✅ Đã nạp lại {len(slangs)} từ lóng!", ephemeral=True)

client.run(TOKEN)