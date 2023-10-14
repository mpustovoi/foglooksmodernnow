package net.shizotoaster.foglooksmodernnow;

import com.mojang.logging.LogUtils;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shizotoaster.foglooksmodernnow.client.FogManager;
import net.shizotoaster.foglooksmodernnow.config.FogLooksGoodNowConfig;
import org.slf4j.Logger;

@Mod(FogLooksModernNow.MODID)
public class FogLooksModernNow {
    public static final String MODID = "foglooksmodernnow";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FogLooksModernNow() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FogLooksGoodNowConfig.config, MODID + ".toml");
    }

    public void onConfigLoad(ModConfigEvent.Loading event) {
        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig()));
    }

    public void onConfigReload(ModConfigEvent.Reloading event) {
        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig()));
    }
}
