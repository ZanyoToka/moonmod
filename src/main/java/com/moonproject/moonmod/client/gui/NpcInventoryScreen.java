package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.ai.npc.inventory.NpcInventoryMenu;
import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import com.moonproject.moonmod.network.ModNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * AI NPC 专属女仆式管家管理界面 (Maid-style GUI)
 * 包含：
 * 1. 3D 实体模型实时交互预览 (跟随鼠标视线)
 * 2. 4 格专属护甲槽 + 2 格手持物槽
 * 3. 18 格 (2x9) NPC 随身独立行囊
 * 4. 4 项模块化任务一键切换：待命、伴随、护卫、采掘
 * 5. 玩家背包与快捷栏交互
 */
public class NpcInventoryScreen extends AbstractContainerScreen<NpcInventoryMenu> {

    private final AiPlayerNpcEntity npc;
    private Button btnIdle;
    private Button btnFollow;
    private Button btnGuard;
    private Button btnHarvest;

    public NpcInventoryScreen(NpcInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.npc = menu.getNpc();
        this.imageWidth = 276;
        this.imageHeight = 188;
        this.inventoryLabelY = 94;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        int btnY = this.topPos + 102;
        int btnW = 42;
        int btnH = 18;

        // 任务切换按钮组 (Maid-style Task Switcher)
        this.btnIdle = this.addRenderableWidget(Button.builder(Component.literal("🧘待命"), b -> switchTask("idle"))
                .bounds(this.leftPos + 6, btnY, btnW, btnH).build());

        this.btnFollow = this.addRenderableWidget(Button.builder(Component.literal("🚶伴随"), b -> switchTask("follow"))
                .bounds(this.leftPos + 50, btnY, btnW, btnH).build());

        this.btnGuard = this.addRenderableWidget(Button.builder(Component.literal("🛡️护卫"), b -> switchTask("guard"))
                .bounds(this.leftPos + 6, btnY + 22, btnW, btnH).build());

        this.btnHarvest = this.addRenderableWidget(Button.builder(Component.literal("🪓采掘"), b -> switchTask("harvest"))
                .bounds(this.leftPos + 50, btnY + 22, btnW, btnH).build());
    }

    private void switchTask(String taskId) {
        if (npc != null) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.SwitchNpcTaskMessage(npc.getId(), taskId));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = this.leftPos;
        int y = this.topPos;

        // 1. 底板深色高科技圆角矩形
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFA141A28);
        graphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + 22, 0xFF1E293B);
        // 外框发光边线
        graphics.renderOutline(x, y, this.imageWidth, this.imageHeight, 0xFF38BDF8);

        // 2. 3D 实体模型展示框 (左侧 45x80 区域)
        graphics.fill(x + 6, y + 26, x + 96, y + 98, 0xFF0B0F19);
        graphics.renderOutline(x + 6, y + 26, 90, 72, 0xFF475569);

        if (this.npc != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    x + 50,
                    y + 92,
                    28,
                    (float) (x + 50) - mouseX,
                    (float) (y + 92 - 38) - mouseY,
                    this.npc
            );
        }

        // 3. 绘制装备槽位背景方框
        // 4 个护甲槽
        for (int i = 0; i < 4; i++) {
            renderSlotBox(graphics, x + 8, y + 18 + i * 18, 0xFF334155);
        }
        // 2 个手持槽 (主手、副手)
        renderSlotBox(graphics, x + 80, y + 54, 0xFF334155);
        renderSlotBox(graphics, x + 80, y + 72, 0xFF334155);

        // 4. 绘制 NPC 随身行囊 18 格槽位背景 (2x9)
        graphics.fill(x + 103, y + 16, x + 103 + 9 * 18 + 4, y + 16 + 2 * 18 + 4, 0xFF192233);
        graphics.renderOutline(x + 103, y + 16, 9 * 18 + 4, 2 * 18 + 4, 0xFF0EA5E9);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                renderSlotBox(graphics, x + 105 + col * 18, y + 18 + row * 18, 0xFF334155);
            }
        }

        // 5. 绘制玩家背包槽位背景 (3x9) 与 快捷栏 (1x9)
        graphics.fill(x + 103, y + 102, x + 103 + 9 * 18 + 4, y + 102 + 3 * 18 + 4, 0xFF192233);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                renderSlotBox(graphics, x + 105 + col * 18, y + 104 + row * 18, 0xFF2D3748);
            }
        }
        graphics.fill(x + 103, y + 160, x + 103 + 9 * 18 + 4, y + 160 + 18 + 4, 0xFF192233);
        for (int col = 0; col < 9; col++) {
            renderSlotBox(graphics, x + 105 + col * 18, y + 162, 0xFF2D3748);
        }
    }

    private void renderSlotBox(GuiGraphics graphics, int sx, int sy, int color) {
        graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF0F172A);
        graphics.renderOutline(sx - 1, sy - 1, 18, 18, color);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题栏：名字 + 心情 + 好感度
        if (npc != null) {
            String titleStr = "§b" + npc.getScoreboardName() + " §7| §d💖" + npc.getMood() + " §e⭐Lv." + (npc.getAffection() / 20);
            graphics.drawString(this.font, titleStr, 8, 7, 0xFFFFFFFF, false);

            // 当前执行的任务模式高亮徽章
            String currentMode = npc.getTaskMode();
            String taskBadge = switch (currentMode.toLowerCase()) {
                case "guard" -> "§a[🛡️ 护卫中]";
                case "harvest" -> "§e[🪓 采掘中]";
                case "follow" -> "§b[🚶 伴随中]";
                default -> "§7[🧘 待命中]";
            };
            graphics.drawString(this.font, taskBadge, 6, 148, 0xFFFFFFFF, false);
        }

        // NPC 行囊标签
        graphics.drawString(this.font, "§3随身行囊 (18格)", 105, 7, 0x80FFFFFF, false);

        // 玩家背包标签
        graphics.drawString(this.font, "§8玩家物品栏", 105, 94, 0x80FFFFFF, false);
    }
}
