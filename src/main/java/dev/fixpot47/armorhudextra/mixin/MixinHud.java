package dev.fixpot47.armorhudextra.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import dev.fixpot47.armorhudextra.ArmorHudExtraMod.HudEntry;
import dev.fixpot47.armorhudextra.config.ArmorHudExtraConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

import static dev.fixpot47.armorhudextra.ArmorHudExtraMod.*;

@Mixin(Hud.class)
public abstract class MixinHud {
    @Shadow @Final private RandomSource random;

    @Shadow
    protected abstract void extractSlot(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            DeltaTracker tickCounter,
            Player player,
            ItemStack stack,
            int seed
    );

    @Shadow
    public abstract Font getFont();

    @Shadow @Final private static Identifier HOTBAR_SPRITE;
    @Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_SPRITE;

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    public void renderArmorHudExtra(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        Profiler.get().push(MOD_ID);
        drawArmorHudExtra(graphics, tickCounter);
        Profiler.get().pop();
    }

    @Unique
    private void drawArmorHudExtra(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        ArmorHudExtraConfig config = getManager().getConfig();
        if (!config.isEnabled()) return;

        Player player = getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = getWidgetRect(graphics, player);
        if (rect.isEmpty()) return;

        List<HudEntry> entries = getHudEntries(player);
        if (config.isReversed()) {
            entries = entries.reversed();
        }

        int textureWidth = SIZE + ((entries.size() - 1) * STEP);

        graphics.pose().pushMatrix();
        graphics.pose().translate(rect.get().getX(), rect.get().getY());

        if (config.getOrientation() == ArmorHudExtraConfig.Orientation.VERTICAL) {
            graphics.pose().rotate(Mth.HALF_PI).translate(0, -SIZE);
        }

        int color = ARGB.white(ArmorHudExtraMod.getModCompat().hudOpacity());

        switch (config.getStyle()) {
            case HOTBAR -> {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        HOTBAR_SPRITE,
                        182, 22,
                        0, 0,
                        0, 0,
                        textureWidth - 3, SIZE,
                        color
                );
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        HOTBAR_SPRITE,
                        182, 22,
                        182 - 3, 0,
                        textureWidth - 3, 0,
                        3, SIZE,
                        color
                );
            }
            case ROUNDED_CORNERS -> {
                if (entries.size() > 1) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, 3, SIZE, color);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 3, 0, 3, 0, textureWidth - 6, SIZE, color);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, SIZE - 3, 1, textureWidth - 3, 0, 3, SIZE, color);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE, SIZE, color);
                }
            }
            case ROUNDED -> {
                if (entries.size() > 1) {
                    int borderWidth = (SIZE - STEP) / 2;
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE - borderWidth, SIZE, color);
                    for (int i = 1; i < entries.size() - 1; i++) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, borderWidth, 1, borderWidth + i * STEP, 0, STEP, SIZE, color);
                    }
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 1, 1, textureWidth - STEP - borderWidth, 0, SIZE - borderWidth, SIZE, color);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE, SIZE, color);
                }
            }
            case NONE -> {
            }
        }

        graphics.pose().popMatrix();

        for (int i = 0; i < entries.size(); i++) {
            HudEntry entry = entries.get(i);
            ItemStack stack = entry.stack();

            int x = rect.get().getX();
            int y = rect.get().getY();

            switch (config.getOrientation()) {
                case HORIZONTAL -> x += STEP * i;
                case VERTICAL -> y += STEP * i;
            }

            if (config.isIconsShown() && entry.isArmor() && stack.isEmpty()) {
                Identifier identifier = InventoryMenuAccessor.getTEXTURE_EMPTY_SLOTS().get(entry.armorSlot());
                if (identifier != null) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x + 3, y + 3, 16, 16);
                }
            }

            this.extractSlot(graphics, x + 3, y + 3, tickCounter, player, stack, i + 1);

            ArmorHudExtraConfig.Side extrasSide =
                    config.getAnchor() == ArmorHudExtraConfig.Anchor.HOTBAR
                            ? config.getSide()
                            : config.getSide().getOpposite();

            if (config.getAnchor().isTop()
                    && config.getOrientation() == ArmorHudExtraConfig.Orientation.HORIZONTAL) {
                y += SIZE;
            } else if (extrasSide == ArmorHudExtraConfig.Side.RIGHT
                    && config.getOrientation() == ArmorHudExtraConfig.Orientation.VERTICAL) {
                x += SIZE;
            }

            if (config.getDurabilityDisplay() != ArmorHudExtraConfig.DurabilityDisplay.BAR
                    && !stack.isEmpty()
                    && stack.isDamageableItem()) {
                String durability = durabilityText(stack, config);
                if (!durability.isEmpty()) {
                    int textHeight = this.getFont().lineHeight;

                    if (config.getOrientation() == ArmorHudExtraConfig.Orientation.HORIZONTAL) {
                        if (!config.getAnchor().isTop()) {
                            y -= textHeight;
                        }

                        graphics.centeredText(
                                this.getFont(),
                                durability,
                                x + (SIZE / 2),
                                y,
                                ARGB.opaque(stack.getBarColor())
                        );

                        if (config.getAnchor().isTop()) {
                            y += textHeight;
                        }
                    } else {
                        int textWidth = this.getFont().width(durability) + 2;
                        int textY = (SIZE - textHeight) / 2;

                        if (extrasSide == ArmorHudExtraConfig.Side.LEFT) {
                            x -= textWidth;
                        }

                        graphics.text(
                                this.getFont(),
                                durability,
                                x + 1,
                                y + textY,
                                ARGB.opaque(stack.getBarColor())
                        );

                        if (extrasSide == ArmorHudExtraConfig.Side.RIGHT) {
                            x += textWidth;
                        }
                    }
                }
            }

            boolean mayWarn = entry.isArmor() || config.isWarnHeldItems();
            if (mayWarn && config.isWarningShown() && shouldShowWarning(stack)) {
                drawWarning(graphics, config, extrasSide, x, y);
            }
        }
    }

    @Unique
    private String durabilityText(ItemStack stack, ArmorHudExtraConfig config) {
        int max = stack.getMaxDamage();
        int remaining = max - stack.getDamageValue();

        return switch (config.getDurabilityDisplay()) {
            case NUMERIC -> String.valueOf(remaining);
            case PERCENTAGE -> {
                int percentage = (int) Math.floor((remaining / (double) max) * 100.0);
                if (percentage == 100 && !config.isShowFullDurability()) {
                    yield "";
                }
                yield percentage + "%";
            }
            case EXACT -> remaining + "/" + max;
            case BAR -> "";
        };
    }

    @Unique
    private void drawWarning(
            GuiGraphicsExtractor graphics,
            ArmorHudExtraConfig config,
            ArmorHudExtraConfig.Side extrasSide,
            int x,
            int y
    ) {
        if (config.getWarningBobIntensity() != 0) {
            int intensity = config.getWarningBobIntensity();
            y += (int) (this.random.nextInt(intensity) - Math.ceil(intensity / 2F));
        }

        int warningColor = ARGB.opaque(0xFF5555);

        if (config.getOrientation() == ArmorHudExtraConfig.Orientation.HORIZONTAL) {
            if (!config.getAnchor().isTop()) {
                y -= WARNING_SIZE + 2;
            }
            graphics.centeredText(this.getFont(), "!", x + (SIZE / 2), y, warningColor);
        } else {
            int textWidth = this.getFont().width("!") + 2;
            if (extrasSide == ArmorHudExtraConfig.Side.LEFT) {
                x -= textWidth;
            }
            int textY = (SIZE - this.getFont().lineHeight) / 2;
            graphics.text(this.getFont(), "!", x + 1, y + textY, warningColor);
        }
    }

    @Inject(
            method = "extractEffects",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    )
    public void calculateStatusEffectIconsOffset(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci,
            @Share("shift") LocalIntRef shiftRef
    ) {
        ArmorHudExtraConfig config = getManager().getConfig();
        if (!config.isEnabled()
                || !config.isPushStatusEffectIcons()
                || config.getAnchor() != ArmorHudExtraConfig.Anchor.TOP
                || config.getSide() != ArmorHudExtraConfig.Side.RIGHT) {
            return;
        }

        Player player = getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = getEffectiveWidgetRect(graphics, player);
        if (rect.isPresent()) {
            shiftRef.set(rect.get().getY() + rect.get().getHeight());
        }
    }

    @ModifyVariable(method = "extractEffects", at = @At(value = "STORE"), name = "y")
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}
