package com.madworld;

import com.madworld.block.ModBlocks;
import com.madworld.config.MadConfig;
import com.madworld.entity.ModEntities;
import com.madworld.item.ModItems;
import com.madworld.net.ModNetwork;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MadWorldMod implements ModInitializer {
    public static final String MOD_ID = "madworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        MadConfig.load();
        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        ModNetwork.init();
        MadHooks.init();
        MadCommands.init();
        LOGGER.info("МИР СОШЁЛ С УМА! загружен. Удачи.");
    }
}
