package com.moonproject.moonmod.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * 语言文件自动生成（zh_cn / en_us 各跑一份）。
 * 一行 add(物品, 名字)，运行 runData 后合并进 assets/moonproject/lang/。
 */
public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        // 示例：
        // add(ModItems.ABNORMALITY_FRAGMENT.get(), "异想体碎片");
        // add(ModItems.ENKEPHALIN.get(), "脑啡肽");
        // add(ModItems.EGO_CORE.get(), "E.G.O 核心");
        // 实体与音效同理：add(ModEntities.XXX.get(), "xxx");
    }
}
