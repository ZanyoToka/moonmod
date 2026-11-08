package com.moonproject.moonmod.client.bedrock;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 外部模型包与动作资源热更管理器 (Bedrock Pack Hot-Reload Manager)
 * 借鉴车万女仆 CustomPackLoader 机制：
 * 扫描 run/npc_packs 文件夹与外部 ZIP 资源包，支持在游戏运行时免重启热更模型与贴图。
 */
public class BedrockPackManager {
    private static final Logger LOGGER = LogManager.getLogger(BedrockPackManager.class);
    private static final String PACK_DIR_NAME = "npc_packs";

    private static BedrockModel activeBedrockModel = null;
    private static ResourceLocation activeTextureLocation = null;

    public static BedrockModel getActiveModel() {
        return activeBedrockModel;
    }

    public static ResourceLocation getActiveTexture() {
        return activeTextureLocation;
    }

    public static boolean hasCustomModel() {
        return activeBedrockModel != null && activeTextureLocation != null;
    }

    /**
     * 重新加载外部资源包 (热更)
     */
    public static void reload() {
        File packDir = new File(Minecraft.getInstance().gameDirectory, PACK_DIR_NAME);
        if (!packDir.exists()) {
            packDir.mkdirs();
            LOGGER.info("[BedrockPackManager] 创建外部模型目录: {}", packDir.getAbsolutePath());
            return;
        }

        File[] files = packDir.listFiles();
        if (files == null || files.length == 0) {
            LOGGER.info("[BedrockPackManager] 外部模型目录为空，维持原版/默认皮肤渲染。");
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (loadFromDirectory(file)) return;
            } else if (file.getName().endsWith(".zip")) {
                if (loadFromZip(file)) return;
            }
        }
    }

    private static boolean loadFromDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return false;

        File modelFile = null;
        File textureFile = null;

        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (name.endsWith(".geo.json") || name.endsWith("model.json")) {
                modelFile = f;
            } else if (name.endsWith(".png")) {
                textureFile = f;
            }
        }

        if (modelFile != null && textureFile != null) {
            try (InputStream mIn = new FileInputStream(modelFile);
                 InputStream tIn = new FileInputStream(textureFile)) {
                activeBedrockModel = BedrockModelLoader.load(mIn);
                NativeImage nativeImage = NativeImage.read(tIn);
                DynamicTexture dynTex = new DynamicTexture(nativeImage);
                activeTextureLocation = Minecraft.getInstance().getTextureManager().register("npc_custom_pack_" + dir.getName(), dynTex);
                LOGGER.info("[BedrockPackManager] 成功热加载目录模型包: {} (模型: {}, 贴图: {})", dir.getName(), modelFile.getName(), textureFile.getName());
                return true;
            } catch (Exception e) {
                LOGGER.error("[BedrockPackManager] 加载目录模型包失败: {}", dir.getName(), e);
            }
        }
        return false;
    }

    private static boolean loadFromZip(File zipFile) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            ZipEntry modelEntry = null;
            ZipEntry texEntry = null;

            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName().toLowerCase();
                if (name.endsWith(".geo.json") || name.endsWith("model.json")) {
                    modelEntry = entry;
                } else if (name.endsWith(".png") && !name.contains("icon")) {
                    texEntry = entry;
                }
            }

            if (modelEntry != null && texEntry != null) {
                try (InputStream mIn = zip.getInputStream(modelEntry);
                     InputStream tIn = zip.getInputStream(texEntry)) {
                    activeBedrockModel = BedrockModelLoader.load(mIn);
                    NativeImage nativeImage = NativeImage.read(tIn);
                    DynamicTexture dynTex = new DynamicTexture(nativeImage);
                    activeTextureLocation = Minecraft.getInstance().getTextureManager().register("npc_zip_pack_" + zipFile.getName(), dynTex);
                    LOGGER.info("[BedrockPackManager] 成功热加载 ZIP 资源模型包: {}", zipFile.getName());
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("[BedrockPackManager] 加载 ZIP 模型包失败: {}", zipFile.getName(), e);
        }
        return false;
    }
}
