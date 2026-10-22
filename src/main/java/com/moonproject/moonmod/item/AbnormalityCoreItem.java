package com.moonproject.moonmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 「异想体核心」这一类物品的基类。
 *
 * <p>每个核心对应一个异想体：{@link #coreId} 是这个异想体的标识（如 {@code scorched_girl}），
 * 工作方块中央槽只接受本类物品，并按 {@code coreId} 决定产出与出逃结果。</p>
 *
 * <p>名字不需要叫「异想体核心」——每个核心用自己的名字（例如焦化少女的核心叫「终末火柴」）。</p>
 */
public class AbnormalityCoreItem extends Item {

    private final String coreId;

    public AbnormalityCoreItem(String coreId, Properties properties) {
        super(properties);
        this.coreId = coreId;
    }

    /** 该核心对应的异想体标识。 */
    public String getCoreId() {
        return coreId;
    }

    /** 从物品栈读取核心标识；不是核心则返回 null。 */
    public static String getCoreId(ItemStack stack) {
        return stack.getItem() instanceof AbnormalityCoreItem core ? core.getCoreId() : null;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack);
    }

    /** 在物品提示里标注「这是一枚异想体核心」，并显示对应异想体。 */
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level,
                                java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§5异想体核心").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("§7对应异想体：§d" + displayName(coreId))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.literal("§8放入场谐振能源采集单元使用")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }

    /** coreId 转显示名。 */
    private static String displayName(String id) {
        return switch (id) {
            case "scorched_girl" -> "焦化少女";
            default -> id;
        };
    }
}