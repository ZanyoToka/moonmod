package com.moonproject.moonmod.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 物品模型自动生成（手持平铺贴图）。
 * 新物品加一行 basicItem(...)，运行 runData 生成对应的 models/item/xxx.json。
 * 刀剑等特殊模型（OBJ / GeckoLib）不在此生成，见 client/obj/ 与 animmodels/。
 */
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // 示例：
        // basicItem(ModItems.ABNORMALITY_FRAGMENT.get()); // models/item/abnormality_fragment.json
        // basicItem(ModItems.EGO_CORE.get());
        // basicItem(ModItems.ENKEPHALIN.get());
    }
}
