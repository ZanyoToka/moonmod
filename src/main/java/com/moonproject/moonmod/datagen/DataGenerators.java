package com.moonproject.moonmod.datagen;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.datagen.providers.ModItemModelProvider;
import com.moonproject.moonmod.datagen.providers.ModLanguageProvider;
import com.moonproject.moonmod.datagen.providers.ModRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 数据生成入口（规划第 4 章 datagen/）。
 * 运行 data 配置（IntelliJ 里选 runData，或命令行 {@code gradlew runData}）时，
 * 自动把配方 / 物品模型 / 语言文件生成到 src/generated/resources/。
 * 新内容优先写 provider 自动生成，避免手改 JSON 拼错。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // 服务端数据：制作表、战利品表、进度、标签
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
        // 客户端数据：物品模型、语言文件
        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(packOutput, MoonProjectMod.MODID, existingFileHelper));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(packOutput, MoonProjectMod.MODID, "zh_cn"));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(packOutput, MoonProjectMod.MODID, "en_us"));
    }
}
