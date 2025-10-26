package com.fanya.gamemc.mixin;

import com.fanya.gamemc.screen.GameSelectionScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addGameSelectionButton(CallbackInfo ci) {
        if (this.client == null) return;

        int buttonWidth = 204;
        int buttonHeight = 20;

        int x = this.width / 2 - buttonWidth / 2;
        int y = this.height / 4 + 105 + 24; // стандартная кнопка "Выйти" + отступ

        ButtonWidget arcadeButton = ButtonWidget.builder(
                        Text.translatable("menu.gamemc.menubtn"),
                        button -> this.client.setScreen(new GameSelectionScreen((Screen)(Object)this))
                )
                .dimensions(x, y, buttonWidth, buttonHeight)
                .build();

        this.addDrawableChild(arcadeButton);
    }

}
