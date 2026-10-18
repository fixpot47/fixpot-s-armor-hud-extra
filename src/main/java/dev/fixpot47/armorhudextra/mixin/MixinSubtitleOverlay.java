package dev.fixpot47.armorhudextra.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import dev.fixpot47.armorhudextra.config.ArmorHudExtraConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SubtitleOverlay.class)
public class MixinSubtitleOverlay {
    @Inject(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I", ordinal = 3)
    )
    public void calculateOffset(
            GuiGraphicsExtractor graphics,
            CallbackInfo ci,
            @Share("offset") LocalIntRef offsetRef
    ) {
        ArmorHudExtraConfig config = ArmorHudExtraMod.getManager().getConfig();
        if (!config.isEnabled()
                || !config.isPushSubtitles()
                || config.getAnchor() != ArmorHudExtraConfig.Anchor.BOTTOM
                || config.getSide() != ArmorHudExtraConfig.Side.RIGHT) {
            return;
        }

        Player player = ArmorHudExtraMod.getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = ArmorHudExtraMod.getEffectiveWidgetRect(graphics, player);
        if (rect.isPresent()) {
            offsetRef.set(Math.max(rect.get().getHeight() - 25, 0));
        }
    }

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    public void offset(
            GuiGraphicsExtractor graphics,
            CallbackInfo ci,
            @Share("offset") LocalIntRef offsetRef
    ) {
        graphics.pose().translate(0.0F, -offsetRef.get());
    }
}
