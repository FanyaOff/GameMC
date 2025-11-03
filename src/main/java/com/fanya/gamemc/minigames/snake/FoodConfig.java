package com.fanya.gamemc.minigames.snake;

import net.minecraft.util.Identifier;

/**
 * Конфигурация одного вида еды: текстура, очки, вес (шанс появления).
 */
public class FoodConfig {
    private final Identifier texture;
    private final int points;
    private final int weight; // относительный вес (чем больше - тем чаще)

    public FoodConfig(Identifier texture, int points, int weight) {
        this.texture = texture;
        this.points = points;
        this.weight = Math.max(0, weight);
    }

    public Identifier getTexture() {
        return texture;
    }

    public int getPoints() {
        return points;
    }

    public int getWeight() {
        return weight;
    }
}
