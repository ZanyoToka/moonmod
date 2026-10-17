package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.client.obj.ObjItemModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 注册自定义 OBJ 物品模型加载器：{@code "loader": "moonproject:obj_item"}。 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ObjModelRegistration {
    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        // 注意：这个 register 的第一个参数是 String（不是 ResourceLocation）
        // 参数只需路径，modid 会自动补上（写成 "moonproject:obj_item" 会变成 moonproject:moonproject:obj_item）
        event.register("obj_item", new ObjItemModel.Loader());
    }

    private ObjModelRegistration() {
    }
}