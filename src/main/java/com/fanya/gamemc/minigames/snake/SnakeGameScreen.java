package com.fanya.gamemc.minigames.snake;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;
import com.fanya.gamemc.data.GameRecords;

public class SnakeGameScreen extends Screen {
    private int bestScore;

    private static final Identifier SLIME = Identifier.ofVanilla("textures/block/slime_block.png");
    private static final Identifier EMERALD = Identifier.ofVanilla("textures/block/emerald_block.png");

    private final Screen parent;
    private SnakeGame game;
    private static final int GRID_WIDTH = 25;
    private static final int GRID_HEIGHT = 18;

    private static final int MIN_CELL_SIZE = 8;
    private static final int MAX_CELL_SIZE = 28;

    private int cellSize;
    private int gridOffsetX;
    private int gridOffsetY;

    public SnakeGameScreen(Screen parent) {
        super(Text.translatable("game.snake.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        bestScore = GameRecords.getInstance().getBestScore("snake");
        calculateGridSize();

        if (game == null) {
            game = new SnakeGame(GRID_WIDTH, GRID_HEIGHT);
        }

        int buttonY = Math.max(this.height - 25, gridOffsetY + GRID_HEIGHT * cellSize + 8);
        int buttonWidth = Math.min(85, this.width / 6);
        int buttonHeight = 16;
        int spacing = 8;

        this.addDrawableChild(ButtonWidget.builder(
                        ScreenTexts.BACK,
                        button -> {
                            if (this.client != null) {
                                this.client.setScreen(this.parent);
                            }
                        }
                )
                .dimensions(this.width / 2 - buttonWidth - spacing / 2, buttonY, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("game.snake.button.newgame"),
                button -> game.reset()
        ).dimensions(this.width / 2 + spacing / 2, buttonY, buttonWidth, buttonHeight).build());
    }

    private void calculateGridSize() {
        int reservedWidth = 160;
        int reservedHeight = 200;

        int availableWidth = Math.max(this.width - reservedWidth, 200);
        int availableHeight = Math.max(this.height - reservedHeight, 150);

        int cellWidthBased = availableWidth / GRID_WIDTH;
        int cellHeightBased = availableHeight / GRID_HEIGHT;

        cellSize = Math.min(cellWidthBased, cellHeightBased);
        cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, cellSize));

        int totalGridWidth = GRID_WIDTH * cellSize;
        int totalGridHeight = GRID_HEIGHT * cellSize;

        gridOffsetX = (this.width - totalGridWidth) / 2;
        gridOffsetY = (this.height - totalGridHeight) / 2;

        if (gridOffsetX < 15) gridOffsetX = 15;
        if (gridOffsetY < 40) gridOffsetY = 40;

        int maxGridBottom = this.height - 35;
        if (gridOffsetY + totalGridHeight > maxGridBottom) {
            gridOffsetY = Math.max(40, maxGridBottom - totalGridHeight);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderPanoramaBackground(context, delta);
        context.fillGradient(0, 0, this.width, this.height,
                0xB0000000, 0xC0000000);

        int borderSize = Math.max(2, cellSize / 8);
        context.fillGradient(
                gridOffsetX - borderSize,
                gridOffsetY - borderSize,
                gridOffsetX + GRID_WIDTH * cellSize + borderSize,
                gridOffsetY + GRID_HEIGHT * cellSize + borderSize,
                0xFF1a8c99, 0xFF0d5d66
        );

        context.fill(gridOffsetX, gridOffsetY,
                gridOffsetX + GRID_WIDTH * cellSize,
                gridOffsetY + GRID_HEIGHT * cellSize,
                0xFF0a1a1f);

        drawGrid(context);

        if (!game.isGameOver()) {
            game.update();
        }

        drawGame(context);

        drawInfoPanel(context);

        if (game.isGameOver()) {
            if (game.getScore() > bestScore) {
                bestScore = game.getScore();
                GameRecords.getInstance().setBestScore("snake", bestScore);
            }
            drawGameOverScreen(context);
        }

        this.children().forEach(child -> {
            if (child instanceof ButtonWidget button) {
                button.render(context, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    private void drawGrid(DrawContext context) {
        int gridColor = 0x20FFFFFF;

        for (int x = 0; x <= GRID_WIDTH; x++) {
            context.fill(
                    gridOffsetX + x * cellSize,
                    gridOffsetY,
                    gridOffsetX + x * cellSize + 1,
                    gridOffsetY + GRID_HEIGHT * cellSize,
                    gridColor
            );
        }

        for (int y = 0; y <= GRID_HEIGHT; y++) {
            context.fill(
                    gridOffsetX,
                    gridOffsetY + y * cellSize,
                    gridOffsetX + GRID_WIDTH * cellSize,
                    gridOffsetY + y * cellSize + 1,
                    gridColor
            );
        }
    }

    private void drawGame(DrawContext context) {
        int padding = Math.max(1, cellSize / 10);

        SnakeGame.Position food = game.getFood();
        Identifier foodTexture = game.getCurrentFoodTexture();

        GpuTextureView foodGpuTexture = MinecraftClient.getInstance().getTextureManager().getTexture(foodTexture).getGlTextureView();

        int foodSize = Math.max(1, cellSize - padding * 2);
        if (foodSize > 2) {
            RenderSystem.setShaderTexture(0, foodGpuTexture);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, foodTexture,
                    gridOffsetX + food.x * cellSize + padding,
                    gridOffsetY + food.y * cellSize + padding,
                    0, 0,
                    foodSize, foodSize,
                    foodSize, foodSize);
        }

        for (int i = 0; i < game.getSnake().size(); i++) {
            SnakeGame.Position segment = game.getSnake().get(i);

            Identifier texture = (i == 0) ? EMERALD : SLIME;
            GpuTextureView snakeGpuTexture = MinecraftClient.getInstance().getTextureManager().getTexture(texture).getGlTextureView();
            int segmentPadding = Math.max(1, cellSize / 12);
            int segmentSize = Math.max(1, cellSize - segmentPadding * 2);

            if (segmentSize > 2) {
                RenderSystem.setShaderTexture(0, snakeGpuTexture);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, texture,
                        gridOffsetX + segment.x * cellSize + segmentPadding,
                        gridOffsetY + segment.y * cellSize + segmentPadding,
                        0, 0,
                        segmentSize, segmentSize,
                        segmentSize, segmentSize);
            }
        }
    }

    private void drawInfoPanel(DrawContext context) {
        int panelHeight = Math.max(24, Math.min(32, cellSize + 8));
        int panelY = Math.max(5, gridOffsetY - panelHeight - 4);

        int panelWidth = GRID_WIDTH * cellSize;

        context.fillGradient(
                gridOffsetX - 3, panelY,
                gridOffsetX + panelWidth + 3, panelY + panelHeight,
                0xD0101010, 0xE0202020
        );

        context.fill(
                gridOffsetX - 3, panelY,
                gridOffsetX + panelWidth + 3, panelY + 2,
                0xFF1a8c99
        );

        int textSize = this.textRenderer.fontHeight;
        int textY1 = panelY + 4;
        int textY2 = panelY + panelHeight - textSize - 2;

        String scoreText = Text.translatable("game.snake.score", game.getScore()).getString();
        context.drawTextWithShadow(this.textRenderer, scoreText,
                gridOffsetX + 5, textY1, 0xFFFFD700);

        String lengthText = Text.translatable("game.snake.length", game.getSnake().size()).getString();
        context.drawTextWithShadow(this.textRenderer, lengthText,
                gridOffsetX + 5, textY2, 0xFF00FF00);

        String bestText = Text.translatable("game.snake.best_score", bestScore).getString();
        float scale = Math.max(1.0f, cellSize / 18f);

        int textWidth = (int)(this.textRenderer.getWidth(bestText) * scale);
        int textHeight = (int)(this.textRenderer.fontHeight * scale);

        float x = gridOffsetX + panelWidth / 2f - textWidth / 2f;
        float y = panelY + panelHeight / 2f - textHeight / 2f;

        context.drawText(
                this.textRenderer,
                Text.literal(bestText),
                (int)x,
                (int)y,
                0xFF87CEEB,
                true
        );

        String controls = "WASD | R";
        int controlsWidth = this.textRenderer.getWidth(controls);
        if (controlsWidth < panelWidth - 60) {
            context.drawTextWithShadow(this.textRenderer, controls,
                    gridOffsetX + panelWidth - controlsWidth,
                    panelY + panelHeight / 2 - textSize / 2, 0xFFAAAAAA);
        }
    }

    private void drawGameOverScreen(DrawContext context) {
        context.fillGradient(gridOffsetX, gridOffsetY,
                gridOffsetX + GRID_WIDTH * cellSize,
                gridOffsetY + GRID_HEIGHT * cellSize,
                0xD0AA0000, 0xD0550000);

        int centerX = gridOffsetX + (GRID_WIDTH * cellSize) / 2;
        int centerY = gridOffsetY + (GRID_HEIGHT * cellSize) / 2;

        int lineHeight = this.textRenderer.fontHeight + 4;

        Text gameOverText = Text.translatable("game.snake.lose");
        context.drawCenteredTextWithShadow(this.textRenderer, gameOverText,
                centerX, centerY - lineHeight * 2, 0xFFFFFFFF);

        Text scoreText = Text.translatable("game.snake.score", game.getScore());
        context.drawCenteredTextWithShadow(this.textRenderer, scoreText,
                centerX, centerY - lineHeight / 2, 0xFFFFFF00);

        Text lengthText = Text.translatable("game.snake.length", game.getSnake().size());
        context.drawCenteredTextWithShadow(this.textRenderer, lengthText,
                centerX, centerY + lineHeight / 2, 0xFF00FF00);

        Text bestScoreText = Text.translatable("game.snake.best_score", bestScore);
        context.drawCenteredTextWithShadow(this.textRenderer, bestScoreText,
                centerX, centerY + lineHeight * 3, 0xFF87CEEB);

        Text restartText = Text.translatable("game.snake.restart_hint");
        context.drawCenteredTextWithShadow(this.textRenderer, restartText,
                centerX, centerY + lineHeight * 2, 0xFFCCCCCC);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        switch (keyInput.key()) {
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> {
                game.setDirection(Direction.NORTH);
                return true;
            }
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> {
                game.setDirection(Direction.SOUTH);
                return true;
            }
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> {
                game.setDirection(Direction.WEST);
                return true;
            }
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> {
                game.setDirection(Direction.EAST);
                return true;
            }
            case GLFW.GLFW_KEY_R -> {
                game.reset();
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (this.client != null) {
                    this.client.setScreen(this.parent);
                }
                return true;
            }
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public void resize(net.minecraft.client.MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
