package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.block.ExpBurnerBlock;
import com.moonproject.moonmod.block.ResonanceCollectorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MoonProjectMod.MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MoonProjectMod.MODID);

    // 经验熔炉方块
    /** 场谐振能源采集单元：从异想体提取脑啡肽的装置（功能待实现）。 */
    public static final RegistryObject<Block> RESONANCE_COLLECTOR = registerBlock("resonance_collector",
            ResonanceCollectorBlock::new);

    public static final RegistryObject<Block> EXP_BURNER = registerBlock("exp_burner",
            () -> new ExpBurnerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
            ));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockSupplier) {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        registerBlockItem(name, block);
        return block;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}