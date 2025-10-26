package com.fanya.gamemc;

import com.fanya.gamemc.screen.GameSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMC implements ClientModInitializer {
    public static final String MOD_ID = "gamemc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyBinding.Category GAME_MC_CATEGORY =
            KeyBinding.Category.create(Identifier.of(MOD_ID, "keys"));

    private static KeyBinding openScreenKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("GameMC initialized!");

        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gamemc.open_screen",      // ID кейбинда (локализуется через lang-файл)
                InputUtil.Type.KEYSYM,         // Тип ввода
                GLFW.GLFW_KEY_HOME,            // Клавиша HOME
                GAME_MC_CATEGORY               // Новая категория
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.wasPressed()) {
                if (client.player != null) {
                    client.execute(() -> client.setScreen(new GameSelectionScreen(client.currentScreen)));
                }
            }
        });
    }
}
