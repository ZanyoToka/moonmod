package com.moonproject.moonmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moonproject.moonmod.entity.ScorchedGirlEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 焦化少女的渲染器：直接用僵尸的人形模型（{@link HumanoidModel}）+ 自定义皮肤，
 * 整体缩小到 0.55 倍，做出「小僵尸」的体型。
 */
public class ScorchedGirlRenderer
        extends MobRenderer<ScorchedGirlEntity, HumanoidModel<ScorchedGirlEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/entity/scorched_girl.png");

    public ScorchedGirlRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(ScorchedGirlEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(ScorchedGirlEntity entity, PoseStack poseStack, float partialTick) {
        // 小僵尸体型
        poseStack.scale(0.55F, 0.55F, 0.55F);
    }

    @Override
    protected void setupRotations(ScorchedGirlEntity entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick);
        // 引线期间轻微抖动
        if (entity.isFusing()) {
            float shake = (float) Math.sin(ageInTicks * 1.6F) * 1.5F * entity.fuseProgress();
            poseStack.translate(shake * 0.01F, 0.0F, 0.0F);
        }
    }
}