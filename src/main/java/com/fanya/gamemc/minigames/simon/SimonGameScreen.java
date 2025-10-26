package com.fanya.gamemc.minigames.simon;

import com.fanya.gamemc.data.GameRecords;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.fanya.gamemc.minigames.simon.GuiNoteParticle.NOTE_TEXTURE;

public class SimonGameScreen extends Screen {

    private static final Identifier NOTE_BLOCK_TEXTURE = Identifier.ofVanilla("textures/block/note_block.png");

    private final Screen parent;
    private SimonGame game;
    private int bestScore;

    private int centerX;
    private int centerY;
    private int blockSize;
    private int spacing;

    private final List<GuiNoteParticle> guiParticles = new java.util.ArrayList<>();
    private int lastShownStep = -1;

    public SimonGameScreen(Screen parent) {
        super(Text.translatable("game.simon.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        bestScore = GameRecords.getInstance().getBestScore("simon");

        if (game == null) {
            game = new SimonGame(3);
        }

        int buttonWidth = Math.min(90, this.width / 6);
        int buttonHeight = 16;
        int y = this.height - 28;
        int spacingButton = 8;

        this.addDrawableChild(ButtonWidget.builder(
                        ScreenTexts.BACK,
                        button -> {
                            if (this.client != null) this.client.setScreen(this.parent);
                        }
                )
                .dimensions(this.width / 2 - buttonWidth - spacingButton / 2, y, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("game.simon.button.newgame"),
                        button -> {
                            game.reset();
                        }
                )
                .dimensions(this.width / 2 + spacingButton / 2, y, buttonWidth, buttonHeight)
                .build());

        calculateLayout();
    }

    private void calculateLayout() {
        blockSize = Math.max(28, Math.min(64, this.width / 8));
        spacing = Math.max(6, blockSize / 6);
        int totalWidth = blockSize * 5 + spacing * 4;
        centerX = (this.width - totalWidth) / 2;
        centerY = Math.max(80, (this.height / 2) - blockSize / 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        guiParticles.removeIf(p -> !p.isAlive());
        for (GuiNoteParticle p : guiParticles) {
            p.update();
        }

        super.renderPanoramaBackground(context, delta);
        context.fillGradient(0, 0, this.width, this.height, 0xB0000000, 0xC0000000);

        if (game != null && game.getState() != SimonGame.State.LOST) {
            game.update();
        }

        int pad = 6;
        int panelWidth = blockSize * 5 + spacing * 4 + pad * 2;
        int panelHeight = blockSize + pad * 2;
        int panelX = centerX - pad;
        int panelY = centerY - pad;
        context.fillGradient(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF1a8c99, 0xFF0d5d66);

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF0A1A1F);

        for (int i = 0; i < 5; i++) {
            int x = centerX + i * (blockSize + spacing);
            int y = centerY;
            drawNoteBlock(context, x, y, blockSize, i);
        }

        drawInfo(context);

        if (game != null && game.getState() == SimonGame.State.LOST) {
            drawGameOver(context);
        }

        this.children().forEach(child -> {
            if (child instanceof ButtonWidget button) {
                button.render(context, mouseX, mouseY, delta);
            }
        });

        GpuTextureView gpuTexture = MinecraftClient.getInstance().getTextureManager().getTexture(NOTE_TEXTURE).getGlTextureView();
        RenderSystem.setShaderTexture(0, gpuTexture);

        for (GuiNoteParticle p : guiParticles) {
            float r = ((p.color >> 16) & 0xFF) / 255f;
            float g = ((p.color >> 8) & 0xFF) / 255f;
            float b = (p.color & 0xFF) / 255f;
            float a = p.alpha;

            RenderSystem.setShaderTexture(0, gpuTexture);

            int colorInt =
                    ((int)(a * 255) << 24) |
                            ((int)(r * 255) << 16) |
                            ((int)(g * 255) << 8)  |
                            ((int)(b * 255));

            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    NOTE_TEXTURE,
                    (int)p.x,
                    (int)p.y,
                    0, 0,
                    16, 16,
                    16, 16,
                    colorInt
            );
        }


    }

    private void drawNoteBlock(DrawContext context, int x, int y, int size, int index) {
        GpuTextureView gpuTexture = MinecraftClient.getInstance().getTextureManager().getTexture(NOTE_BLOCK_TEXTURE).getGlTextureView();
        RenderSystem.setShaderTexture(0, gpuTexture);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, NOTE_BLOCK_TEXTURE, x, y, 0, 0, size, size, size, size);

        if (game != null && game.getState() == SimonGame.State.SHOWING) {
            int showing = game.getCurrentlyShowingIndex();
            if (showing >= 0 && showing < game.getSequence().size()) {
                int seqIdx = game.getSequence().get(showing);

                if (seqIdx == index && lastShownStep != showing) {
                    lastShownStep = showing;
                    int color = switch (index) {
                        case 0 -> 0xFFFF0000; // красный
                        case 1 -> 0xFF00FF00; // зелёный
                        case 2 -> 0xFF0000FF; // синий
                        case 3 -> 0xFFFFFF00; // жёлтый
                        case 4 -> 0xFFFF00FF; // пурпурный
                        default -> 0xFFFFFFFF;
                    };

                    guiParticles.add(new GuiNoteParticle(x + blockSize / 2f, y, color));
                }
            } else {
                lastShownStep = -1;
            }
        }

        int mx = (int) (this.client.mouse.getX() * this.width / this.client.getWindow().getFramebufferWidth());
        int my = (int) (this.client.mouse.getY() * this.height / this.client.getWindow().getFramebufferHeight());
        if (mx >= x && mx <= x + size && my >= y && my <= y + size) {
            context.fill(x, y, x + size, y + size, 0x33FFFFFF);
        }

        // Рисуем рамку
        context.fill(x, y + size - 4, x + size, y + size - 3, 0xFF444444);
    }

    private boolean isCurrentlyLit() {
        return (game != null && game.getState() == SimonGame.State.SHOWING && game.getCurrentlyShowingIndex() >= 0);
    }

    private void drawInfo(DrawContext context) {
        int panelY = 20;
        int centerX = this.width / 2;
        int panelWidth = 200;

        String titleStr = this.textRenderer.trimToWidth(Text.translatable("game.simon.title").getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(titleStr), centerX - this.textRenderer.getWidth(titleStr) / 2, panelY, 0xFF00FFFF, true);

        String seqStr = this.textRenderer.trimToWidth(Text.translatable("game.simon.sequence_length", game != null ? game.getSequenceLength() : 0).getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(seqStr), centerX - this.textRenderer.getWidth(seqStr) / 2, panelY + 15, 0xFFAAAAAA, true);

        String bestStr = this.textRenderer.trimToWidth(Text.translatable("game.simon.best_score", bestScore).getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(bestStr), centerX - this.textRenderer.getWidth(bestStr) / 2, panelY + 30, 0xFFAAAAAA, true);
    }


    private void drawGameOver(DrawContext context) {
        int boxW = 260;
        int boxH = 120;
        int bx = (this.width - boxW) / 2;
        int by = (this.height - boxH) / 2;

        context.fillGradient(bx, by, bx + boxW, by + boxH, 0xD0AA0000, 0xD0550000);

        int centerX = bx + boxW / 2;
        int y = by + 18;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("game.simon.lose"), centerX, y, 0xFFFFFFFF);
        y += 22;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("game.simon.length", (game != null ? game.getSequenceLength() - 1 : 0)), centerX, y, 0xFFFFFF00);
        y += 22;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("game.simon.restart_hint"), centerX, y, 0xFFCCCCCC);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (game == null) return super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        if (game.getState() == SimonGame.State.SHOWING) {
            return false;
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        for (int i = 0; i < 5; i++) {
            int x = centerX + i * (blockSize + spacing);
            int y = centerY;
            if (mx >= x && mx <= x + blockSize && my >= y && my <= y + blockSize) {
                boolean handled = game.clickButton(i);

                int color = switch (i) {
                    case 0 -> 0xFFFF0000;
                    case 1 -> 0xFF00FF00;
                    case 2 -> 0xFF0000FF;
                    case 3 -> 0xFFFFFF00;
                    case 4 -> 0xFFFF00FF;
                    default -> 0xFFFFFFFF;
                };
                guiParticles.add(new GuiNoteParticle(x + blockSize / 2f, y, color));

                if (game.getState() == SimonGame.State.LOST) {
                    int score = Math.max(0, game.getSequenceLength() - 1);
                    if (score > bestScore) {
                        bestScore = score;
                        GameRecords.getInstance().setBestScore("simon", bestScore);
                    }
                }
                return handled || super.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            if (game != null) game.reset();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.client != null) this.client.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
