package com.fanya.gamemc;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMC implements ClientModInitializer {
    public static final String MOD_ID = "gamemc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("GameMC initialized!");
    }
}
