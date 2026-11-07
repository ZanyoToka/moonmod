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
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoonProjectMod.MODID);

    public static final RegistryObject<CreativeModeTab> MOON_TAB = TABS.register("moon_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.moonproject"))
                    .icon(() -> new ItemStack(ModBlocks.CONTAINMENT_UNIT.get()))
                    .displayItems((parameters, output) -> {
                        // 物品与装备
                        output.accept(ModItems.ABNORMALITY_FRAGMENT.get());
                        output.accept(ModItems.EGO_CORE.get());
                        output.accept(ModItems.ABNORMALITY_CORE.get());
                        output.accept(ModItems.FINAL_MATCH.get());
                        output.accept(ModItems.FINAL_MATCH_LIGHT.get());
                        // 人格卡：等级各展示一张，具体身份由 NBT 决定（见 IdentityCardData）
                        output.accept(IdentityCardItem.create("urban_fixer"));
                        output.accept(IdentityCardItem.create("liu_association"));
                        output.accept(IdentityCardItem.create("south4_manager"));
                        output.accept(ModItems.ENKEPHALIN.get());
                        output.accept(ModItems.SCORCHED_GIRL_SPAWN_EGG.get());
                        output.accept(ModItems.REGRET.get());
                        output.accept(ModItems.ALAYA_VIJNANA.get());
                        output.accept(ModItems.EGO_HELMET.get());
                        output.accept(ModItems.EGO_CHESTPLATE.get());
                        output.accept(ModItems.EGO_LEGGINGS.get());
                        output.accept(ModItems.EGO_BOOTS.get());

                        // 方块设施
                        output.accept(ModBlocks.EXP_BURNER.get());
                        output.accept(ModBlocks.RESONANCE_COLLECTOR.get());
                        output.accept(ModBlocks.MEPHISTOPHELES_SEAT.get());
                        output.accept(ModBlocks.CONTAINMENT_UNIT.get());
                        output.accept(ModBlocks.ENKEPHALIN_EXTRACTOR.get());
                        output.accept(ModBlocks.MELTDOWN_SIREN.get());
                        output.accept(ModBlocks.REINFORCED_PLATING.get());

                        // 图书馆与边狱工坊设施
                        output.accept(ModBlocks.BOOKBINDING_TABLE.get());
                        output.accept(ModBlocks.IDENTITY_EXTRACTOR.get());
                        output.accept(ModBlocks.MEPHISTOPHELES_ENGINE.get());
                        output.accept(ModBlocks.KCORP_INJECTOR.get());
                        output.accept(ModBlocks.GOLDEN_BOUGH_SHRINE.get());
                    })
                    .build());
}
