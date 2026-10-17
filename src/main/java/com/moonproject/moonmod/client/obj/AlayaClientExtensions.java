package com.moonproject.moonmod.client.obj;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** 把自定义渲染器挂到阿赖耶识物品上。 */
@OnlyIn(Dist.CLIENT)
public final class AlayaClientExtensions implements IClientItemExtensions {
    private static AlayaItemRenderer renderer;

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            renderer = new AlayaItemRenderer();
        }
        return renderer;
    }
}