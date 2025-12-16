package com.fanya.gamemc.util;

import com.fanya.gamemc.GameMC;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomSounds {
    public static final SoundEvent PUT_GAME_CARD = registerSound("put_game_card");
    public static final SoundEvent GET_GAME_CARD = registerSound("get_game_card");
    public static final SoundEvent SWAP_GAME_CARD = registerSound("swap_game_card");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(GameMC.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
    public static void initialize() {
        GameMC.LOGGER.info("Registering " + GameMC.MOD_ID + " Sounds");
    }
}