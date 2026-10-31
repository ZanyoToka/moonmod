package com.moonproject.moonmod.trinket;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/** 把 {@link TrinketData} 挂到玩家身上的 Provider。 */
public class TrinketProvider implements ICapabilitySerializable<CompoundTag> {

    /** 能力令牌。 */
    public static final Capability<ITrinketData> TRINKET =
            CapabilityManager.get(new CapabilityToken<>() { });

    private final TrinketData data = new TrinketData();
    private final LazyOptional<ITrinketData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return cap == TRINKET ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        data.save(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.load(nbt);
    }

    /** 读取玩家身上的饰品数据（没有则返回空实现）。 */
    public static ITrinketData get(net.minecraft.world.entity.player.Player player) {
        return player.getCapability(TRINKET).orElse(EMPTY);
    }

    /** 空实现，避免调用方做 null 判断。 */
    private static final ITrinketData EMPTY = new TrinketData();

    public TrinketProvider() {
    }
}
