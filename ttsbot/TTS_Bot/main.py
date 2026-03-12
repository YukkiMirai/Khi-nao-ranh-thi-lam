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
        self.tts_queues: dict[int, asyncio.Queue] = {}  # guild_id -> Queue

    def get_queue(self, guild_id: int) -> asyncio.Queue:
        if guild_id not in self.tts_queues:
            self.tts_queues[guild_id] = asyncio.Queue()
            asyncio.get_event_loop().create_task(self.queue_worker(guild_id))
        return self.tts_queues[guild_id]

    async def queue_worker(self, guild_id: int):
        queue = self.tts_queues[guild_id]
        while not self.is_closed():
            vc, text = await queue.get()
            try:
                await self._speak(vc, text)
            except Exception as e:
                print(f"Lỗi queue worker: {e}")
            finally:
                queue.task_done()

    async def enqueue(self, vc, text: str):
        if not vc or not text.strip(): return
        await self.get_queue(vc.guild.id).put((vc, text))

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

    async def _speak(self, vc, text):
        """Generate audio and play it, waiting until playback finishes."""
        if not vc or not text.strip(): return
        processed_text = self.normalize_text(text)
        try:
            audio_buffer = io.BytesIO()
            ffmpeg_options = None
            if config['engine'] == "google":
                tts = gTTS(text=processed_text, lang='vi')
                tts.write_to_fp(audio_buffer)
                try:
                    rate_val = max(0.5, min(2.0, 1 + int(config['rate'].rstrip('%')) / 100))
                    ffmpeg_options = f"-filter:a atempo={rate_val:.2f}"
                except Exception:
                    pass
            else:
                communicate = edge_tts.Communicate(processed_text, config['voice_name'], rate=config['rate'])
                async for chunk in communicate.stream():
                    if chunk["type"] == "audio": audio_buffer.write(chunk["data"])
            audio_buffer.seek(0)
            exe_path = "ffmpeg.exe" if os.name == "nt" else "ffmpeg"
            done = asyncio.get_running_loop().create_future()
            def after_play(err):
                done.get_loop().call_soon_threadsafe(done.set_result, err)
            if vc.is_playing(): vc.stop()
            vc.play(discord.FFmpegPCMAudio(audio_buffer, pipe=True, executable=exe_path, options=ffmpeg_options), after=after_play)
            await done
        except Exception as e:
            print(f"Lỗi phát: {e}")

    async def on_voice_state_update(self, member, before, after):
        if member.bot: return
        if config.get("welcome_enabled", True) and after.channel and before.channel != after.channel:
            vc = member.guild.voice_client
            if vc and vc.channel == after.channel and config['welcome_text']:
                template = config['welcome_text']
                if '${name}' in template:
                    msg = template.replace('${name}', member.display_name)
                else:
                    msg = f"{template} {member.display_name}"
                await self.enqueue(vc, msg)

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
        if vc:
            # Bot đang trong kênh voice, nói luôn không cần người gửi phải ở trong kênh
            if user_voice and user_voice.channel and vc.channel != user_voice.channel:
                await vc.move_to(user_voice.channel)
            await self.enqueue(vc, content)
        elif user_voice and user_voice.channel:
            # Bot chưa vào kênh, chỉ vào nếu người gửi đang ở trong kênh voice
            vc = await user_voice.channel.connect()
            await self.enqueue(vc, content)

client = MyBot()

# --- SLASH COMMANDS ---

@client.tree.command(name="setup", description="Cấu hình bot (Kênh, Giọng, Chào mừng). Dùng ${name} trong welcome_text để chèn tên.")
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
    if speed:
        try:
            multiplier = float(speed)
            config['rate'] = f"{(multiplier - 1) * 100:+.0f}%"
        except ValueError:
            pass
    if welcome_text: config['welcome_text'] = welcome_text
    if welcome_status: config['welcome_enabled'] = (welcome_status.value == "on")
    
    save_json(CONFIG_FILE, config)
    channel_mention = f"<#{config['text_channel_id']}>" if config['text_channel_id'] else "Chưa đặt"
    welcome_str = "Bật" if config['welcome_enabled'] else "Tắt"
    try:
        rate_float = 1 + int(config['rate'].rstrip('%')) / 100
        rate_display = f"{rate_float:.2f}x ({config['rate']})"
    except Exception:
        rate_display = config['rate']
    msg = (
        f"✅ **Cấu hình hiện tại:**\n"
        f"📢 Kênh text: {channel_mention}\n"
        f"🔊 Engine TTS: `{config['engine']}`\n"
        f"🎙️ Giọng: `{config['voice_name']}`\n"
        f"⚡ Tốc độ: `{rate_display}`\n"
        f"⏱️ Tự rời sau: `{config['auto_leave_seconds']}s`\n"
        f"👋 Chào mừng: `{welcome_str}` — \"{config['welcome_text']}\""
    )
    await interaction.response.send_message(msg, ephemeral=True)

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