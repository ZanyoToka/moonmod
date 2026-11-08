package com.moonproject.moonmod.client.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * 基岩版 (Bedrock Edition) 骨骼节点 (Bedrock Part / Bone)
 * 支持层级父子树形变换、独立轴心点 Pivot 旋转、动态动作角度与物理飘带微调。
 */
public class BedrockPart {
    public final String name;
    public final float pivotX, pivotY, pivotZ;
    public final float initRotX, initRotY, initRotZ;

    // 运行行动画与逻辑动态注入的角度与位移 (弧度)
    public float xRot, yRot, zRot;
    public float offsetX, offsetY, offsetZ;
    public boolean visible = true;

    private final List<BedrockCube> cubes = new ArrayList<>();
    private final List<BedrockPart> children = new ArrayList<>();

    public BedrockPart(String name, float pivotX, float pivotY, float pivotZ,
                       float initRotX, float initRotY, float initRotZ) {
        this.name = name;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.initRotX = initRotX;
        this.initRotY = initRotY;
        this.initRotZ = initRotZ;
    }

    public void addCube(BedrockCube cube) {
        if (cube != null) {
            this.cubes.add(cube);
        }
    }

    public void addChild(BedrockPart child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    public List<BedrockPart> getChildren() {
        return children;
    }

    public void resetPose() {
        this.xRot = 0.0F;
        this.yRot = 0.0F;
        this.zRot = 0.0F;
        this.offsetX = 0.0F;
        this.offsetY = 0.0F;
        this.offsetZ = 0.0F;
        for (BedrockPart child : children) {
            child.resetPose();
        }
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha, float texW, float texH) {
        if (!visible) return;

        poseStack.pushPose();

        // 1. 平移至基岩骨骼 Pivot 旋转中心点 (除以 16 映射到 MC 原版 1 格 = 16 像素)
        poseStack.translate(pivotX / 16.0F + offsetX, pivotY / 16.0F + offsetY, pivotZ / 16.0F + offsetZ);

        // 2. 叠加初始旋转与动画动态旋转 (Z -> Y -> X 顺规)
        float totalRotZ = initRotZ + zRot;
        float totalRotY = initRotY + yRot;
        float totalRotX = initRotX + xRot;

        if (totalRotZ != 0.0F) poseStack.mulPose(Axis.ZP.rotation(totalRotZ));
        if (totalRotY != 0.0F) poseStack.mulPose(Axis.YP.rotation(totalRotY));
        if (totalRotX != 0.0F) poseStack.mulPose(Axis.XP.rotation(totalRotX));

        // 3. 反平移回局部渲染坐标系
        poseStack.translate(-pivotX / 16.0F, -pivotY / 16.0F, -pivotZ / 16.0F);

        // 4. 渲染本骨骼的所有立方体几何方块
        PoseStack.Pose pose = poseStack.last();
        for (BedrockCube cube : cubes) {
            cube.compile(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha, texW, texH);
        }

        // 5. 递归渲染子骨骼节点
        for (BedrockPart child : children) {
            child.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, texW, texH);
        }

        poseStack.popPose();
    }
}
