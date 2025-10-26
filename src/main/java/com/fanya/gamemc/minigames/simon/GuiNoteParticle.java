package com.fanya.gamemc.minigames.simon;

import net.minecraft.util.Identifier;

public class GuiNoteParticle {
    public static final Identifier NOTE_TEXTURE = Identifier.of("minecraft", "textures/particle/note.png");

    public float x, y;
    public float vy;
    public float alpha;
    public int lifetime;
    public int color;

    public GuiNoteParticle(float x, float y, int color) {
        this.x = x;
        this.y = y;
        this.vy = -0.8f - (float)(Math.random() * 0.4f);
        this.alpha = 1.0f;
        this.lifetime = 14 + (int)(Math.random() * 6);
        this.color = color;
    }

    public void update() {
        y += vy;
        lifetime--;
        alpha = Math.max(0f, lifetime / 15f);
    }

    public boolean isAlive() {
        return lifetime > 0;
    }
}
