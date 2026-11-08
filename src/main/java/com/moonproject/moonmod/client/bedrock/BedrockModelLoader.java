package com.moonproject.moonmod.client.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 基岩版 (Bedrock Edition) 几何模型解析器
 * 解析 Blockbench / Minecraft Bedrock 1.10.0 / 1.12.0+ .geo.json 格式，构建层级骨骼树。
 */
public class BedrockModelLoader {
    private static final Logger LOGGER = LogManager.getLogger(BedrockModelLoader.class);

    public static BedrockModel load(InputStream in) {
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject geometry = null;

            if (root.has("minecraft:geometry")) {
                JsonArray arr = root.getAsJsonArray("minecraft:geometry");
                if (!arr.isEmpty()) geometry = arr.get(0).getAsJsonObject();
            } else {
                // Legacy 1.10.0 / 1.8.0 格式
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    if (entry.getKey().startsWith("geometry.")) {
                        geometry = entry.getValue().getAsJsonObject();
                        break;
                    }
                }
            }

            if (geometry == null) {
                LOGGER.warn("[BedrockModelLoader] 未在 JSON 中找到可解析的 geometry 对象！");
                return null;
            }

            // 读取贴图尺寸
            float texWidth = 64.0F;
            float texHeight = 64.0F;
            if (geometry.has("description")) {
                JsonObject desc = geometry.getAsJsonObject("description");
                if (desc.has("texture_width")) texWidth = desc.get("texture_width").getAsFloat();
                if (desc.has("texture_height")) texHeight = desc.get("texture_height").getAsFloat();
            } else {
                if (geometry.has("texturewidth")) texWidth = geometry.get("texturewidth").getAsFloat();
                if (geometry.has("textureheight")) texHeight = geometry.get("textureheight").getAsFloat();
            }

            BedrockModel model = new BedrockModel(texWidth, texHeight);

            if (!geometry.has("bones")) {
                return model;
            }

            JsonArray bonesArray = geometry.getAsJsonArray("bones");
            Map<String, BedrockPart> partsByName = new HashMap<>();
            Map<String, String> parentRelations = new HashMap<>();

            for (JsonElement boneEl : bonesArray) {
                JsonObject bObj = boneEl.getAsJsonObject();
                String name = bObj.has("name") ? bObj.get("name").getAsString() : "unnamed";
                String parent = bObj.has("parent") ? bObj.get("parent").getAsString() : null;

                float px = 0, py = 0, pz = 0;
                if (bObj.has("pivot")) {
                    JsonArray pivot = bObj.getAsJsonArray("pivot");
                    px = -pivot.get(0).getAsFloat();
                    py = -pivot.get(1).getAsFloat();
                    pz = pivot.get(2).getAsFloat();
                }

                float rx = 0, ry = 0, rz = 0;
                if (bObj.has("rotation")) {
                    JsonArray rot = bObj.getAsJsonArray("rotation");
                    rx = rot.get(0).getAsFloat() * ((float) Math.PI / 180F);
                    ry = rot.get(1).getAsFloat() * ((float) Math.PI / 180F);
                    rz = rot.get(2).getAsFloat() * ((float) Math.PI / 180F);
                }

                BedrockPart part = new BedrockPart(name, px, py, pz, rx, ry, rz);

                // 解析方块 cubes
                if (bObj.has("cubes")) {
                    JsonArray cubes = bObj.getAsJsonArray("cubes");
                    for (JsonElement cEl : cubes) {
                        JsonObject cObj = cEl.getAsJsonObject();
                        JsonArray origin = cObj.getAsJsonArray("origin");
                        JsonArray size = cObj.getAsJsonArray("size");

                        float ox = -origin.get(0).getAsFloat() - size.get(0).getAsFloat();
                        float oy = -origin.get(1).getAsFloat() - size.get(1).getAsFloat();
                        float oz = origin.get(2).getAsFloat();

                        float sx = size.get(0).getAsFloat();
                        float sy = size.get(1).getAsFloat();
                        float sz = size.get(2).getAsFloat();

                        float u = 0, v = 0;
                        if (cObj.has("uv")) {
                            if (cObj.get("uv").isJsonArray()) {
                                JsonArray uv = cObj.getAsJsonArray("uv");
                                u = uv.get(0).getAsFloat();
                                v = uv.get(1).getAsFloat();
                            }
                        }

                        float inflate = cObj.has("inflate") ? cObj.get("inflate").getAsFloat() : 0.0F;
                        boolean mirror = cObj.has("mirror") && cObj.get("mirror").getAsBoolean();

                        part.addCube(new BedrockCube(ox, oy, oz, sx, sy, sz, u, v, inflate, mirror, texWidth, texHeight));
                    }
                }

                partsByName.put(name.toLowerCase(), part);
                if (parent != null) {
                    parentRelations.put(name.toLowerCase(), parent.toLowerCase());
                }
            }

            // 构建骨骼层级树
            for (Map.Entry<String, BedrockPart> entry : partsByName.entrySet()) {
                String name = entry.getKey();
                BedrockPart part = entry.getValue();
                String parentName = parentRelations.get(name);

                if (parentName != null && partsByName.containsKey(parentName)) {
                    partsByName.get(parentName).addChild(part);
                } else {
                    model.addRootBone(part);
                }
            }

            return model;
        } catch (Exception e) {
            LOGGER.error("[BedrockModelLoader] 解析基岩版模型失败: ", e);
            return null;
        }
    }
}
