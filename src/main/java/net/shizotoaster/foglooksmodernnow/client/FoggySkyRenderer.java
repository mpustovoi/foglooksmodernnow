package net.shizotoaster.foglooksmodernnow.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class FoggySkyRenderer {
    public static void renderSky(ClientWorld level, float partialTick, MatrixStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (FogManager.shouldRenderCaveFog()) {
            FogManager densityManager = FogManager.getDensityManager();

            Vec3d fogColor = FogManager.getCaveFogColor();
            float darkness = densityManager.darkness.get(partialTick);
            float undergroundFactor = 1 - MathHelper.lerp(darkness, densityManager.getUndergroundFactor(partialTick), 1.0F);
            undergroundFactor *= undergroundFactor * undergroundFactor * undergroundFactor;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            Vec3d skyFogColor = densityManager.unlitFogColor;

            float radius = 5.0F;
            renderCone(poseStack, bufferbuilder, 32, true, radius, -30.0F,
                    (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor,
                    0.0F, (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor);
            renderCone(poseStack, bufferbuilder, 32, false, radius, 30.0F,
                    (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor * 0.2F,
                    0.0F, (float) (fogColor.x * skyFogColor.x), (float) (fogColor.y * skyFogColor.y), (float) (fogColor.z * skyFogColor.z), undergroundFactor);

            RenderSystem.depthMask(true);
        }
    }

    private static void renderCone(MatrixStack poseStack, BufferBuilder bufferBuilder, int resolution, boolean normal, float radius, float topVertexHeight, float topR, float topG, float topB, float topA, float bottomVertexHeight, float bottomR, float bottomG, float bottomB, float bottomA) {
        Matrix4f matrix = poseStack.peek().getPositionMatrix();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, 0.0F, topVertexHeight, 0.0F).color(topR, topG, topB, topA).next();
        for(int vertex = 0; vertex <= resolution; ++vertex) {
            float angle = (float)vertex * ((float)Math.PI * 2F) / ((float)resolution);
            float x = MathHelper.sin(angle) * radius;
            float z = MathHelper.cos(angle) * radius;

            bufferBuilder.vertex(matrix, x, bottomVertexHeight, normal ? z : -z).color(bottomR, bottomG, bottomB, bottomA).next();
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}
