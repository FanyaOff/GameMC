package com.fanya.gamemc.minigames._2048;

import com.fanya.gamemc.data.GameRecords;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Game2048Screen extends Screen {
    private static final Identifier[] BLOCK_TEXTURES = buildTextures();
    private static Identifier[] buildTextures() {
        Identifier[] ids = new Identifier[12];
        ids[1] = Identifier.ofVanilla("textures/block/dirt.png");
        ids[2] = Identifier.ofVanilla("textures/block/stone.png");
        ids[3] = Identifier.ofVanilla("textures/block/iron_ore.png");
        ids[4] = Identifier.ofVanilla("textures/block/gold_ore.png");
        ids[5] = Identifier.ofVanilla("textures/block/emerald_ore.png");
        ids[6] = Identifier.ofVanilla("textures/block/diamond_ore.png");
        ids[7] = Identifier.ofVanilla("textures/block/redstone_ore.png");
        ids[8] = Identifier.ofVanilla("textures/block/lapis_ore.png");
        ids[9] = Identifier.ofVanilla("textures/block/ancient_debris.png");
        ids[10] = Identifier.ofVanilla("textures/block/end_stone.png");
        ids[11] = Identifier.ofVanilla("textures/block/beacon.png");
        return ids;
    }

    private final Screen parent;
    private Game2048 game;
    private int bestScore;

    private int playX, playY, cellSize, spacing, playWidth, playHeight;
    private int panelX;

    private int tickCounter = 0;
    private final int dropInterval = 50;

    public Game2048Screen(Screen parent) {
        super(Text.literal("2048 Blocks"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        game = new Game2048();
        bestScore = GameRecords.getInstance().getBestScore("2048_blocks");

        cellSize = 23;
        spacing = 3;

        playWidth = game.getCols() * (cellSize + spacing) - spacing;
        playHeight = game.getRows() * (cellSize + spacing) - spacing;

        int btnWidth = 100;
        int btnHeight = 20;
        int spacingBtn = 10;

        int totalBtnWidth = btnWidth * 3 + spacingBtn * 2;
        int startX = (this.width - totalBtnWidth) / 2;
        int btnY = 3;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.snake.info.back"), b -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(startX, btnY, btnWidth, btnHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.snake.info.new_game"), b -> {
            game.reset();
        }).dimensions(startX + btnWidth + spacingBtn, btnY, btnWidth, btnHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.snake.info.pause"), b -> {
            if (game != null) game.togglePause();
            b.setMessage(game.getState() == Game2048.State.PAUSED
                    ? Text.translatable("game.snake.info.resume")
                    : Text.translatable("game.snake.info.pause"));
        }).dimensions(startX + 2 * (btnWidth + spacingBtn), btnY, btnWidth, btnHeight).build());

        playX = startX;
        playY = (this.height - playHeight) / 2;

        panelX = playX + playWidth + 20;

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (game.getState() == Game2048.State.RUNNING) {
            tickCounter++;
            if (tickCounter >= dropInterval) {
                tickCounter = 0;
                game.dropStep();
            }
        }

        context.fillGradient(playX - 4, playY - 4, playX + playWidth + 4, playY + playHeight + 4, 0xFF333333, 0xFF111111);

        int[][] board = game.getBoard();
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                int x = playX + c * (cellSize + spacing);
                int y = playY + r * (cellSize + spacing);
                context.fill(x, y, x + cellSize, y + cellSize, 0xFF0A1A1F);
                int lvl = board[r][c];
                if (lvl != 0) drawBlock(context, x, y, lvl);
            }
        }

        if (game.getState() == Game2048.State.RUNNING) {
            int x = playX + game.getCurrentX() * (cellSize + spacing);
            int y = playY + game.getCurrentY() * (cellSize + spacing);
            drawBlock(context, x, y, game.getCurrentLevel());
        }

        drawPanel(context);

        if (game.getState() == Game2048.State.GAMEOVER) {
            context.fill(playX, playY, playX + playWidth, playY + playHeight, 0xAAFF0000);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.snake.info.game_over"),
                    playX + playWidth / 2, playY + playHeight / 2 - 10, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.snake.info.score", game.getScore()),
                    playX + playWidth / 2, playY + playHeight / 2 + 10, 0xFFFFFF00);
        }

        if (game.getState() == Game2048.State.PAUSED) {
            context.fill(playX, playY, playX + playWidth, playY + playHeight, 0xAA000000); // чёрное полупрозрачное
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.snake.info.paused"),
                    playX + playWidth / 2, playY + playHeight / 2, 0xFFFFFFFF);
        }

        if (game.getState() == Game2048.State.VICTORY) {
            context.fill(playX, playY, playX + playWidth, playY + playHeight, 0xAA00FF00); // зеленое затемнение
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.snake.info.victory"),
                    playX + playWidth / 2, playY + playHeight / 2 - 10, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.snake.info.score", game.getScore()),
                    playX + playWidth / 2, playY + playHeight / 2 + 10, 0xFFFFFF00);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBlock(DrawContext context, int x, int y, int lvl) {
        Identifier tex = BLOCK_TEXTURES[Math.min(lvl, 11)];
        GpuTextureView view = MinecraftClient.getInstance().getTextureManager().getTexture(tex).getGlTextureView();
        RenderSystem.setShaderTexture(0, view);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, cellSize, cellSize, cellSize, cellSize);
        int val = 1 << lvl;
        String s = String.valueOf(val);
        int w = textRenderer.getWidth(s);
        context.drawText(textRenderer, Text.literal(s),
                x + (cellSize - w) / 2, y + (cellSize - 8) / 2, 0xFFFFFFFF, true);
    }

    private void drawPanel(DrawContext context) {
        int panelTop = playY - 6;
        int panelBottom = playY + playHeight + 6;

        context.fill(panelX - 10, panelTop, panelX + 190, panelBottom, 0x88000000);

        int y = playY;

        context.drawText(textRenderer, Text.translatable("game.snake.info.title"), panelX, y, 0xFF00FFFF, false);
        y += 20;

        context.drawText(textRenderer, Text.translatable("game.snake.info.score", game.getScore()), panelX, y, 0xFFFFFFFF, false);
        y += 16;

        int best = GameRecords.getInstance().getBestScore("2048_blocks");
        context.drawText(textRenderer, Text.translatable("game.snake.info.best", best), panelX, y, 0xFFFFFFFF, false);
        y += 30;

        context.drawText(textRenderer, Text.translatable("game.snake.info.controls"), panelX, y, 0xFFAAAAAA, false);
        y += 14;

        context.drawText(textRenderer, Text.translatable("game.snake.info.left"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.snake.info.right"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.snake.info.down"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.snake.info.drop"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.snake.info.restart"), panelX, y, 0xFFCCCCCC, false);
    }

    private void drawGameOver(DrawContext ctx) {
        int bx = playX + playWidth / 2 - 100;
        int by = playY + playHeight / 2 - 40;
        ctx.fillGradient(bx, by, bx + 200, by + 80, 0xD0AA0000, 0xD0550000);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("GAME OVER"), bx + 100, by + 16, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + game.getScore()), bx + 100, by + 36, 0xFFFFFF00);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("R - Restart"), bx + 100, by + 56, 0xFFCCCCCC);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (game.getState() == Game2048.State.RUNNING || game.getState() == Game2048.State.PAUSED) {
            switch (input.getKeycode()) {
                case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> game.move(-1);
                case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> game.move(1);
                case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> game.dropStep();
                case GLFW.GLFW_KEY_SPACE -> game.hardDrop();
                case GLFW.GLFW_KEY_R -> game.reset();
                case GLFW.GLFW_KEY_P -> game.togglePause();
                case GLFW.GLFW_KEY_ESCAPE -> {
                    if (client != null) client.setScreen(parent);
                }
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
