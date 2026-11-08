package com.moonproject.moonmod.client.bedrock;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用基岩版实体模型 (Bedrock Entity Model)
 * 支持 Blockbench 基岩版 JSON 格式规范。
 * 能够自动将实体姿势、行走摆动、头部朝向以及 12 种情绪动作与飘带物理映射到基岩骨骼。
 */
public class BedrockModel extends EntityModel<AiPlayerNpcEntity> {

    public final float texWidth;
    public final float texHeight;
    private final List<BedrockPart> rootBones = new ArrayList<>();
    private final Map<String, BedrockPart> allBones = new HashMap<>();

    public BedrockModel(float texWidth, float texHeight) {
        this.texWidth = texWidth > 0 ? texWidth : 64.0F;
        this.texHeight = texHeight > 0 ? texHeight : 64.0F;
    }

    public void addRootBone(BedrockPart part) {
        if (part != null) {
            this.rootBones.add(part);
            registerBoneRecursive(part);
        }
    }

    private void registerBoneRecursive(BedrockPart part) {
        this.allBones.put(part.name.toLowerCase(), part);
        for (BedrockPart child : part.getChildren()) {
            registerBoneRecursive(child);
        }
    }

    public BedrockPart getBone(String name) {
        if (name == null) return null;
        return allBones.get(name.toLowerCase());
    }

    @Override
    public void setupAnim(AiPlayerNpcEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 1. 重置所有骨骼初始姿势
        for (BedrockPart root : rootBones) {
            root.resetPose();
        }

        // 2. 查找标准解剖骨骼 (兼顾 Blockbench 命名与车万女仆命名)
        BedrockPart head = findBone("head");
        BedrockPart body = findBone("body", "waist", "torso");
        BedrockPart rightArm = findBone("right_arm", "armright", "rightarm");
        BedrockPart leftArm = findBone("left_arm", "armleft", "leftarm");
        BedrockPart rightLeg = findBone("right_leg", "legright", "rightleg");
        BedrockPart leftLeg = findBone("left_leg", "legleft", "leftleg");

        // 3. 头部转动
        if (head != null) {
            head.yRot = netHeadYaw * ((float) Math.PI / 180F);
            head.xRot = headPitch * ((float) Math.PI / 180F);
        }

        // 4. 行走步态摆动
        if (rightArm != null && leftArm != null && rightLeg != null && leftLeg != null) {
            rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
            leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        }

        // 5. 待机呼吸微动态
        if (rightArm != null && leftArm != null) {
            rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
            leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
        }

        // 6. 物理骨骼飘带 / 呆毛 / 裙摆 / 侧发微摆动
        BedrockPart ahoge = findBone("ahoge", "hair", "hair_tail");
        if (ahoge != null) {
            ahoge.zRot += Mth.sin(ageInTicks * 0.12F) * 0.1F;
        }
        BedrockPart skirt = findBone("skirt", "skirt_front", "skirt_back");
        if (skirt != null) {
            skirt.xRot += Mth.sin(limbSwing * 0.6662F) * 0.15F * limbSwingAmount;
        }

        // 7. 映射 12 种情绪具身骨骼动作
        byte animState = entity.getAnimationState();
        switch (animState) {
            case AiPlayerNpcEntity.ANIM_WAVE -> {
                if (rightArm != null) {
                    rightArm.xRot = -2.8F;
                    rightArm.zRot = -0.3F + Mth.sin(ageInTicks * 0.6F) * 0.35F;
                }
            }
            case AiPlayerNpcEntity.ANIM_TALK -> {
                if (head != null) {
                    head.xRot += Mth.sin(ageInTicks * 0.3F) * 0.12F;
                }
            }
            case AiPlayerNpcEntity.ANIM_THINK -> {
                if (head != null) head.zRot = 0.25F;
                if (rightArm != null) {
                    rightArm.xRot = -1.8F;
                    rightArm.yRot = -0.6F;
                }
            }
            case AiPlayerNpcEntity.ANIM_HAPPY -> {
                if (rightArm != null) rightArm.xRot = -2.9F;
                if (leftArm != null) leftArm.xRot = -2.9F;
            }
            case AiPlayerNpcEntity.ANIM_BOW -> {
                if (body != null) body.xRot = 0.6F;
                if (head != null) head.xRot = 0.4F;
            }
            case AiPlayerNpcEntity.ANIM_CLAP -> {
                if (rightArm != null && leftArm != null) {
                    rightArm.xRot = -1.5F;
                    leftArm.xRot = -1.5F;
                    float clap = Mth.sin(ageInTicks * 0.8F) * 0.25F;
                    rightArm.yRot = -0.4F + clap;
                    leftArm.yRot = 0.4F - clap;
                }
            }
            case AiPlayerNpcEntity.ANIM_DANCE -> {
                if (body != null) {
                    body.zRot = Mth.sin(ageInTicks * 0.25F) * 0.2F;
                    body.yRot = Mth.cos(ageInTicks * 0.2F) * 0.25F;
                }
            }
        }
    }

    private BedrockPart findBone(String... names) {
        for (String name : names) {
            BedrockPart part = allBones.get(name.toLowerCase());
            if (part != null) return part;
        }
        return null;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        for (BedrockPart root : rootBones) {
            root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, texWidth, texHeight);
        }
    }
}
