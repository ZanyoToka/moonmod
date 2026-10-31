package com.moonproject.moonmod.block;

import com.moonproject.moonmod.gui.ReinforcedPlatingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * 都市重装防爆壁 (Reinforced Plating Block):
 * 脑叶公司收容室的核心装甲护壁，具有极高硬度与超强防爆能力。
 * 六面包裹收容单元可大幅提高收容室结构稳定性，减缓逆卡巴拉计数的衰减速率。
 */
public class ReinforcedPlatingBlock extends Block {

    public ReinforcedPlatingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public ReinforcedPlatingBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(50.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops());
    }

    public static int countAdjacentPlatings(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof ReinforcedPlatingBlock) {
                count++;
            }
        }
        return count;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new ReinforcedPlatingMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), new SimpleContainerData(4)),
                    Component.translatable("block.moonproject.reinforced_plating")
            ), pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
