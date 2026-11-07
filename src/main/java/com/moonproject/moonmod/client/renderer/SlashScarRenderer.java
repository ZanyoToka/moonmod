package com.moonproject.moonmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.moonproject.moonmod.entity.SlashScarEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * 刀光残留的渲染器：一张始终正对镜头的弧形面片（billboard），按寿命淡入淡出。
 *
 * <p>为什么用 billboard 而不是真实几何：斩痕本身是「光」——没有厚度，也没有方向。
 * 让面片永远面向镜头，无论玩家从正面还是侧面看，都能看到完整的一道弧。</p>
 */
public class SlashScarRenderer extends EntityRenderer<SlashScarEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/entity/slash_scar.png");

    /** 淡入时长（tick）。 */
    private static final float FADE_IN_TICKS = 3.0F;
    /** 淡出时长（tick）：寿命最后两秒开始变淡。 */
    private static final float FADE_OUT_TICKS = 40.0F;
    /** 贴图宽高比：贴图为 512x256，高度是宽度的一半。 */
    private static final float ASPECT = 0.5F;

    public SlashScarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SlashScarEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(SlashScarEntity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        // 面片很大且可能只有一部分在视锥内，直接放行，避免侧看时整道弧被剔除掉
        return true;
    }

    @Override
    public void render(SlashScarEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float life = Math.max(1.0F, entity.getLife());
        float age = Math.min(entity.tickCount + partialTick, life);
        float fadeIn = Mth.clamp(age / FADE_IN_TICKS, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((life - age) / FADE_OUT_TICKS, 0.0F, 1.0F);
        float alpha = fadeIn * fadeOut;
        if (alpha <= 0.01F) {
            return;
        }

        poseStack.pushPose();
        // 面向镜头 + 自身的旋转角
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll()));

        float halfW = entity.getScale() * 0.5F;
        float halfH = halfW * ASPECT;

        // 颜色一律走白色（贴图本身已是白弧）；概念抹除稍偏冷，普通斩击纯白
        boolean erase = entity.getVariant() == 1;
        float red = erase ? 0.90F : 1.0F;
        float green = erase ? 0.95F : 1.0F;
        float blue = 1.0F;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        // 正面 + 背面各画一次：省掉 cull 状态的纠结，侧看也不会消失
        quad(consumer, matrix, halfW, halfH, red, green, blue, alpha, false);
        quad(consumer, matrix, halfW, halfH, red, green, blue, alpha, true);

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, float halfW, float halfH,
                             float red, float green, float blue, float alpha, boolean flipped) {
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        if (flipped) {
            vertex(consumer, matrix, halfW, -halfH, u0, v1, red, green, blue, alpha);
            vertex(consumer, matrix, -halfW, -halfH, u1, v1, red, green, blue, alpha);
            vertex(consumer, matrix, -halfW, halfH, u1, v0, red, green, blue, alpha);
            vertex(consumer, matrix, halfW, halfH, u0, v0, red, green, blue, alpha);
        } else {
            vertex(consumer, matrix, -halfW, -halfH, u0, v1, red, green, blue, alpha);
            vertex(consumer, matrix, halfW, -halfH, u1, v1, red, green, blue, alpha);
            vertex(consumer, matrix, halfW, halfH, u1, v0, red, green, blue, alpha);
            vertex(consumer, matrix, -halfW, halfH, u0, v0, red, green, blue, alpha);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v,
                               float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
