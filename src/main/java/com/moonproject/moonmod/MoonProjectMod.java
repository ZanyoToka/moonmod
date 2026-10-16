package com.moonproject.moonmod;

import com.mojang.logging.LogUtils;
import com.moonproject.moonmod.item.ModCreativeTab;
import com.moonproject.moonmod.item.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * 「月亮计划」(Project Moon) 同人模组主类。
 * modid 为 "moonproject"，与 mods.toml 中保持一致。
 */
@Mod(MoonProjectMod.MODID)
public class MoonProjectMod {
    public static final String MODID = "moonproject";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MoonProjectMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 注册物品
        ModItems.ITEMS.register(modEventBus);
        // 注册创造标签页
        ModCreativeTab.TABS.register(modEventBus);

        LOGGER.info("[月亮计划] 模组加载成功。");
    }
}
