package com.fanya.gamemc.minigames.snake;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SnakeSizeSelectScreen extends Screen {
    private final Screen parent;

    public SnakeSizeSelectScreen(Screen parent) {
        super(Text.translatable("game.snake.size_select.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 160;
        int buttonHeight = 20;
        int spacing = 8;

        int startY = this.height / 2 - (buttonHeight * 3 + spacing * 2) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.snake.button.small"),
                        button -> openSnakeGame(15, 10))
                .dimensions(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.snake.button.medium"),
                        button -> openSnakeGame(25, 18))
                .dimensions(this.width / 2 - buttonWidth / 2, startY + (buttonHeight + spacing), buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.snake.button.big"),
                        button -> openSnakeGame(35, 25))
                .dimensions(this.width / 2 - buttonWidth / 2, startY + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"),
                        button -> this.client.setScreen(parent))
                .dimensions(this.width / 2 - buttonWidth / 2, startY + (buttonHeight + spacing) * 3 + 10, buttonWidth, buttonHeight)
                .build());
    }

    private void openSnakeGame(int width, int height) {
        SnakeGameScreen.selectedGridWidth = width;
        SnakeGameScreen.selectedGridHeight = height;
        this.client.setScreen(new SnakeGameScreen(parent, width, height));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xA0000000, 0xC0000000);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("game.snake.size_select.title"),
                this.width / 2,
                this.height / 2 - 60,
                0xFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }
}
