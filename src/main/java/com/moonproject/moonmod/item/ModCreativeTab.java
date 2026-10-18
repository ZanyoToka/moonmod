package com.moonproject.moonmod.item;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.init.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 「月亮计划」创造模式标签页。
 */
public class ModCreativeTab {
    // 1.20.1 的创造模式标签页注册键是 Registries.CREATIVE_MODE_TAB
    // （ForgeRegistries.Keys 里没有 CREATIVE_MODE_TABS，写它会编译不过）
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoonProjectMod.MODID);

    public static final RegistryObject<CreativeModeTab> MOON_TAB = TABS.register("moon_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.moonproject"))
                    .icon(() -> new ItemStack(ModItems.REGRET.get()))
                    .displayItems((parameters, output) -> {
                        // 物品与装备
                        output.accept(ModItems.ABNORMALITY_FRAGMENT.get());
                        output.accept(ModItems.EGO_CORE.get());
                output.accept(ModItems.ABNORMALITY_CORE.get());
                output.accept(ModItems.ENKEPHALIN.get());
                        output.accept(ModItems.REGRET.get());
                        output.accept(ModItems.ALAYA_VIJNANA.get());
                        output.accept(ModItems.EGO_HELMET.get());
                        output.accept(ModItems.EGO_CHESTPLATE.get());
                        output.accept(ModItems.EGO_LEGGINGS.get());
                        output.accept(ModItems.EGO_BOOTS.get());

                        // 方块：经验熔炉
                        output.accept(ModBlocks.EXP_BURNER.get());
                output.accept(ModBlocks.RESONANCE_COLLECTOR.get());
                    })
                    .build());
}