package com.moonproject.moonmod.client.model;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * 假人玩家专属 3D 骨骼动作模型
 * 扩展原版 PlayerModel，注入 12 种生动拟人的骨骼动画姿态：
 * 招手、托腮沉思、欢呼、说话、鞠躬、气恼叉腰、垂头沮丧、鼓掌、惊讶捂嘴、傲娇挺胸、快乐摇摆舞。
 * 与 LittleSkin 大肥鱼皮肤 100% 严丝合缝，流畅自然且性能极优。
 */
public class AiPlayerNpcModel extends PlayerModel<AiPlayerNpcEntity> {

    public AiPlayerNpcModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(AiPlayerNpcEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        byte anim = entity.getAnimationState();

        switch (anim) {
            case AiPlayerNpcEntity.ANIM_WAVE -> {
                // 1. 热情招手动作：右手高举轻快摆动
                this.rightArm.xRot = -2.6F;
                this.rightArm.yRot = 0.0F;
                this.rightArm.zRot = Mth.sin(ageInTicks * 0.4F) * 0.35F - 0.2F;
            }
            case AiPlayerNpcEntity.ANIM_TALK -> {
                // 2. 说话点头动作：头部伴随语调自然起伏
                this.head.xRot += Mth.sin(ageInTicks * 0.35F) * 0.08F;
            }
            case AiPlayerNpcEntity.ANIM_THINK -> {
                // 3. 托腮沉思动作：右手托下巴，头微歪
                this.rightArm.xRot = -1.6F;
                this.rightArm.yRot = -0.55F;
                this.rightArm.zRot = 0.25F;
                this.head.zRot = 0.12F;
                this.head.xRot += 0.15F;
            }
            case AiPlayerNpcEntity.ANIM_HAPPY -> {
                // 4. 双手高举欢呼跳跃
                float wave = Mth.sin(ageInTicks * 0.3F) * 0.2F;
                this.rightArm.xRot = -2.8F + wave;
                this.rightArm.zRot = 0.45F;
                this.leftArm.xRot = -2.8F - wave;
                this.leftArm.zRot = -0.45F;
            }
            case AiPlayerNpcEntity.ANIM_BOW -> {
                // 5. 优雅鞠躬致意
                this.body.xRot = 0.35F;
                this.head.xRot += 0.3F;
                this.rightArm.xRot = 0.25F;
                this.leftArm.xRot = 0.25F;
            }
            case AiPlayerNpcEntity.ANIM_ANGRY -> {
                // 6. 叉腰气恼跺脚：双手叉腰，头左右轻微摇晃
                this.rightArm.xRot = -0.35F;
                this.rightArm.yRot = -0.55F;
                this.rightArm.zRot = 0.55F;
                this.leftArm.xRot = -0.35F;
                this.leftArm.yRot = 0.55F;
                this.leftArm.zRot = -0.55F;
                this.head.yRot += Mth.sin(ageInTicks * 0.4F) * 0.15F;
            }
            case AiPlayerNpcEntity.ANIM_SAD -> {
                // 7. 垂头沮丧：低头垂目，双臂无力垂下
                this.head.xRot = 0.52F;
                this.rightArm.xRot = 0.15F;
                this.rightArm.zRot = 0.06F;
                this.leftArm.xRot = 0.15F;
                this.leftArm.zRot = -0.06F;
            }
            case AiPlayerNpcEntity.ANIM_CLAP -> {
                // 8. 鼓掌赞赏：双手胸前高频拍手
                float clapDelta = Mth.sin(ageInTicks * 0.75F) * 0.28F;
                this.rightArm.xRot = -1.25F;
                this.rightArm.yRot = -0.55F + clapDelta;
                this.leftArm.xRot = -1.25F;
                this.leftArm.yRot = 0.55F - clapDelta;
                this.head.xRot += Mth.sin(ageInTicks * 0.35F) * 0.05F;
            }
            case AiPlayerNpcEntity.ANIM_SURPRISE -> {
                // 9. 惊讶捂嘴：双手微收至嘴前，身体后仰
                this.head.xRot = -0.22F;
                this.rightArm.xRot = -1.5F;
                this.rightArm.yRot = -0.38F;
                this.rightArm.zRot = 0.2F;
                this.leftArm.xRot = -1.5F;
                this.leftArm.yRot = 0.38F;
                this.leftArm.zRot = -0.2F;
            }
            case AiPlayerNpcEntity.ANIM_PROUD -> {
                // 10. 傲娇挺胸：手插腰，仰头得意
                this.rightArm.xRot = -0.2F;
                this.rightArm.yRot = -0.5F;
                this.rightArm.zRot = 0.48F;
                this.leftArm.xRot = -0.2F;
                this.leftArm.yRot = 0.5F;
                this.leftArm.zRot = -0.48F;
                this.head.xRot = -0.2F;
                this.head.zRot = Mth.sin(ageInTicks * 0.12F) * 0.06F;
            }
            case AiPlayerNpcEntity.ANIM_DANCE -> {
                // 11. 快乐摇摆舞：左右微晃，双臂伴随节奏摆动
                float sway = Mth.sin(ageInTicks * 0.25F);
                this.body.zRot = sway * 0.12F;
                this.head.zRot = -sway * 0.15F;
                this.rightArm.xRot = -1.1F + sway * 0.35F;
                this.leftArm.xRot = -1.1F - sway * 0.35F;
            }
            default -> {
                // 0. 待机呼吸律动
                this.head.xRot += Mth.sin(ageInTicks * 0.08F) * 0.02F;
            }
        }

        // 将肢体骨骼动作精准同步至第二层外衣模型（大肥鱼外层立体皮肤）
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftSleeve.copyFrom(this.leftArm);
        this.jacket.copyFrom(this.body);
        this.hat.copyFrom(this.head);
    }
}
