package com.moonproject.moonmod.client;

import com.moonproject.moonmod.entity.SeatEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 座椅隐形骑乘实体的空渲染器。
 */
public class SeatRenderer extends EntityRenderer<SeatEntity> {
    public SeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SeatEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(SeatEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }
}
