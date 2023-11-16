package net.shizotoaster.foglooksmodernnow.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shizotoaster.foglooksmodernnow.FogLooksModernNow;
import net.shizotoaster.foglooksmodernnow.util.MathUtils;

@Mod.EventBusSubscriber(modid = FogLooksModernNow.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
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

            event.setNearPlaneDistance(renderDistance * densityManager.fogStart.get((float) event.getPartialTick()));
            event.setFarPlaneDistance(renderDistance * densityManager.fogEnd.get((float) event.getPartialTick()) * undergroundFogMultiplier);
            event.setFogShape(FogShape.SPHERE);

            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderFogColors(ViewportEvent.ComputeFogColor event) {
        if (FogManager.shouldRenderCaveFog()) {
            FogManager densityManager = FogManager.getDensityManager();

            Vec3d fogColor = FogManager.getCaveFogColor();

            float undergroundFactor = 1 - densityManager.getUndergroundFactor((float) event.getPartialTick());
            event.setRed((float) MathUtils.lerp(undergroundFactor, event.getRed(), fogColor.x * densityManager.unlitFogColor.getX()));
            event.setGreen((float) MathUtils.lerp(undergroundFactor, event.getGreen(), fogColor.y * densityManager.unlitFogColor.getY()));
            event.setBlue((float) MathUtils.lerp(undergroundFactor, event.getBlue(), fogColor.z * densityManager.unlitFogColor.getZ()));
        }
    }
}
