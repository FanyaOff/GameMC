package com.fanya.gamemc.screen;

import com.fanya.gamemc.minigames.simon.SimonGameScreen;
import com.fanya.gamemc.minigames.snake.SnakeGameScreen;
import com.fanya.gamemc.minigames.snake.SnakeSizeSelectScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GameSelectionScreen extends Screen {
    private final Screen parent;
    private final List<ButtonWidget> gameButtons = new ArrayList<>();
    private ButtonWidget backButton;
    private int scrollOffset = 0;

    private int panelX, panelY, panelWidth = 350, panelHeight = 220;
    private int listAreaX, listAreaY, listAreaWidth, listAreaHeight;

    private int buttonWidth = 200;
    private int buttonHeight = 20;
    private int buttonGap = 8;

    public GameSelectionScreen(Screen parent) {
        super(Text.translatable("menu.gamemc.select"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        panelX = this.width / 2 - panelWidth / 2;
        panelY = this.height / 2 - panelHeight / 2;

        listAreaX = panelX + 16;
        listAreaY = panelY + 58;
        listAreaWidth = panelWidth - 32;

        listAreaHeight = panelHeight - 76 - buttonHeight - 12;

        int centerX = this.width / 2 - buttonWidth / 2;

        gameButtons.clear();
        gameButtons.add(ButtonWidget.builder(
                Text.translatable("menu.gamemc.button.snakegame"),
                button -> setScreenIfPresent(new SnakeSizeSelectScreen(this))
        ).dimensions(centerX, 0, buttonWidth, buttonHeight).build());

        gameButtons.add(ButtonWidget.builder(
                Text.translatable("menu.gamemc.button.simon"),
                button -> setScreenIfPresent(new SimonGameScreen(this))
        ).dimensions(centerX, 0, buttonWidth, buttonHeight).build());

        int backBtnX = this.width / 2 - buttonWidth / 2;
        int backBtnY = panelY + panelHeight - 18 - buttonHeight;
        backButton = ButtonWidget.builder(
                ScreenTexts.BACK,
                button -> setScreenIfPresent(this.parent)
        ).dimensions(backBtnX, backBtnY, buttonWidth, buttonHeight).build();
    }

    private void setScreenIfPresent(Screen scr) {
        if (this.client != null)
            this.client.setScreen(scr);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderPanoramaBackground(context, delta);

        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        context.fill(panelX + 4, panelY + 4, panelX + panelWidth + 4, panelY + panelHeight + 4, 0x80000000);
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0101010, 0xE0202020);

        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF1a8c99);
        context.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF1a8c99);
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF1a8c99);
        context.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1a8c99);

        context.fill(panelX + 40, panelY + 45, panelX + panelWidth - 40, panelY + 47, 0x60FFFFFF);

        String title = this.textRenderer.trimToWidth(Text.translatable("menu.gamemc.title").getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(title),
                this.width / 2 - this.textRenderer.getWidth(title) / 2, panelY + 15, 0xFF00FFFF, true);

        String desc = this.textRenderer.trimToWidth(Text.translatable("menu.gamemc.description").getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(desc),
                this.width / 2 - this.textRenderer.getWidth(desc) / 2, panelY + 30, 0xFFAAAAAA, true);

        MinecraftClient mc = MinecraftClient.getInstance();
        double sf = mc.getWindow().getScaleFactor();
        int scX = (int) (listAreaX * sf);
        int scY = (int) (mc.getWindow().getHeight() - (listAreaY + listAreaHeight) * sf);
        int scW = (int) (listAreaWidth * sf);
        int scH = (int) (listAreaHeight * sf);
        RenderSystem.enableScissorForRenderTypeDraws(scX, scY, scW, scH);

        int y = listAreaY - scrollOffset;
        for (ButtonWidget btn : gameButtons) {
            btn.setX(this.width / 2 - buttonWidth / 2);
            btn.setY(y);
            if (y + buttonHeight > listAreaY && y < listAreaY + listAreaHeight) {
                btn.render(context, mouseX, mouseY, delta);
            }
            y += buttonHeight + buttonGap;
        }
        RenderSystem.disableScissorForRenderTypeDraws();

        backButton.render(context, mouseX, mouseY, delta);

        // скроллбар
        int totalBtnsHeight = (buttonHeight + buttonGap) * gameButtons.size();
        if (totalBtnsHeight > listAreaHeight) {
            int barX = listAreaX + listAreaWidth + 2;
            int barY = listAreaY;
            int barWidth = 6;
            int barHeight = listAreaHeight;
            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x40111111);

            int handleHeight = Math.max(16, (int) ((float) barHeight * barHeight / totalBtnsHeight));
            int handleY = (int) (barY + ((float) scrollOffset / (totalBtnsHeight - listAreaHeight)) * (barHeight - handleHeight));
            context.fill(barX, handleY, barX + barWidth, handleY + handleHeight, 0xFF888888);
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalBtnsHeight = (buttonHeight + buttonGap) * gameButtons.size();
        int maxScroll = Math.max(0, totalBtnsHeight - listAreaHeight);
        if (mouseX > listAreaX && mouseX < listAreaX + listAreaWidth &&
                mouseY > listAreaY && mouseY < listAreaY + listAreaHeight && maxScroll > 0) {
            scrollOffset = clamp(scrollOffset - (int) (verticalAmount * 20), 0, maxScroll);
            return true;
        }
        return false;
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int y = listAreaY - scrollOffset;
        for (ButtonWidget btn : gameButtons) {
            if (y + buttonHeight > listAreaY && y < listAreaY + listAreaHeight) {
                if (btn.mouseClicked(click, doubled)) return true;
            }
            y += buttonHeight + buttonGap;
        }

        if (backButton.mouseClicked(click, doubled)) return true;

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean shouldPause() { return false; }
}
