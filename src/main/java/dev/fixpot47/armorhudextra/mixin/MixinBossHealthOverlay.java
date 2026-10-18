package dev.fixpot47.armorhudextra.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import dev.fixpot47.armorhudextra.config.ArmorHudExtraConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BossHealthOverlay.class)
public class MixinBossHealthOverlay {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void calculateOffset(GuiGraphicsExtractor graphics, CallbackInfo ci, @Share("offset") LocalIntRef offsetRef) {
        ArmorHudExtraConfig config = ArmorHudExtraMod.getManager().getConfig();
        if (!config.isEnabled()
                || !config.isPushBossbars()
                || config.getAnchor() != ArmorHudExtraConfig.Anchor.TOP_CENTER) {
            return;
        }

        Player player = ArmorHudExtraMod.getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = ArmorHudExtraMod.getEffectiveWidgetRect(graphics, player);
        rect.ifPresent(value -> offsetRef.set(value.getY() + value.getHeight()));
    }

    @ModifyVariable(method = "extractRenderState", at = @At("STORE"), name = "yOffset")
    public int pushBossBars(int y, @Share("offset") LocalIntRef offsetRef) {
        return y + offsetRef.get();
    }
}
