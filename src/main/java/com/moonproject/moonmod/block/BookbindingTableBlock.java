package com.moonproject.moonmod.block;

import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import com.moonproject.moonmod.gui.BookbindingTableMenu;

/**
 * 【总类层装订台】 (Bookbinding Table)
 * <p>图书馆特色高级合成工作台。由罗兰与安吉拉在总类层整理知识、装订光之书页的坚固工作台。
 * 右键打开专属装订界面，伴随轻柔的翻书声与金色纸屑附魔微粒，并抚平使用者的精神躁动。</p>
 */
public class BookbindingTableBlock extends CraftingTableBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Component CONTAINER_TITLE = Component.translatable("container.moonmod.bookbinding_table");

    public BookbindingTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            // 客户端产生金色附魔书页飞扬粒子
            for (int i = 0; i < 6; i++) {
                double px = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.8D;
                double py = pos.getY() + 1.0D + level.random.nextDouble() * 0.3D;
                double pz = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.8D;
                level.addParticle(ParticleTypes.ENCHANT, px, py, pz, (level.random.nextDouble() - 0.5D) * 0.5D, 0.2D, (level.random.nextDouble() - 0.5D) * 0.5D);
            }
            return InteractionResult.SUCCESS;
        }

        // 服务端逻辑：翻书声 + 精神抚平 + 打开装订界面
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.6F, 1.2F);

        // 微量理智回复（专注阅览带来的平静）
        if (Sanity.hasSanity(player)) {
            Sanity.add(player, 1);
        }

        player.displayClientMessage(
                Component.literal("§6[总类层装订台] §f翻阅光芒汇聚之书，知识在此装订成册。"),
                true
        );

        player.openMenu(state.getMenuProvider(level, pos));
        return InteractionResult.CONSUME;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) -> new BookbindingTableMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE
        );
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // 偶发性地向装订台上飘散金色知识光尘
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
            double y = pos.getY() + 1.05D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.1D, 0.0D);
        }
    }
}
