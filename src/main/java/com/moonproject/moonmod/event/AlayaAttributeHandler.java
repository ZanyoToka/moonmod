package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.item.AlayaVijnanaItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * 按形态给阿赖耶识下发属性修正。
 *
 * <p>{@link ItemAttributeModifierEvent} 在客户端（渲染 Tooltip）与服务端（装备时）都会触发，
 * 因此两边的判定必须完全一致——这里只读物品 NBT，不依赖任何侧别状态。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class AlayaAttributeHandler {
    private static final UUID DAMAGE_UUID = UUID.fromString("2f0b8f1c-6f4a-4a5e-9c3d-0f6a1b7c9e01");
    private static final UUID SPEED_UUID = UUID.fromString("2f0b8f1c-6f4a-4a5e-9c3d-0f6a1b7c9e02");
    private static final UUID REACH_UUID = UUID.fromString("2f0b8f1c-6f4a-4a5e-9c3d-0f6a1b7c9e03");

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AlayaVijnanaItem)) {
            return;
        }
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }

        boolean drawn = AlayaVijnanaItem.isDrawn(stack);

        event.clearModifiers();
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_UUID,
                "alaya_vijnana_damage",
                drawn ? MoonProjectConfig.drawnDamage() : MoonProjectConfig.sheathedDamage(),
                AttributeModifier.Operation.ADDITION));
        event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_UUID,
                "alaya_vijnana_attack_speed",
                drawn ? MoonProjectConfig.drawnAttackSpeed() : MoonProjectConfig.sheathedAttackSpeed(),
                AttributeModifier.Operation.ADDITION));

        // 大太刀的攻击距离：Forge 的 forge:entity_reach（原版默认 3.0 格）
        double reach = MoonProjectConfig.entityReachBonus();
        if (reach > 0.0D) {
            event.addModifier(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_UUID,
                    "alaya_vijnana_reach", reach, AttributeModifier.Operation.ADDITION));
        }
    }

    private AlayaAttributeHandler() {
    }
}
