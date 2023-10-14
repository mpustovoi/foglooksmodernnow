package net.shizotoaster.foglooksmodernnow.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.shizotoaster.foglooksmodernnow.FogLooksModernNow;
import net.shizotoaster.foglooksmodernnow.client.FogManager;
import net.shizotoaster.foglooksmodernnow.client.FoggySkyRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo info) {
        FogManager.densityManager = new FogManager();

        FogLooksModernNow.LOGGER.info("Initialized Density Manager");
    }

    @Inject(method = "close()V", at = @At("TAIL"))
    private void close(CallbackInfo info) {
        FogManager.getDensityManager().close();
        FogManager.densityManager = null;
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("TAIL"))
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        FoggySkyRenderer.renderSky(mc.world, tickDelta, matrices, camera, projectionMatrix, thickFog, fogCallback);
    }
}
