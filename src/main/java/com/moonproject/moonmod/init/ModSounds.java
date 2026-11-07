package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 音效注册表。
 *
 * <p><b>白盒：音效是怎么响的？</b></p>
 * <ol>
 *   <li>这里的 {@link RegistryObject} 只声明「有这么一个事件名」，实际音量/音高由播放时的
 *       {@code playSound(..., volume, pitch)} 决定。</li>
 *   <li>事件名与 {@code assets/moonproject/sounds.json} 的键一一对应，sounds.json 再把键
 *       映射到 {@code assets/moonproject/sounds/<名字>.ogg}。改文件路径只改 sounds.json，
 *       改播放时机只改调用处，两边解耦。</li>
 *   <li>所有音效都是<b>服务端</b>播放（{@code level.playSound(null, ...)}），由网络同步到
 *       附近客户端——不要只在客户端播，否则别人听不到。</li>
 * </ol>
 *
 * <p>当前音效全部由一次性脚本程序合成（正弦/噪声/包络），不依赖任何外部素材，
 * 因此不涉及第三方音频版权。</p>
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MoonProjectMod.MODID);

    /** 拔刀：金属出鞘摩擦 + 低频闷响。 */
    public static final RegistryObject<SoundEvent> ALAYA_DRAW = register("alaya_draw");
    /** 收刀：刀身回鞘的咔哒与轻响。 */
    public static final RegistryObject<SoundEvent> ALAYA_SHEATH = register("alaya_sheath");
    /** 普通挥砍的空气切割声。 */
    public static final RegistryObject<SoundEvent> ALAYA_SLASH = register("alaya_slash");
    /** 重斩（天杀 / 抹去）的低沉破空声。 */
    public static final RegistryObject<SoundEvent> ALAYA_SLASH_HEAVY = register("alaya_slash_heavy");
    /** 概念抹除：上升失真与骤停。 */
    public static final RegistryObject<SoundEvent> ALAYA_ERASE = register("alaya_erase");
    /** 记忆灼烧：细碎爆裂与嘶声。 */
    public static final RegistryObject<SoundEvent> ALAYA_MEMORY_BURN = register("alaya_memory_burn");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(MoonProjectMod.MODID, name)));
    }
}
