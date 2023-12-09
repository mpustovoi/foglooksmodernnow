package net.shizotoaster.foglooksmodernnow;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
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

        NeoForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FogLooksGoodNowConfig.config, MODID + ".toml");
    }

    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent.Loading event) {
        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig()));
    }

    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading event) {
        FogManager.getDensityManagerOptional().ifPresent((fogDensityManager -> fogDensityManager.initializeConfig()));
    }
}
