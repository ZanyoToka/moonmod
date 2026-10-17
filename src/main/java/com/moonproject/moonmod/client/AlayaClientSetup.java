package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.item.AlayaVijnanaItem;
import com.moonproject.moonmod.item.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端渲染准备：为阿赖耶识注册「形态」模型谓词，用来切换鞘击/拔刀两张贴图。
 * 谓词值 1.0 = 解放态（模型 overrides 写在 alaya_vijnana.json 里）。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AlayaClientSetup {
    /** 模型谓词名（用非过时工厂方法，避免 new ResourceLocation(String,String) 的移除警告）。 */
    public static final ResourceLocation DRAWN_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "drawn");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.ALAYA_VIJNANA.get(), DRAWN_PROPERTY,
                (stack, level, entity, seed) -> AlayaVijnanaItem.isDrawn(stack) ? 1.0F : 0.0F));
    }

    private AlayaClientSetup() {
    }
}
