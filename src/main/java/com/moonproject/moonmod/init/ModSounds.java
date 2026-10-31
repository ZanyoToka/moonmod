package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 音效注册表（规划第 4 章：武器挥砍/枪声、异想体叫声、机器运转、警报）。
 * 当前为空骨架，新增音效示例：
 *
 * <pre>
 * public static final RegistryObject&lt;SoundEvent&gt; ALAYA_SLASH =
 *     SOUNDS.register("alaya_slash", () -&gt; SoundEvent.createVariableRangeEvent(
 *         new ResourceLocation(MoonProjectMod.MODID, "alaya_slash")));
 * </pre>
 *
 * 配套工作：assets/moonproject/sounds/ 下放 ogg 文件 + sounds.json 条目，
 * 再在武器攻击事件里 player.level().playSound(...) 播放。
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MoonProjectMod.MODID);
}
