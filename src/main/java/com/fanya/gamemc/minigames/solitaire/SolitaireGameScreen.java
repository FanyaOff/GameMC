package com.fanya.gamemc.minigames.solitaire;

import com.fanya.gamemc.GameMC;
import com.fanya.gamemc.minigames.solitaire.subclass.SolitaireCard;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SolitaireGameScreen extends Screen {

    private static final Identifier[][] CARD_TEXTURES = parsTextures();
    private static final Identifier BACK_CARD = Identifier.of(GameMC.MOD_ID, "textures/gui/suits/card.png");

    private static Identifier[][] parsTextures() {
        Identifier[][] arr = new Identifier[4][13];
        for(SolitaireCard.Suits suit : SolitaireCard.Suits.values())
            for(SolitaireCard.Denominations denomination : SolitaireCard.Denominations.values())
                arr[suit.ordinal()][denomination.ordinal()] = Identifier.of(GameMC.MOD_ID, SolitaireCard.getTexturePath(suit, denomination));
        return arr;
    }

    private final Screen parent;
    private SolitaireGame game;

    private int cardSizeX, cardSizeY, spacingX, spacingY, playWidth, playHeight, btnWidth, btnHeight, spacingBtn;

    private int moveCounter = 0;

    public SolitaireGameScreen(Screen parent) {
        super(Text.translatable("game.solitaire.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        game = new SolitaireGame();

        int scale = 2;

        cardSizeX = 32*scale;
        cardSizeY = 48*scale;
        spacingX = 10*scale;
        spacingY = 5*scale;

        playWidth = 7 * (cardSizeX + spacingX) + spacingX; // 304
        playHeight = 22 * spacingY + 2 * cardSizeY; // 316

        if(width < playWidth || height < playHeight) {
            scale = 1;
            cardSizeX = 32*scale;
            cardSizeY = 48*scale;
            spacingX = 10*scale;
            spacingY = 5*scale;

            playWidth = 7 * (cardSizeX + spacingX) + spacingX;
            playHeight = 22 * spacingY + 2 * cardSizeY;
        }

        btnWidth = 100;
        btnHeight = 20;
        spacingBtn = 10;

        int totalBtnWidth = btnWidth * 2 + spacingBtn * 2;
        int startX = (this.width - totalBtnWidth) / 2;
        int btnY = 3;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.solitaire.info.back"), b -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(startX, btnY, btnWidth, btnHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("game.solitaire.info.new_game"), b -> {
            game.reset();
        }).dimensions(startX + btnWidth + spacingBtn, btnY, btnWidth, btnHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        //context.drawTexture(RenderPipelines.GUI_TEXTURED, BACK_CARD, 0, 0, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);

        //for(SolitaireCard.Suits suit : SolitaireCard.Suits.values())
        //    for(SolitaireCard.Denominations denomination : SolitaireCard.Denominations.values())
        //        context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[suit.ordinal()][denomination.ordinal()], (cardSizeX + spacing) * denomination.ordinal(), (cardSizeY + spacing) * (suit.ordinal() + 1), 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
        renderUI(context);
    }

    private void renderUI(DrawContext context) {
        int playStartX = Math.max((width - playWidth) / 2, 0);
        int playStartY = Math.min((height - playHeight - btnHeight - spacingBtn) * 2 / 3 + btnHeight + spacingBtn, height - playHeight);

        // main panel
        context.fillGradient(playStartX-2, playStartY-2, playStartX + playWidth+2, playStartY + playHeight+2, 0xFF1a8c99, 0xFF0d5d66);
        context.fill(playStartX, playStartY, playStartX + playWidth, playStartY + playHeight, 0xFF0A1A1F);

        // deck panel
        context.fillGradient(playStartX+spacingX-2, playStartY+spacingY-2, playStartX+spacingX+cardSizeX+2, playStartY+spacingY+cardSizeY+2, 0x881a8c99, 0x880d5d66);
        context.fillGradient(playStartX+spacingX*2+cardSizeX-2, playStartY+spacingY-2, playStartX+(spacingX+cardSizeX)*2+2, playStartY+spacingY+cardSizeY+2, 0x881a8c99, 0x880d5d66);
        context.fill(playStartX+spacingX, playStartY+spacingY, playStartX+spacingX+cardSizeX, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);
        context.fill(playStartX+spacingX*2+cardSizeX, playStartY+spacingY, playStartX+(spacingX+cardSizeX)*2, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);

        // base panel
        for(int x = playStartX+(spacingX+cardSizeX)*3+spacingX; x < playStartX + playWidth; x += spacingX+cardSizeX) {
            context.fillGradient(x - 2, playStartY+spacingY-2, x + cardSizeX + 2, playStartY+spacingY+cardSizeY+2, 0x881a8c99, 0x880d5d66);
            context.fill(x, playStartY+spacingY, x + cardSizeX, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);
        }

        if(!game.isDeckEmpty()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BACK_CARD,playStartX+spacingX, playStartY+spacingY,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (game.getState() == SolitaireGame.State.RUNNING) {
            switch (input.key()) {
                case GLFW.GLFW_KEY_R -> game.reset();
                case GLFW.GLFW_KEY_ESCAPE -> {
                    if (client != null) client.setScreen(parent);
                }
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if(click.isRight()) {
            //play "trrr" sound
            game.checkWin();
            return true;
        } else if(click.isLeft()) {
            //TODO
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean shouldPause() {
        return false; // игра не ставится на паузу при открытии меню
    }
}
