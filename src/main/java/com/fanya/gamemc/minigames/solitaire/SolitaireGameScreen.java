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

    private int cardSizeX, cardSizeY, spacingX, spacingY, playWidth, playHeight, playStartX, playStartY, btnWidth, btnHeight, spacingBtn;

    private int moveCounter = 0;

    public SolitaireGameScreen(Screen parent) {
        super(Text.translatable("game.solitaire.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if(game == null) game = new SolitaireGame();

        cardSizeX = 32;
        cardSizeY = 48;
        spacingX = 10;
        spacingY = 7;

        playWidth = 7 * (cardSizeX + spacingX) + spacingX; // 304
        playHeight = 22 * spacingY + 2 * cardSizeY; // 316

        btnWidth = 100;
        btnHeight = 20;
        spacingBtn = 10;

        playStartX = Math.max((width - playWidth) / 2, 0);
        playStartY = Math.min((height - playHeight - btnHeight - spacingBtn) * 2 / 3 + btnHeight + spacingBtn, height - playHeight);

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

        renderUI(context);
        // for debugging
        //renderSelectBorder(context, mouseX, mouseY, 0x5FFFFF00);
        renderCards(context);

    }

    private void renderSelectBorder(DrawContext context, int mouseX, int mouseY, int selectColor) {
        for(int n = 0; n < 7; n++) {
            int i = playStartX+spacingX+(cardSizeX+spacingX)*n;
            int x = mouseX-i;
            if(x >= 0 && x <= cardSizeX) {
                if(mouseY >= playStartY+spacingY && mouseY <= playStartY+spacingY+cardSizeY) {
                    if(n != 2) {
                        context.fill(i - 1, playStartY + spacingY - 1, i + cardSizeX + 1, playStartY + spacingY + cardSizeY + 1, selectColor);
                    }
                }
                int maxM = game.getColonCount(n);
                for(int m = 0; m < maxM; m++) {
                    int j = playStartY+cardSizeY+spacingY*(2+m);
                    int y = mouseY-j;
                    if(y >= 0 && y <= (m == maxM-1 ? cardSizeY : spacingY)) {
                        context.fill(i-1,j-1,i+cardSizeX+1,j+cardSizeY+1, selectColor);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void renderUI(DrawContext context) {
        // main panel
        context.fillGradient(playStartX-2, playStartY-2, playStartX + playWidth+2, playStartY + playHeight+2, 0xFF1a8c99, 0xFF0d5d66);
        context.fill(playStartX, playStartY, playStartX + playWidth, playStartY + playHeight, 0xFF0A1A1F);

        // deck panel
        context.fillGradient(playStartX+spacingX-1, playStartY+spacingY-1, playStartX+spacingX+cardSizeX+1, playStartY+spacingY+cardSizeY+1, 0x881a8c99, 0x880d5d66);
        context.fillGradient(playStartX+spacingX*2+cardSizeX-1, playStartY+spacingY-1, playStartX+(spacingX+cardSizeX)*2+1, playStartY+spacingY+cardSizeY+1, 0x881a8c99, 0x880d5d66);
        context.fill(playStartX+spacingX, playStartY+spacingY, playStartX+spacingX+cardSizeX, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);
        context.fill(playStartX+spacingX*2+cardSizeX, playStartY+spacingY, playStartX+(spacingX+cardSizeX)*2, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);

        // base panel
        for(int x = playStartX+(spacingX+cardSizeX)*3+spacingX; x < playStartX + playWidth; x += spacingX+cardSizeX) {
            context.fillGradient(x - 1, playStartY+spacingY-1, x + cardSizeX + 1, playStartY+spacingY+cardSizeY+1, 0x881a8c99, 0x880d5d66);
            context.fill(x, playStartY+spacingY, x + cardSizeX, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);
        }
    }

    private void renderCards(DrawContext context) {
        if(!game.isDeckEmpty()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BACK_CARD,playStartX+spacingX, playStartY+spacingY,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
        }

        SolitaireCard card = game.getGameDeck();
        if(card != null)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()],
                    playStartX+spacingX*2+cardSizeX, playStartY+spacingY,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
        for(int i = 0; i < 4; i ++) renderBase(context, i);
        for(int i = 0; i < 7; i ++) renderColon(context, i);
    }

    private void renderColon(DrawContext context, int num) {
        SolitaireCard card = game.getColon(num);
        int y = playStartY+spacingY*2+cardSizeY;
        while (card != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, card.isShown() ? CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()] : BACK_CARD,
                    playStartX+spacingX*(1+num)+cardSizeX*num, y,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
            card = card.getNext();
            y += spacingY;
        }
    }

    private void renderBase(DrawContext context, int num) {
        SolitaireCard card = game.getBase(num);
        if(card != null)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()],
                    playStartX+spacingX*(4+num)+cardSizeX*(3+num), playStartY+spacingY,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
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
        GameMC.LOGGER.info(click.isLeft() + " " + click.isRight() + " " + click.button());
        if(click.button() == 1) {
            //play "trrr" sound
            game.checkWin();
            return true;
        } else if(click.button() == 0) {
            int cardPos = getCardByMouse(click.x(), click.y());
            int cartX = cardPos%10,
                cartY = (cardPos/10)-1;
            GameMC.LOGGER.info(String.valueOf(cardPos));
            switch (cardPos) {
                case 0: game.nextDeckCard();
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) { // move mouse
        if(click.isLeft()) {
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) { // unclick mouse
        return super.mouseReleased(click);
    }

    @Override
    public boolean shouldPause() {
        return false; // игра не ставится на паузу при открытии меню
    }




    public int getCardByMouse(double mouseX, double mouseY) {
        for(int n = 0; n < 7; n++) {
            int i = playStartX+spacingX+(cardSizeX+spacingX)*n;
            int x = (int) mouseX-i;
            if(x >= 0 && x <= cardSizeX) {
                if(mouseY >= playStartY+spacingY && mouseY <= playStartY+spacingY+cardSizeY) {
                    return n;
                }
                int maxM = game.getColonCount(n);
                for(int m = 0; m < maxM; m++) {
                    int j = playStartY+cardSizeY+spacingY*(2+m);
                    int y = (int) mouseY-j;
                    if(y >= 0 && y <= (m == maxM-1 ? cardSizeY : spacingY)) {
                        return (m+1)*10+n;
                    }
                }
                break;
            }
        }
        return -1;
    }
}
