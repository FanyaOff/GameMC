package com.fanya.gamemc.minigames.solitaire;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SolitaireGameScreen extends Screen{
    private final Screen parent;
    private SolitaireGame game;

    public SolitaireGameScreen(Screen parent) {
        super(Text.translatable("game.2048.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {}
}
