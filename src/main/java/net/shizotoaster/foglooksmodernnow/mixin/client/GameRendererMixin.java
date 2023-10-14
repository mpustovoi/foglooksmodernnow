package net.shizotoaster.foglooksmodernnow.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.shizotoaster.foglooksmodernnow.client.FogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo info) {
        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.tick()));
    }
}