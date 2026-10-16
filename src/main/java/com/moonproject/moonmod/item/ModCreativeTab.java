package com.moonproject.moonmod.item;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 「月亮计划」创造模式标签页。
 */
public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(ForgeRegistries.Keys.CREATIVE_MODE_TABS, MoonProjectMod.MODID);

    public static final RegistryObject<CreativeModeTab> MOON_TAB = TABS.register("moon_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.moonproject"))
                    .icon(() -> new ItemStack(ModItems.REGRET.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ABNORMALITY_FRAGMENT.get());
                        output.accept(ModItems.EGO_CORE.get());
                        output.accept(ModItems.REGRET.get());
                    })
                    .build());
}
