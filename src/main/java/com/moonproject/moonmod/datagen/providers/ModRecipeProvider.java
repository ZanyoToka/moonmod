package com.moonproject.moonmod.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

/**
 * 制作表自动生成（规划：约 40 条配方，现有手写 JSON 在 data/moonproject/recipes/）。
 * 新配方在此按示例追加，运行 runData 后自动输出 JSON。
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // 示例：脑啡肽 9 合 1 浓缩
        // ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CONCENTRATED_ENKEPHALIN.get())
        //         .pattern("EEE").pattern("EEE").pattern("EEE")
        //         .define('E', ModItems.ENKEPHALIN.get())
        //         .unlockedBy("has_enkephalin", has(ModItems.ENKEPHALIN.get()))
        //         .save(consumer);
        //
        // 熔炉类（烧书炉产余烬）示例：
        // SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.BOOK.get()),
        //         RecipeCategory.MISC, ModItems.ASHES.get(), 0.35F, 200)
        //         .unlockedBy("has_book", has(ModItems.BOOK.get()))
        //         .save(consumer, new ResourceLocation(MoonProjectMod.MODID, "burn_book"));
    }
}
