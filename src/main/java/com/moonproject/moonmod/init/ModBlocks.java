package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.block.ContainmentUnitBlock;
import com.moonproject.moonmod.block.EnkephalinExtractorBlock;
import com.moonproject.moonmod.block.ExpBurnerBlock;
import com.moonproject.moonmod.block.MeltdownSirenBlock;
import com.moonproject.moonmod.block.MephistophelesSeatBlock;
import com.moonproject.moonmod.block.ReinforcedPlatingBlock;
import com.moonproject.moonmod.block.BookbindingTableBlock;
import com.moonproject.moonmod.block.IdentityExtractorBlock;
import com.moonproject.moonmod.block.MephistophelesEngineBlock;
import com.moonproject.moonmod.block.KCorpInjectorBlock;
import com.moonproject.moonmod.block.GoldenBoughShrineBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
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
    public static final RegistryObject<Block> EXP_BURNER = registerBlock("exp_burner",
            () -> new ExpBurnerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(ExpBurnerBlock.LIT) ? 13 : 0)
            ));

    // 边狱巴士特色座椅 —— 梅菲斯特客席
    public static final RegistryObject<Block> MEPHISTOPHELES_SEAT = registerBlock("mephistopheles_seat",
            () -> new MephistophelesSeatBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            ));

    // ===== 脑叶公司收容与设施体系 =====

    /** 异想体收容单元 */
    public static final RegistryObject<Block> CONTAINMENT_UNIT = registerBlock("containment_unit",
            ContainmentUnitBlock::new);

    /** 脑啡肽提取终端 */
    public static final RegistryObject<Block> ENKEPHALIN_EXTRACTOR = registerBlock("enkephalin_extractor",
            EnkephalinExtractorBlock::new);

    /** 脑叶警报广播机 */
    public static final RegistryObject<Block> MELTDOWN_SIREN = registerBlock("meltdown_siren",
            MeltdownSirenBlock::new);

    /** 都市重装防爆壁 */
    public static final RegistryObject<Block> REINFORCED_PLATING = registerBlock("reinforced_plating",
            ReinforcedPlatingBlock::new);

    // ===== 图书馆与边狱工坊体系 =====

    /** 总类层装订台 */
    public static final RegistryObject<Block> BOOKBINDING_TABLE = registerBlock("bookbinding_table",
            () -> new BookbindingTableBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
            ));

    /** 人格抽取共鸣舱 */
    public static final RegistryObject<Block> IDENTITY_EXTRACTOR = registerBlock("identity_extractor",
            () -> new IdentityExtractorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(IdentityExtractorBlock.EXTRACTING) ? 10 : 3)
            ));

    /** 梅菲斯特引擎核心 */
    public static final RegistryObject<Block> MEPHISTOPHELES_ENGINE = registerBlock("mephistopheles_engine",
            () -> new MephistophelesEngineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(4.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(MephistophelesEngineBlock.LIT) ? 13 : 0)
            ));

    /** K公司再生安瓿灌装机 */
    public static final RegistryObject<Block> KCORP_INJECTOR = registerBlock("kcorp_injector",
            () -> new KCorpInjectorBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(2.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(state -> 8)
            ));

    /** 黄金枝基座 / 光之种发芽坛 */
    public static final RegistryObject<Block> GOLDEN_BOUGH_SHRINE = registerBlock("golden_bough_shrine",
            () -> new GoldenBoughShrineBlock(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(GoldenBoughShrineBlock.HAS_BOUGH) ? 15 : 6)
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
