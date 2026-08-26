import discord
from discord import app_commands
import edge_tts
from gtts import gTTS
import io
import os
import re
import json
import asyncio
from pathlib import Path
from dotenv import load_dotenv

load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')

BASE_DIR = Path(__file__).resolve().parent
CONFIG_FILE = BASE_DIR / 'config.json'
SLANG_FILE = BASE_DIR / 'slang.json'
IDLE_DISCONNECT_SECONDS = 3600
VOICE_IDLE_CHECK_INTERVAL = 60

DEFAULT_CONFIG = {
    "guild_id": 0,
    "text_channel_id": 0,
    "engine": "edge",
    "voice_name": "vi-VN-NamMinhNeural",
    "rate": "+5%",
    "auto_leave_seconds": IDLE_DISCONNECT_SECONDS,
    "welcome_text": "Chào mừng",
    "welcome_enabled": True 
}

def load_json(file_path, default_data):
    file_path = Path(file_path)
    if file_path.exists():
        with file_path.open('r', encoding='utf-8') as f:
            try: return json.load(f)
            except: return default_data
    save_json(file_path, default_data)
    return default_data

def save_json(file_path, data):
    file_path = Path(file_path)
    with file_path.open('w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

config = load_json(CONFIG_FILE, DEFAULT_CONFIG)
# Merge any new default keys that may be missing from a previously saved config
_updated = False
for _k, _v in DEFAULT_CONFIG.items():
    if _k not in config:
        config[_k] = _v
        _updated = True
if config.get('auto_leave_seconds') != IDLE_DISCONNECT_SECONDS:
    config['auto_leave_seconds'] = IDLE_DISCONNECT_SECONDS
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
        self.allowed_guild_id = int(config.get('guild_id') or 0)
        self.tts_queues: dict[int, asyncio.Queue[str]] = {}
        self.queue_workers: dict[int, asyncio.Task] = {}
        self.voice_locks: dict[int, asyncio.Lock] = {}
        self.idle_task: asyncio.Task | None = None

    def is_allowed_guild(self, guild_id: int | None) -> bool:
        if not guild_id:
            return False
        return not self.allowed_guild_id or guild_id == self.allowed_guild_id

    def bind_guild(self, guild_id: int):
        if not guild_id:
            return
        if self.allowed_guild_id and self.allowed_guild_id != guild_id:
            return
        if self.allowed_guild_id != guild_id:
            self.allowed_guild_id = guild_id
            config['guild_id'] = guild_id
            save_json(CONFIG_FILE, config)

    def touch_activity(self):
        self.last_active = asyncio.get_running_loop().time()

    def get_queue(self, guild_id: int) -> asyncio.Queue:
        queue = self.tts_queues.get(guild_id)
        if queue is None:
            queue = asyncio.Queue()
            self.tts_queues[guild_id] = queue
        worker = self.queue_workers.get(guild_id)
        if worker is None or worker.done():
            self.queue_workers[guild_id] = asyncio.create_task(self.queue_worker(guild_id))
        return queue

    def get_voice_lock(self, guild_id: int) -> asyncio.Lock:
        lock = self.voice_locks.get(guild_id)
        if lock is None:
            lock = asyncio.Lock()
            self.voice_locks[guild_id] = lock
        return lock

    async def queue_worker(self, guild_id: int):
        queue = self.tts_queues[guild_id]
        while not self.is_closed():
            text = await queue.get()
            try:
                await self._speak(guild_id, text)
            except Exception as e:
                print(f"Lỗi queue worker: {e}")
            finally:
                queue.task_done()

    async def enqueue(self, guild_id: int, text: str):
        if not text.strip():
            return
        self.touch_activity()
        await self.get_queue(guild_id).put(text)

    async def setup_hook(self):
        if self.allowed_guild_id:
            guild_obj = discord.Object(id=self.allowed_guild_id)
            self.tree.copy_global_to(guild=guild_obj)
            await self.tree.sync(guild=guild_obj)
        else:
            await self.tree.sync()
        self.touch_activity()
        self.idle_task = asyncio.create_task(self.check_voice_activity())

    async def on_ready(self):
        print(f'Bot {self.user} đã sẵn sàng với bộ lệnh slang đầy đủ!')
        if not self.allowed_guild_id and len(self.guilds) == 1:
            self.bind_guild(self.guilds[0].id)
            guild_obj = discord.Object(id=self.allowed_guild_id)
            self.tree.copy_global_to(guild=guild_obj)
            try:
                await self.tree.sync(guild=guild_obj)
            except Exception as e:
                print(f"Lỗi sync slash command theo guild: {e}")

    async def check_voice_activity(self):
        while not self.is_closed():
            await asyncio.sleep(VOICE_IDLE_CHECK_INTERVAL)
            now = asyncio.get_running_loop().time()
            if self.last_active <= 0 or (now - self.last_active) < IDLE_DISCONNECT_SECONDS:
                continue
            for guild in self.guilds:
                if not self.is_allowed_guild(guild.id):
                    continue
                vc = guild.voice_client
                if not vc or not vc.is_connected() or vc.is_playing() or vc.is_paused():
                    continue
                queue = self.tts_queues.get(guild.id)
                if queue and not queue.empty():
                    continue
                async with self.get_voice_lock(guild.id):
                    vc = guild.voice_client
                    if not vc or not vc.is_connected() or vc.is_playing() or vc.is_paused():
                        continue
                    queue = self.tts_queues.get(guild.id)
                    if queue and not queue.empty():
                        continue
                    try:
                        await vc.disconnect(force=True)
                    except Exception as e:
                        print(f"Lỗi disconnect voice: {e}")

    def normalize_text(self, text):
        # Tách từ và thay thế dựa trên slang.json
        words = text.split()
        for i in range(len(words)):
            clean_word = re.sub(r'[^\w\s]', '', words[i].lower())
            if clean_word in slangs:
                words[i] = slangs[clean_word]
        return " ".join(words)

    async def _speak(self, guild_id, text):
        """Generate audio and play it, waiting until playback finishes."""
        if not text.strip():
            return
        guild = self.get_guild(guild_id)
        if not guild:
            return
        processed_text = self.normalize_text(text)
        async with self.get_voice_lock(guild_id):
            vc = guild.voice_client
            if not vc or not vc.is_connected():
                return
            audio_buffer = io.BytesIO()
            ffmpeg_options = None
            try:
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
                        if chunk["type"] == "audio":
                            audio_buffer.write(chunk["data"])
                audio_buffer.seek(0)
                exe_path = "ffmpeg.exe" if os.name == "nt" else "ffmpeg"
                done = asyncio.get_running_loop().create_future()

                def after_play(err):
                    if done.done():
                        return
                    loop = done.get_loop()
                    if err:
                        loop.call_soon_threadsafe(done.set_exception, err)
                    else:
                        loop.call_soon_threadsafe(done.set_result, None)

                source = discord.FFmpegPCMAudio(audio_buffer, pipe=True, executable=exe_path, options=ffmpeg_options)
                try:
                    vc.play(source, after=after_play)
                except Exception:
                    source.cleanup()
                    raise
                await done
            except Exception as e:
                print(f"Lỗi phát: {e}")
            finally:
                audio_buffer.close()

    async def on_voice_state_update(self, member, before, after):
        if member.bot: return
        if not self.is_allowed_guild(member.guild.id): return
        if config.get("welcome_enabled", True) and after.channel and before.channel != after.channel:
            vc = member.guild.voice_client
            if vc and vc.channel == after.channel and config['welcome_text']:
                template = config['welcome_text']
                if '${name}' in template:
                    msg = template.replace('${name}', member.display_name)
                else:
                    msg = f"{template} {member.display_name}"
                await self.enqueue(member.guild.id, msg)

    async def on_message(self, message):
        if not message.guild or message.author == self.user:
            return
        if not self.is_allowed_guild(message.guild.id):
            return
        if message.channel.id != int(config['text_channel_id']):
            return
        self.touch_activity()
        content = re.sub(r'<a?(:.*:)\d+>', r'\1', message.clean_content).replace(":", " ")
        if message.stickers:
            for s in message.stickers: content += f" {s.name}"
        if not content.strip(): return
        user_voice = message.author.voice
        async with self.get_voice_lock(message.guild.id):
            vc = message.guild.voice_client
            if vc:
                if user_voice and user_voice.channel and vc.channel != user_voice.channel and not vc.is_playing() and not vc.is_paused():
                    try:
                        await vc.move_to(user_voice.channel)
                    except Exception as e:
                        print(f"Lỗi move voice: {e}")
                await self.enqueue(message.guild.id, content)
            elif user_voice and user_voice.channel:
                try:
                    await user_voice.channel.connect()
                except discord.ClientException:
                    pass
                await self.enqueue(message.guild.id, content)

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
    if interaction.guild_id is None:
        await interaction.response.send_message("Bot này chỉ hoạt động trong server.", ephemeral=True)
        return
    if client.allowed_guild_id and interaction.guild_id != client.allowed_guild_id:
        await interaction.response.send_message("Bot này đã được khóa cho một server khác.", ephemeral=True)
        return
    client.bind_guild(interaction.guild_id)
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
    config['auto_leave_seconds'] = IDLE_DISCONNECT_SECONDS
    
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
        f"⏱️ Tự rời sau: `{IDLE_DISCONNECT_SECONDS}s không hoạt động`\n"
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