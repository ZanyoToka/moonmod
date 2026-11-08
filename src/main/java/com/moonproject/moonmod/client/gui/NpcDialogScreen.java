package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * 极简沉浸式电影级底部对话字幕条 (Minimal Sleek Translucent Subtitle Bar)
 * 彻底消除遮挡视野的大黑框，不阻挡游戏世界与 NPC 身体动作，
 * 极致轻量化半透明毛玻璃质感，与头顶 3D 浮空漫画气泡双模完美呼应。
 */
public class NpcDialogScreen extends Screen {

    public static NpcDialogScreen activeInstance = null;

    private final int entityId;
    private final String npcName;
    private int mood;
    private int affection;
    private String emotion;
    private String speech;
    private boolean waiting = false;

    // 缓存台词行排版
    private List<FormattedCharSequence> cachedLines = new ArrayList<>();
    private EditBox inputBox;

    public NpcDialogScreen(int entityId, String npcName, int mood, int affection, String emotion, String initialSpeech) {
        super(Component.literal("AI 对话 - " + npcName));
        this.entityId = entityId;
        this.npcName = npcName;
        this.mood = mood;
        this.affection = affection;
        this.emotion = emotion != null ? emotion : "happy";
        this.speech = initialSpeech != null && !initialSpeech.isEmpty() ? initialSpeech : "咕噜噜~ 找本鱼有什么事吗？";
    }

    @Override
    protected void init() {
        super.init();
        activeInstance = this;

        int barWidth = Math.min(this.width - 32, 540);
        int barHeight = 48;
        int startX = (this.width - barWidth) / 2;
        int startY = this.height - barHeight - 8;

        // 极简半透明输入框 (嵌在字幕条最底行)
        this.inputBox = new EditBox(this.font, startX + 10, startY + 28, barWidth - 110, 16, Component.literal(""));
        this.inputBox.setMaxLength(128);
        this.inputBox.setHint(Component.literal("§8💬 输入想对她说的话... (按回车 Enter 发送)"));
        this.inputBox.setBordered(false); // 无边框沉浸嵌入
        this.inputBox.setTextColor(0xFFFFFF);
        this.addRenderableWidget(this.inputBox);

        this.setInitialFocus(this.inputBox);
        rebuildTextCache(barWidth - 20);
    }

    private void rebuildTextCache(int maxWidth) {
        if (this.font != null && this.speech != null) {
            this.cachedLines = this.font.split(Component.literal("§f" + this.speech), maxWidth);
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (activeInstance == this) {
            activeInstance = null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 回车键 (257 = Enter, 335 = Keypad Enter) 发送消息
        if (keyCode == 257 || keyCode == 335) {
            doSendMessage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int barWidth = Math.min(this.width - 32, 540);
        int barHeight = 48;
        int startX = (this.width - barWidth) / 2;
        int startY = this.height - barHeight - 8;

        // 点击右上角 [离开 Esc] 按钮区域
        if (mouseX >= startX + barWidth - 45 && mouseX <= startX + barWidth - 5
                && mouseY >= startY + 26 && mouseY <= startY + 44) {
            this.onClose();
            return true;
        }

        // 点击 [发送 Enter] 按钮区域
        if (mouseX >= startX + barWidth - 95 && mouseX <= startX + barWidth - 50
                && mouseY >= startY + 26 && mouseY <= startY + 44) {
            doSendMessage();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void doSendMessage() {
        if (this.inputBox == null || this.waiting) {
            return;
        }
        String text = this.inputBox.getValue().trim();
        if (text.isEmpty()) {
            return;
        }

        this.waiting = true;
        this.inputBox.setValue("");
        this.speech = "...";
        int barWidth = Math.min(this.width - 32, 540);
        rebuildTextCache(barWidth - 20);

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.8F, 1.3F);
        }

        ModNetwork.CHANNEL.sendToServer(new ModNetwork.SendNpcChatMessage(this.entityId, text));
    }

    public void onReplyReceived(String newSpeech, int newMood, int newAffection, String newEmotion) {
        this.speech = newSpeech;
        this.mood = newMood;
        this.affection = newAffection;
        this.emotion = newEmotion;
        this.waiting = false;
        int barWidth = Math.min(this.width - 32, 540);
        rebuildTextCache(barWidth - 20);

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.4F);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // 沉浸式核心：绝不进行全屏暗化黑屏或模糊！保持 3D 游戏世界与 NPC 全身动作 100% 鲜活可见
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int barWidth = Math.min(this.width - 32, 540);
        int barHeight = 48;
        int startX = (this.width - barWidth) / 2;
        int startY = this.height - barHeight - 8;

        // 1. 绘制极窄半透明黑钛微光条底板 (高度仅 48 像素，透明度 80%)
        graphics.fill(startX, startY, startX + barWidth, startY + barHeight, 0xCC090F1E);
        // 上边沿精致微光发光线 (深天蓝)
        graphics.fill(startX, startY, startX + barWidth, startY + 1, 0x8838BDF8);
        graphics.renderOutline(startX, startY, barWidth, barHeight, 0x4438BDF8);

        // 2. 顶栏微缩状态行
        graphics.drawString(this.font, "§b🐟 " + this.npcName, startX + 10, startY + 4, 0xFFFFFF);

        String moodStr = "§d💖 心情 " + this.mood;
        String affStr = "§e⭐ 好感 Lv." + (this.affection >= 75 ? "4" : (this.affection >= 50 ? "3" : (this.affection >= 25 ? "2" : "1")));
        String emoStr = "§a[" + getEmotionBadge(this.emotion) + "]";
        graphics.drawString(this.font, moodStr + "  " + affStr + "  " + emoStr, startX + 115, startY + 4, 0xCCCCCC);

        // 3. NPC 核心台词行 (精简单行，沉浸电影字幕呈现)
        if (this.waiting) {
            int dotCount = (int) ((System.currentTimeMillis() / 350) % 4);
            graphics.drawString(this.font, "§7(" + this.npcName + " 正在沉思中" + ".".repeat(dotCount) + " 💭)", startX + 10, startY + 16, 0xAAAAAA);
        } else if (!this.cachedLines.isEmpty()) {
            graphics.drawString(this.font, this.cachedLines.get(0), startX + 10, startY + 16, 0xFFFFFF);
        }

        // 4. 输入框底部分割线与按钮微徽章
        graphics.fill(startX + 8, startY + 26, startX + barWidth - 8, startY + 27, 0x22FFFFFF);

        // 极简操作徽章
        boolean sendHover = mouseX >= startX + barWidth - 95 && mouseX <= startX + barWidth - 50 && mouseY >= startY + 26 && mouseY <= startY + 44;
        boolean closeHover = mouseX >= startX + barWidth - 45 && mouseX <= startX + barWidth - 5 && mouseY >= startY + 26 && mouseY <= startY + 44;

        graphics.drawString(this.font, "§b[Enter 发送]", startX + barWidth - 95, startY + 31, sendHover ? 0xFFFFFF : 0xAAAAAA);
        graphics.drawString(this.font, "§7[Esc 离开]", startX + barWidth - 45, startY + 31, closeHover ? 0xFFFFFF : 0x888888);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String getEmotionBadge(String emo) {
        if (emo == null) return "平静";
        return switch (emo.toLowerCase()) {
            case "happy" -> "开心 😊";
            case "excited" -> "兴奋 ⚡";
            case "touched" -> "感动 💖";
            case "sad" -> "低落 💧";
            case "angry" -> "生气 💢";
            case "clap" -> "赞赏 👏";
            case "surprise" -> "惊讶 ❗";
            case "proud" -> "傲娇 ✨";
            case "dance" -> "摇摆 🎶";
            default -> "待机 💬";
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
