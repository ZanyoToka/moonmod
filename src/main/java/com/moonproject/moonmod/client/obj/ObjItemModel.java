package com.moonproject.moonmod.client.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 物品 OBJ 模型加载器（方案 C）：自己解析 OBJ+MTL 生成 BakedQuad，
 * 让物品（含第一/第三人称手持）能渲染 Blender 导出的高模。
 *
 * <p>用法：</p>
 * <pre>
 * {
 *   "loader": "moonproject:obj_item",
 *   "model": "moonproject:models/obj/alaya_drawn.obj",
 *   "flip_v": true,
 *   "textures": {
 *     "BladeSteel": "moonproject:item/alaya_vijnana_drawn",
 *     "TsubaGold": "moonproject:item/alaya_tex_tsuba",
 *     "GoldFit":   "moonproject:item/alaya_tex_tsuba",
 *     "HiltMat":   "moonproject:item/alaya_tex_hilt",
 *     "SheathMat": "moonproject:item/alaya_tex_sheath"
 *   },
 *   "display": { ... }
 * }
 * </pre>
 */
public final class ObjItemModel implements IUnbakedGeometry<ObjItemModel> {

    private final ResourceLocation objLocation;
    private final boolean flipV;
    private final Map<String, ResourceLocation> textures;

    public ObjItemModel(ResourceLocation objLocation, boolean flipV, Map<String, ResourceLocation> textures) {
        this.objLocation = objLocation;
        this.flipV = flipV;
        this.textures = textures;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
        // 没有父模型
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState transform, ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<String, TextureAtlasSprite> sprites = new HashMap<>();
        for (Map.Entry<String, ResourceLocation> e : textures.entrySet()) {
            try {
                TextureAtlasSprite sprite = spriteGetter.apply(
                        new Material(TextureAtlas.LOCATION_BLOCKS, e.getValue()));
                if (sprite != null) {
                    sprites.put(e.getKey(), sprite);
                }
            } catch (Exception ignored) {
                // 贴图缺失就跳过该材质
            }
        }

        List<BakedQuad> quads = ObjParser.parse(objLocation, flipV, sprites);
        TextureAtlasSprite particle = quads.isEmpty() ? null : quads.get(0).getSprite();
        return new ItemObjBakedModel(quads, particle, context.getTransforms(), overrides);
    }

    /** 物品用 BakedModel：所有面都作为"无方向"面返回（物品渲染会全部绘制）。 */
    public static final class ItemObjBakedModel implements BakedModel {
        private final List<BakedQuad> quads;
        private final TextureAtlasSprite particle;
        private final ItemTransforms transforms;
        private final ItemOverrides overrides;

        ItemObjBakedModel(List<BakedQuad> quads, TextureAtlasSprite particle,
                          ItemTransforms transforms, ItemOverrides overrides) {
            this.quads = quads;
            this.particle = particle;
            this.transforms = transforms;
            this.overrides = overrides;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            return side == null ? quads : List.of();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return false;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particle;
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }

    /** 只处理 v / vt / f / usemtl 的极简 OBJ 解析器。 */
    public static final class ObjParser {
        private ObjParser() {
        }

        public static List<BakedQuad> parse(ResourceLocation obj, boolean flipV,
                                            Map<String, TextureAtlasSprite> sprites) {
            List<BakedQuad> out = new ArrayList<>();
            List<float[]> positions = new ArrayList<>();
            List<float[]> uvs = new ArrayList<>();
            String current = null;

            String path = "/assets/" + obj.getNamespace() + "/" + obj.getPath();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    ObjItemModel.class.getResourceAsStream(path), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("usemtl ")) {
                        current = line.substring(7).trim();
                    } else if (line.startsWith("v ")) {
                        String[] p = line.split("\\s+");
                        positions.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    } else if (line.startsWith("vt ")) {
                        String[] p = line.split("\\s+");
                        float u = Float.parseFloat(p[1]);
                        float v = Float.parseFloat(p[2]);
                        uvs.add(new float[]{u, flipV ? 1.0F - v : v});
                    } else if (line.startsWith("f ")) {
                        TextureAtlasSprite sprite = current == null ? null : sprites.get(current);
                        if (sprite == null) {
                            continue;
                        }
                        String[] parts = line.substring(2).trim().split("\\s+");
                        if (parts.length < 3) {
                            continue;
                        }
                        int n = parts.length;
                        float[][] pos = new float[n][];
                        float[][] uv = new float[n][];
                        for (int i = 0; i < n; i++) {
                            String[] idx = parts[i].split("/");
                            int vi = idx.length > 0 ? safeInt(idx[0], 1) - 1 : 0;
                            int ti = idx.length > 1 && !idx[1].isEmpty() ? safeInt(idx[1], 1) - 1 : -1;
                            pos[i] = positions.get(Math.max(0, Math.min(positions.size() - 1, vi)));
                            uv[i] = (ti >= 0 && ti < uvs.size()) ? uvs.get(ti) : new float[]{0.5F, 0.5F};
                        }
                        for (int i = 1; i + 1 < n; i++) {
                            out.add(tri(sprite, pos[0], pos[i], pos[i + 1], uv[0], uv[i], uv[i + 1]));
                        }
                    }
                }
            } catch (Exception ex) {
                return List.of();
            }
            return out;
        }

        private static int safeInt(String s, int fallback) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return fallback;
            }
        }

        /** 三角面 → BakedQuad（第 4 顶点复制第 3 个）。 */
        private static BakedQuad tri(TextureAtlasSprite sprite,
                                     float[] p0, float[] p1, float[] p2,
                                     float[] uv0, float[] uv1, float[] uv2) {
            int[] v = new int[32];
            put(v, 0, p0, uv0, sprite);
            put(v, 1, p1, uv1, sprite);
            put(v, 2, p2, uv2, sprite);
            put(v, 3, p2, uv2, sprite);
            return new BakedQuad(v, -1, Direction.NORTH, sprite, true);
        }

        private static void put(int[] v, int index, float[] pos, float[] uv, TextureAtlasSprite sprite) {
            int b = index * 8;
            float u = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * uv[0];
            float w = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * uv[1];
            v[b] = Float.floatToRawIntBits(pos[0]);
            v[b + 1] = Float.floatToRawIntBits(pos[1]);
            v[b + 2] = Float.floatToRawIntBits(pos[2]);
            v[b + 3] = 0xFFFFFFFF;
            v[b + 4] = Float.floatToRawIntBits(u);
            v[b + 5] = Float.floatToRawIntBits(w);
            v[b + 6] = 0;
            v[b + 7] = Float.floatToRawIntBits(1.0F);
        }
    }

    /** JSON 加载器，注册后用 {@code "loader": "moonproject:obj_item"}。 */
    public static final class Loader implements IGeometryLoader<ObjItemModel> {
        @Override
        public ObjItemModel read(JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
            ResourceLocation obj = new ResourceLocation(json.get("model").getAsString());
            boolean flipV = !json.has("flip_v") || json.get("flip_v").getAsBoolean();
            Map<String, ResourceLocation> textures = new HashMap<>();
            if (json.has("textures")) {
                for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("textures").entrySet()) {
                    textures.put(e.getKey(), new ResourceLocation(e.getValue().getAsString()));
                }
            }
            return new ObjItemModel(obj, flipV, textures);
        }
    }
}
