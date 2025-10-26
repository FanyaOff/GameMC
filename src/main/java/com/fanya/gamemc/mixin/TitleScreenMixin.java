package com.fanya.gamemc.mixin;

import com.fanya.gamemc.screen.GameSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addCustomButton(CallbackInfo ci) {
        if (this.client == null) return;

        int buttonWidth = 200;
        int buttonHeight = 20;
        int l = this.height / 4 + 48;
        int x = this.width / 2 - buttonWidth / 2;
        int y = l + 72 + 36;

        ButtonWidget arcadeButton = ButtonWidget.builder(
                        Text.translatable("menu.gamemc.menubtn"),
                        button -> this.client.setScreen(new GameSelectionScreen((TitleScreen)(Object)this))
                )
                .dimensions(x, y, buttonWidth, buttonHeight)
                .build();

        this.addDrawableChild(arcadeButton);
    }

}
