package dev.fixpot47.armorhudextra.mixin;

import dev.fixpot47.armorhudextra.ArmorHudBridge;
import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import dev.fixpot47.armorhudextra.ArmorHudExtraMod.HeldEntry;
import dev.fixpot47.armorhudextra.config.ArmorHudExtraConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

import static dev.fixpot47.armorhudextra.ArmorHudExtraMod.SIZE;
import static dev.fixpot47.armorhudextra.ArmorHudExtraMod.STEP;

@Mixin(value = Hud.class, priority = 900)
public abstract class MixinHud {
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

    @Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_SPRITE;

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    public void renderArmorHudExtra(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        Profiler.get().push(ArmorHudExtraMod.MOD_ID);
        drawExtra(graphics, tickCounter);
        Profiler.get().pop();
    }

    @Unique
    private void drawExtra(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        ArmorHudExtraConfig config = ArmorHudExtraMod.getManager().getConfig();
        if (!config.isEnabled() || !ArmorHudBridge.isBaseEnabled()) {
            return;
        }

        Player player = ArmorHudExtraMod.getCameraPlayer();
        if (player == null) {
            return;
        }

        Optional<Rect2i> baseRectOptional = ArmorHudBridge.getWidgetRect(graphics, player);
        if (baseRectOptional.isEmpty()) {
            return;
        }

        Rect2i baseRect = baseRectOptional.get();
        List<HeldEntry> heldEntries = ArmorHudExtraMod.getHeldEntries(player);

        String orientation = ArmorHudBridge.orientation();
        String anchor = ArmorHudBridge.anchor();
        String side = ArmorHudBridge.side();
        String style = ArmorHudBridge.style();

        for (int i = 0; i < heldEntries.size(); i++) {
            HeldEntry entry = heldEntries.get(i);
            int[] position = heldSlotPosition(baseRect, i, heldEntries.size(), orientation, anchor, side);
            drawHeldSlot(
                    graphics,
                    tickCounter,
                    player,
                    entry,
                    position[0],
                    position[1],
                    i,
                    style,
                    orientation,
                    anchor,
                    side,
                    config
            );
        }

        if (config.isShowLowestArmorPercentage()) {
            drawLowestArmorPercentage(graphics, player, baseRect, orientation, anchor);
        }
    }

    @Unique
    private int[] heldSlotPosition(
            Rect2i baseRect,
            int index,
            int count,
            String orientation,
            String anchor,
            String side
    ) {
        if ("VERTICAL".equals(orientation)) {
            boolean extendDown = "TOP".equals(anchor) || "TOP_CENTER".equals(anchor);
            int y = extendDown
                    ? baseRect.getY() + baseRect.getHeight() - 2 + (STEP * index)
                    : baseRect.getY() - (STEP * (index + 1));
            return new int[]{baseRect.getX(), y};
        }

        boolean extendRight = "HOTBAR".equals(anchor)
                ? "RIGHT".equals(side)
                : "LEFT".equals(side);

        int x = extendRight
                ? baseRect.getX() + baseRect.getWidth() - 2 + (STEP * index)
                : baseRect.getX() - (STEP * (index + 1));

        return new int[]{x, baseRect.getY()};
    }

    @Unique
    private void drawHeldSlot(
            GuiGraphicsExtractor graphics,
            DeltaTracker tickCounter,
            Player player,
            HeldEntry entry,
            int x,
            int y,
            int index,
            String style,
            String orientation,
            String anchor,
            String side,
            ArmorHudExtraConfig config
    ) {
        if (!"NONE".equals(style)) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    HOTBAR_OFFHAND_LEFT_SPRITE,
                    29, 24,
                    0, 1,
                    x, y,
                    SIZE, SIZE,
                    ARGB.white(1.0F)
            );
        }

        ItemStack stack = entry.stack();
        this.extractSlot(graphics, x + 3, y + 3, tickCounter, player, stack, 100 + index);

        boolean warning = config.isWarnHeldItems() && ArmorHudExtraMod.shouldWarn(stack);
        String durability = durabilityText(stack, config);
        if (warning) {
            durability = durability.isEmpty() ? "!" : "! " + durability;
        }

        if (durability.isEmpty()) {
            return;
        }

        int color = warning
                ? ARGB.opaque(0xFF5555)
                : ARGB.opaque(stack.isDamageableItem() ? stack.getBarColor() : 0xFFFFFF);

        if ("VERTICAL".equals(orientation)) {
            boolean detailsRight = "HOTBAR".equals(anchor)
                    ? "RIGHT".equals(side)
                    : "LEFT".equals(side);

            int textY = y + (SIZE - this.getFont().lineHeight) / 2;
            if (detailsRight) {
                graphics.text(this.getFont(), durability, x + SIZE + 2, textY, color);
            } else {
                int width = this.getFont().width(durability);
                graphics.text(this.getFont(), durability, x - width - 2, textY, color);
            }
        } else {
            boolean topAnchor = "TOP".equals(anchor) || "TOP_CENTER".equals(anchor);
            int textY = topAnchor
                    ? y + SIZE + 1
                    : y - this.getFont().lineHeight - 1;
            graphics.centeredText(this.getFont(), durability, x + (SIZE / 2), textY, color);
        }
    }

    @Unique
    private String durabilityText(ItemStack stack, ArmorHudExtraConfig config) {
        if (stack.isEmpty()
                || !stack.isDamageableItem()
                || config.getHeldItemDurability() == ArmorHudExtraConfig.DurabilityDisplay.NONE) {
            return "";
        }

        int max = stack.getMaxDamage();
        int remaining = max - stack.getDamageValue();

        if (remaining == max && !config.isShowFullDurability()) {
            return "";
        }

        return switch (config.getHeldItemDurability()) {
            case NONE -> "";
            case REMAINING -> String.valueOf(remaining);
            case PERCENTAGE -> (int) Math.floor((remaining / (double) max) * 100.0) + "%";
            case EXACT -> remaining + "/" + max;
        };
    }

    @Unique
    private void drawLowestArmorPercentage(
            GuiGraphicsExtractor graphics,
            Player player,
            Rect2i baseRect,
            String orientation,
            String anchor
    ) {
        ItemStack lowest = ArmorHudExtraMod.getLowestArmorPiece(player);
        if (lowest.isEmpty() || !lowest.isDamageableItem()) {
            return;
        }

        int remaining = lowest.getMaxDamage() - lowest.getDamageValue();
        int percentage = (int) Math.floor((remaining / (double) lowest.getMaxDamage()) * 100.0);
        String text = "A " + percentage + "%";
        int color = ARGB.opaque(lowest.getBarColor());

        if ("VERTICAL".equals(orientation)) {
            int y = baseRect.getY() - this.getFont().lineHeight - 2;
            graphics.centeredText(this.getFont(), text, baseRect.getX() + (SIZE / 2), y, color);
            return;
        }

        boolean topAnchor = "TOP".equals(anchor) || "TOP_CENTER".equals(anchor);
        int y = topAnchor
                ? baseRect.getY() + baseRect.getHeight() + this.getFont().lineHeight + 3
                : baseRect.getY() - (this.getFont().lineHeight * 2) - 3;

        graphics.centeredText(
                this.getFont(),
                text,
                baseRect.getX() + (baseRect.getWidth() / 2),
                y,
                color
        );
    }
}
