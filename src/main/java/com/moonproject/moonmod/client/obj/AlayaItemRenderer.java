package com.moonproject.moonmod.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 阿赖耶识物品渲染器：
 * <ul>
 *   <li>GUI / FIXED：画 2D 官方立绘（保持物品栏图标就是那两张图）</li>
 *   <li>其它视角：渲染 3D OBJ 模型（手持、地面、头饰）</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class AlayaItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation GUI_SHEATHED = ResourceLocation.fromNamespaceAndPath("moonproject", "item/alaya_vijnana");
    private static final ResourceLocation GUI_DRAWN = ResourceLocation.fromNamespaceAndPath("moonproject", "item/alaya_vijnana_drawn");

    public AlayaItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        if (ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.FIXED) {
            renderFlatIcon(stack, pose, buffers, light, overlay);
            return;
        }
        renderObjModel(stack, ctx, pose, buffers, light, overlay);
    }

    /** GUI：把官方贴图当成一张扁平 quad 画出来。 */
    private void renderFlatIcon(ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation tex = ObjItemModel.isDrawn(stack) ? GUI_DRAWN : GUI_SHEATHED;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(tex);
        VertexConsumer consumer = buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutout(tex));
        var matrix = pose.last().pose();
        float s = 1.0F;
        consumer.vertex(matrix, 0.0F, s, 0.0F).color(255, 255, 255, 255)
                .uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, s, s, 0.0F).color(255, 255, 255, 255)
                .uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, s, 0.0F, 0.0F).color(255, 255, 255, 255)
                .uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, 255)
                .uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
    }

    /** 手持等：从已烘焙的 OBJ 模型里取四边形直接渲染。 */
    @SuppressWarnings("deprecation")
    private void renderObjModel(ItemStack stack, ItemDisplayContext ctx, PoseStack pose,
                                MultiBufferSource buffers, int light, int overlay) {
        var model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        if (model instanceof ObjItemModel.ItemObjBakedModel obj) {
            List<BakedQuad> quads = obj.getQuads(null, null, net.minecraft.util.RandomSource.create(42));
            var consumer = ItemRenderer.getFoilBufferDirect(buffers, net.minecraft.client.renderer.RenderType.entityCutout(
                    ResourceLocation.fromNamespaceAndPath("moonproject", "item/blade_solid")), true, stack.hasFoil());
            pose.pushPose();
            pose.scale(0.7F, 0.7F, 0.7F);
            pose.translate(0.5F, 0.0F, 0.5F);
            for (BakedQuad quad : quads) {
                consumer.putBulkData(pose.last(), quad, 1.0F, 1.0F, 1.0F, light, overlay);
            }
            pose.popPose();
        }
    }
}