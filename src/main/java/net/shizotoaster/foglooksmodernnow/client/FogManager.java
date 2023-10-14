package net.shizotoaster.foglooksmodernnow.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.shizotoaster.foglooksmodernnow.FogLooksModernNow;
import net.shizotoaster.foglooksmodernnow.config.FogLooksGoodNowConfig;
import net.shizotoaster.foglooksmodernnow.util.MathUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FogManager {
    @Nullable
    public static FogManager densityManager;
    public static FogManager getDensityManager() {
        return Objects.requireNonNull(densityManager, "Attempted to call getDensityManager before it finished loading!");
    }
    public static Optional<FogManager> getDensityManagerOptional() {
        return Optional.ofNullable(densityManager);
    }

    private final MinecraftClient mc;
    public InterpolatedValue fogStart;
    public InterpolatedValue fogEnd;
    public InterpolatedValue currentSkyLight;
    public InterpolatedValue currentBlockLight;
    public InterpolatedValue currentLight;
    public InterpolatedValue undergroundness;
    public InterpolatedValue darkness;
    public InterpolatedValue[] caveFogColors;

    public Vec3d unlitFogColor = Vec3d.ZERO;

    private Map<String, BiomeFogDensity> configMap;

    public boolean useCaveFog = true;
    public double caveFogMultiplier = 1.0;

    public FogManager() {
        this.mc = MinecraftClient.getInstance();
        this.fogStart = new InterpolatedValue(0.0F);
        this.fogEnd = new InterpolatedValue(1.0F);

        this.currentSkyLight = new InterpolatedValue(16.0F);
        this.currentBlockLight = new InterpolatedValue(16.0F);
        this.currentLight = new InterpolatedValue(16.0F);
        this.undergroundness = new InterpolatedValue(0.0F, 0.02f);
        this.darkness = new InterpolatedValue(0.0F, 0.1f);
        this.caveFogColors = new InterpolatedValue[3];
        this.caveFogColors[0] =  new InterpolatedValue(1.0F);
        this.caveFogColors[1] =  new InterpolatedValue(1.0F);
        this.caveFogColors[2] =  new InterpolatedValue(1.0F);

        this.configMap = new HashMap<>();
        if (FogLooksGoodNowConfig.config.isLoaded()) {
            initializeConfig();
        }
    }

    public void initializeConfig() {
        FogLooksModernNow.LOGGER.info("Initialized Config Values");
        this.fogStart.setDefaultValue(FogLooksGoodNowConfig.CLIENT_CONFIG.defaultFogStart.get());
        this.fogEnd.setDefaultValue(FogLooksGoodNowConfig.CLIENT_CONFIG.defaultFogDensity.get());
        this.useCaveFog = FogLooksGoodNowConfig.CLIENT_CONFIG.useCaveFog.get();
        this.caveFogMultiplier = FogLooksGoodNowConfig.CLIENT_CONFIG.caveFogDensity.get();
        this.configMap = new HashMap<>();

        Vec3d caveFogColor = Vec3d.unpackRgb(FogLooksGoodNowConfig.CLIENT_CONFIG.caveFogColor.get());
        this.caveFogColors[0].setDefaultValue(caveFogColor.x);
        this.caveFogColors[1].setDefaultValue(caveFogColor.y);
        this.caveFogColors[2].setDefaultValue(caveFogColor.z);

        List<Pair<String, BiomeFogDensity>> densityConfigs = FogLooksGoodNowConfig.getDensityConfigs();
        for (Pair<String, BiomeFogDensity> densityConfig : densityConfigs) {
            this.configMap.put(densityConfig.getLeft(), densityConfig.getRight());
        }
    }

    public void tick() {
        BlockPos pos = this.mc.gameRenderer.getCamera().getBlockPos();
        Biome biome = this.mc.world.getBiome(pos).value();
        Identifier key = this.mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
        if (key == null)
            return;

        BiomeFogDensity currentDensity = configMap.get(key.toString());
        boolean isFogDense = this.mc.world.getDimensionEffects().useThickFog(pos.getX(), pos.getZ()) || this.mc.inGameHud.getBossBarHud().shouldThickenFog();
        float density = isFogDense? 0.9F : 1.0F;

        ClientWorld pLevel = MinecraftClient.getInstance().world;
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        BiomeAccess biomemanager = pLevel.getBiomeAccess();
        Vec3d playerPos = camera.getPos().subtract(2.0D, 2.0D, 2.0D).multiply(0.25D);
        this.unlitFogColor = CubicSampler.sampleColor(playerPos, (p_109033_, p_109034_, p_109035_) -> pLevel.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(biomemanager.getBiomeForNoiseGen(p_109033_, p_109034_, p_109035_).value().getFogColor()), 1));

        float[] darknessAffectedFog;

        if (currentDensity != null) {
            darknessAffectedFog = getDarknessEffectedFog(currentDensity.fogStart(), currentDensity.fogDensity() * density);
            Vec3d caveFogColor = Vec3d.unpackRgb(currentDensity.caveFogColor);
            this.caveFogColors[0].interpolate(caveFogColor.x);
            this.caveFogColors[1].interpolate(caveFogColor.y);
            this.caveFogColors[2].interpolate(caveFogColor.z);
        } else {
            darknessAffectedFog = getDarknessEffectedFog(this.fogStart.defaultValue, this.fogEnd.defaultValue * density);
            this.caveFogColors[0].interpolate();
            this.caveFogColors[1].interpolate();
            this.caveFogColors[2].interpolate();
        }

        this.darkness.interpolate(darknessAffectedFog[2]);
        this.fogStart.interpolate(darknessAffectedFog[0]);
        this.fogEnd.interpolate(darknessAffectedFog[1]);

        this.currentSkyLight.interpolate(Math.max(mc.world.getLightLevel(LightType.SKY, pos), mc.world.getLightLevel(LightType.SKY, pos.up())));
        this.currentBlockLight.interpolate(mc.world.getLightLevel(LightType.BLOCK, pos));
        this.currentLight.interpolate(mc.world.getBaseLightLevel(pos, 0));

        boolean isAboveGround =  pos.getY() > mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (isAboveGround) { this.undergroundness.interpolate(0.0F, 0.05f); } else { this.undergroundness.interpolate(1.0F); }
    }

    public float getUndergroundFactor(float partialTick) {
        float y = (float) mc.cameraEntity.getY();
        float yFactor = MathHelper.clamp(MathUtils.mapRange(mc.world.getSeaLevel() - 32.0F, mc.world.getSeaLevel() + 32.0F, 1, 0, y), 0.0F, 1.0F);
        return MathHelper.lerp(yFactor, 1 - this.undergroundness.get(partialTick), this.currentSkyLight.get(partialTick) / 16.0F);
    }

    public static Vec3d getCaveFogColor() {
        MinecraftClient mc = MinecraftClient.getInstance();

        InterpolatedValue[] cfc = densityManager.caveFogColors;
        return new Vec3d(cfc[0].get(mc.getPartialTick()), cfc[1].get(mc.getPartialTick()), cfc[2].get(mc.getPartialTick()));
    }

    public static boolean shouldRenderCaveFog() {
        return MinecraftClient.getInstance().world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.NORMAL && densityManager.useCaveFog && MinecraftClient.getInstance().gameRenderer.getCamera().getSubmersionType() == CameraSubmersionType.NONE;
    }

    public float[] getDarknessEffectedFog(float fs, float fd) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float renderDistance = mc.gameRenderer.getViewDistance() * 16;

        Entity entity = mc.cameraEntity;
        float fogStart = fs;
        float fogEnd = fd;
        float darknessValue = 0.0F;
        this.fogEnd.interpolationSpeed = 0.05f;
        this.fogStart.interpolationSpeed = 0.05f;
        if (entity instanceof LivingEntity e) {
            if (e.hasStatusEffect(StatusEffects.BLINDNESS)) {
                fogStart = (4 * 16) / renderDistance;
                fogEnd = (8 * 16) / renderDistance;
                darknessValue = 1.0F;
            } else if (e.hasStatusEffect(StatusEffects.DARKNESS)) {
                StatusEffectInstance effect = e.getStatusEffect(StatusEffects.DARKNESS);
                if (!effect.getFactorCalculationData().isEmpty()) {

                    /*float factor = this.mc.options.getDarknessEffectScale().get().floatValue();
                    float intensity = effect.getFactorCalculationData().get().getFactor(e, mc.getPartialTick()) * factor;
                    float darkness = 1 - (calculateDarknessScale(e, effect.getFactorData().get().getFactor(e, mc.getPartialTick()), mc.getPartialTick()));*/
                    float factor = this.mc.options.getDarknessEffectScale().getValue().floatValue();
                    float intensity = effect.getFactorCalculationData().get().lerp(e, mc.getPartialTick()) * factor;
                    float darkness = 1 - (calculateDarknessScale(e, effect.getFactorCalculationData().get().lerp(e, mc.getPartialTick()), mc.getPartialTick()));

                    FogLooksModernNow.LOGGER.info("" + intensity);
                    fogStart = ((8.0F * 16) / renderDistance) * darkness;
                    fogEnd = ((15.0F * 16) / renderDistance);
                    darknessValue = effect.getFactorCalculationData().get().lerp(e, mc.getPartialTick());
                }
            }
        }

        return new float[]{fogStart, fogEnd, darknessValue};
    }

    private float calculateDarknessScale(LivingEntity pEntity, float darknessFactor, float partialTicks) {
        float factor = this.mc.options.getDarknessEffectScale().getValue().floatValue();
        float f = 0.45F * darknessFactor;
        return Math.max(0.0F, MathHelper.cos(((float)pEntity.age - partialTicks) * (float)Math.PI * 0.025F) * f) * factor;
    }


    public void close() {}

    public record BiomeFogDensity(float fogStart, float fogDensity, int caveFogColor) {};

    public class InterpolatedValue {
        public float defaultValue;

        private float interpolationSpeed;
        private float previousValue;
        private float currentValue;

        public InterpolatedValue(float defaultValue, float interpolationSpeed) {
            this.defaultValue = defaultValue;
            this.currentValue = defaultValue;
            this.interpolationSpeed = interpolationSpeed;
        }

        public InterpolatedValue(float defaultValue) {
            this(defaultValue, 0.05f);
        }

        public void set(float value) {
            this.previousValue = this.currentValue;
            this.currentValue = value;
        }
        public void set(double value) {
            this.previousValue = this.currentValue;
            this.currentValue = (float) value;
        }

        public void setDefaultValue(float value) {
            this.defaultValue = value;
        }
        public void setDefaultValue(double value) {
            this.defaultValue = (float)value;
        }

        public void interpolate(float value, float interpolationSpeed) {
            this.set(Float.isNaN(value) ? MathHelper.lerp(interpolationSpeed, currentValue, defaultValue) : MathHelper.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(double value, float interpolationSpeed) {
            this.set(Double.isNaN(value) ? MathHelper.lerp(interpolationSpeed, currentValue, defaultValue) : MathHelper.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(float value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate(double value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate() {
            this.set(MathHelper.lerp(interpolationSpeed, currentValue, defaultValue));
        }

        public float get(float partialTick) {
            return MathHelper.lerp(partialTick, previousValue, currentValue);
        }
    }

}