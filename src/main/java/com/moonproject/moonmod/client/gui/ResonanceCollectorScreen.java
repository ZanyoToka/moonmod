package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.ResonanceCollectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 场谐振能源采集单元的界面。
 *
 * <p>底图 {@code textures/gui/resonance_gui_full.png}（1536x1024）按 256x171 缩放绘制；
 * 左侧四个工作按钮与进度条在后续版本追加。</p>
 */
public class ResonanceCollectorScreen extends AbstractContainerScreen<ResonanceCollectorMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/gui/resonance_gui_tall.png");
    /** 底图原始尺寸。 */
    private static final int TEX_W = 256;
    private static final int TEX_H = 256;

    public ResonanceCollectorScreen(ResonanceCollectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 226;
        this.inventoryLabelY = 144;
    }

    /** 左侧四个工作按钮（压迫 / 沟通 / 洞察 / 本能）。 */
    @Override
    protected void init() {
        super.init();
        String[] names = {"压迫", "沟通", "洞察", "本能"};
        for (int i = 0; i < 4; i++) {
            final int id = i;
            int bx = leftPos - 34;
            int by = topPos + 12 + i * 22;
            this.addRenderableWidget(new net.minecraft.client.gui.components.Button.Builder(
                    Component.literal(names[i]),
                    b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id))
                    .pos(bx, by).size(30, 18).build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 从 256x256 画布中取 y=42 起、高 171 的有效区域（9 参数版本，显式纹理尺寸）
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}