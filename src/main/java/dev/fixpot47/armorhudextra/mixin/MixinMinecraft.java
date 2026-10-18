package dev.fixpot47.armorhudextra.mixin;

import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "tick", at = @At("RETURN"))
    public void playLowDurabilityWarning(CallbackInfo ci) {
        Player player = ArmorHudExtraMod.getCameraPlayer();
        if (player == null) return;

        if (ArmorHudExtraMod.getManager().getConfig().isPlayBreakSound()
                && ArmorHudExtraMod.shouldPlayBreakSound(player)) {
            player.makeSound(SoundEvents.ITEM_BREAK.value());
        }
    }
}
