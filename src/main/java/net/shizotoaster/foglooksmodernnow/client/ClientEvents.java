package net.shizotoaster.foglooksmodernnow.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shizotoaster.foglooksmodernnow.FogLooksModernNow;
import net.shizotoaster.foglooksmodernnow.util.MathUtils;

@Mod.EventBusSubscriber(modid = FogLooksModernNow.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getCamera().getSubmersionType() == CameraSubmersionType.NONE) {
            FogManager densityManager = FogManager.getDensityManager();
            float renderDistance = event.getRenderer().getViewDistance();

            float undergroundFogMultiplier = 1.0F;
            if (FogManager.shouldRenderCaveFog()) {
                undergroundFogMultiplier = (float)  MathHelper.lerp(densityManager.getUndergroundFactor((float) event.getPartialTick()), densityManager.caveFogMultiplier, 1.0F);
                float darkness = densityManager.darkness.get((float) event.getPartialTick());
                undergroundFogMultiplier = MathHelper.lerp(darkness, undergroundFogMultiplier, 1.0F);
            }

            RenderSystem.setShaderFogStart(renderDistance * densityManager.fogStart.get((float) event.getPartialTick()));
            RenderSystem.setShaderFogEnd(renderDistance * densityManager.fogEnd.get((float) event.getPartialTick()) * undergroundFogMultiplier);
            RenderSystem.setShaderFogShape(FogShape.SPHERE);
        }
    }

    @SubscribeEvent
    public static void onRenderFogColors(ViewportEvent.ComputeFogColor event) {
        if (FogManager.shouldRenderCaveFog()) {
            FogManager densityManager = FogManager.getDensityManager();

            Vec3d fogColor = FogManager.getCaveFogColor();

            float undergroundFactor = 1 - densityManager.getUndergroundFactor((float) event.getPartialTick());
            event.setRed((float) MathUtils.lerp(undergroundFactor, event.getRed(), fogColor.x * densityManager.unlitFogColor.getX()));
            event.setGreen((float) MathUtils.lerp(undergroundFactor, event.getGreen(), fogColor.y * densityManager.unlitFogColor.getY()));
            event.setBlue((float) MathUtils.lerp(undergroundFactor, event.getBlue(), fogColor.z * densityManager.unlitFogColor.getZ()));

            //event.setRed((float) (fogColor.x * densityManager.unlitFogColor.x()));
            //event.setGreen((float) (fogColor.y * densityManager.unlitFogColor.y()));
            //event.setBlue((float) (fogColor.z * densityManager.unlitFogColor.z()));
        }
    }
}
