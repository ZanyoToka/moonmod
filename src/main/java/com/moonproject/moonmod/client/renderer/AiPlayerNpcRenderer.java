package com.moonproject.moonmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.client.model.AiPlayerNpcModel;
import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 假人玩家渲染器：使用 AiPlayerNpcModel 3D 骨骼动作、LittleSkin 皮肤贴图，
 * 并支持沉浸式头顶 3D 浮空动态漫画对话气泡（在真实世界空间中实时随风微动与朝向摄像机）。
 */
public class AiPlayerNpcRenderer extends HumanoidMobRenderer<AiPlayerNpcEntity, AiPlayerNpcModel> {

    private static final ResourceLocation DEEPSEEK_FISH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "textures/entity/deepseek_fish.png");

    public AiPlayerNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new AiPlayerNpcModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        // 挂载原版玩家装备与手持物渲染层（让假人能拿剑、穿盔甲、举着小蛋糕）
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(AiPlayerNpcEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // 沉浸式头顶 3D 浮空漫画对话气泡渲染
        render3DSpeechBubble(entity, poseStack, buffer, packedLight);
    }

    /**
     * 绘制位于实体头顶的 3D 浮空对话气泡卡片 (World-Space Billboard Bubble)
     */
    private void render3DSpeechBubble(AiPlayerNpcEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.getBubbleTicks() <= 0) return;
        String text = entity.getBubbleText();
        if (text == null || text.isBlank()) return;

        double distSq = this.entityRenderDispatcher.distanceToSqr(entity);
        if (distSq > 4096.0D) return; // 超过 64 格不绘制

        poseStack.pushPose();
        // 浮空在头顶上方 (原版姓名牌之上)
        poseStack.translate(0.0D, entity.getBbHeight() + 0.85D, 0.0D);
        // 面向摄像机视角 (Billboard)
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.022F, -0.022F, 0.022F);

        Matrix4f matrix4f = poseStack.last().pose();
        Font font = this.getFont();

        // 1. 顶部心情与情绪徽章行
        String header = String.format("§d💖 %d  §e⭐ Lv.%s  §a[%s]",
                entity.getMood(),
                (entity.getAffection() >= 50 ? "3" : (entity.getAffection() >= 25 ? "2" : "1")),
                getEmotionBadge(entity.getEmotion()));
        float headerX = -font.width(header) / 2.0F;

        // 2. 文本分行排版
        List<FormattedCharSequence> lines = font.split(Component.literal("§f" + text), 150);

        int totalHeight = (lines.size() + 1) * 11 + 4;
        int startY = -totalHeight;

        // 绘制顶部徽章背景与文字
        font.drawInBatch(header, headerX, startY, -1, false, matrix4f, buffer,
                Font.DisplayMode.SEE_THROUGH, 0x880A101D, packedLight);
        font.drawInBatch(header, headerX, startY, -1, false, matrix4f, buffer,
                Font.DisplayMode.NORMAL, 0, packedLight);

        // 绘制台词内容
        int lineY = startY + 12;
        for (FormattedCharSequence line : lines) {
            float lineX = -font.width(line) / 2.0F;
            font.drawInBatch(line, lineX, lineY, -1, false, matrix4f, buffer,
                    Font.DisplayMode.SEE_THROUGH, 0x880A101D, packedLight);
            font.drawInBatch(line, lineX, lineY, -1, false, matrix4f, buffer,
                    Font.DisplayMode.NORMAL, 0, packedLight);
            lineY += 11;
        }

        poseStack.popPose();
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
    public ResourceLocation getTextureLocation(AiPlayerNpcEntity entity) {
        if (com.moonproject.moonmod.client.bedrock.BedrockPackManager.hasCustomModel()) {
            return com.moonproject.moonmod.client.bedrock.BedrockPackManager.getActiveTexture();
        }
        return DEEPSEEK_FISH_TEXTURE;
    }
}
