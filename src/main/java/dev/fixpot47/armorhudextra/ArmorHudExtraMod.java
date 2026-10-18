package dev.fixpot47.armorhudextra;

import com.mojang.blaze3d.platform.InputConstants;
import dev.fixpot47.armorhudextra.compat.ModCompat;
import dev.fixpot47.armorhudextra.config.ArmorHudExtraConfig;
import dev.fixpot47.armorhudextra.mixin.InventoryMenuAccessor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.Ukutils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArmorHudExtraMod {
    public static final String MOD_ID = "fixpots-armor-hud-extra";

    @Getter
    private static final ConfigManager<ArmorHudExtraConfig> manager =
            ConfigManager.createDefault(ArmorHudExtraConfig.class, MOD_ID);

    public static final int STEP = 20;
    public static final int SIZE = 22;
    public static final int HOTBAR_OFFSET = 98;
    public static final int OFFHAND_OFFSET = 29;
    public static final int ATTACK_INDICATOR_OFFSET = 23;
    public static final int WARNING_SIZE = 8;

    public static final EquipmentSlot[] SLOT_IDS = InventoryMenuAccessor.getSLOT_IDS();

    private static final Map<EquipmentSlot, Integer> LAST_DAMAGE = new EnumMap<>(EquipmentSlot.class);

    @Getter
    @Setter
    private static ModCompat modCompat = new ModCompat.NoOpModCompat();

    private ArmorHudExtraMod() {
    }

    public record HudEntry(ItemStack stack, EquipmentSlot armorSlot, boolean heldItem) {
        public boolean isArmor() {
            return armorSlot != null && !heldItem;
        }
    }

    @Nullable
    public static Player getCameraPlayer() {
        return Minecraft.getInstance().getCameraEntity() instanceof Player player ? player : null;
    }

    public static List<HudEntry> getHudEntries(Player player) {
        ArmorHudExtraConfig config = manager.getConfig();
        List<HudEntry> armorEntries = Arrays.stream(SLOT_IDS)
                .map(slot -> new HudEntry(player.getItemBySlot(slot), slot, false))
                .toList();

        List<HudEntry> entries = new ArrayList<>();
        switch (config.getWidgetShown()) {
            case ALWAYS -> entries.addAll(armorEntries);
            case IF_ANY_PRESENT -> {
                boolean anyArmor = armorEntries.stream().anyMatch(entry -> !entry.stack().isEmpty());
                if (anyArmor) entries.addAll(armorEntries);
            }
            case NOT_EMPTY -> armorEntries.stream()
                    .filter(entry -> !entry.stack().isEmpty())
                    .forEach(entries::add);
            case DAMAGED_PIECES -> armorEntries.stream()
                    .filter(entry -> shouldShowWarning(entry.stack()))
                    .forEach(entries::add);
        }

        if (config.isShowMainHand() && !player.getMainHandItem().isEmpty()) {
            entries.add(new HudEntry(player.getMainHandItem(), null, true));
        }
        if (config.isShowOffhand() && !player.getOffhandItem().isEmpty()) {
            entries.add(new HudEntry(player.getOffhandItem(), null, true));
        }

        return entries;
    }

    public static Optional<Rect2i> getWidgetRect(GuiGraphicsExtractor graphics, Player player) {
        ArmorHudExtraConfig config = manager.getConfig();
        List<HudEntry> entries = getHudEntries(player);
        if (entries.isEmpty()) return Optional.empty();

        final int sideMultiplier;
        final int sideOffsetMultiplier;
        if ((config.getAnchor() == ArmorHudExtraConfig.Anchor.HOTBAR && config.getSide() == ArmorHudExtraConfig.Side.LEFT)
                || (config.getAnchor() != ArmorHudExtraConfig.Anchor.HOTBAR && config.getSide() == ArmorHudExtraConfig.Side.RIGHT)) {
            sideMultiplier = -1;
            sideOffsetMultiplier = -1;
        } else {
            sideMultiplier = 1;
            sideOffsetMultiplier = 0;
        }

        final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
            case ALWAYS_IGNORE -> 0;
            case ALWAYS_LEAVE_SPACE ->
                    player.getMainArm() == config.getSide().asArm() ? ATTACK_INDICATOR_OFFSET : OFFHAND_OFFSET;
            case ADHERE -> {
                if (player.getMainArm() == config.getSide().asArm()) {
                    if (Minecraft.getInstance().options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR
                            && player.getAttackStrengthScale(0) < 1) {
                        yield ATTACK_INDICATOR_OFFSET;
                    }
                } else if (!player.getOffhandItem().isEmpty()) {
                    yield OFFHAND_OFFSET;
                }
                yield 0;
            }
        };

        final int textureWidth = SIZE + ((entries.size() - 1) * STEP);
        final int widgetWidth = config.getOrientation() == ArmorHudExtraConfig.Orientation.VERTICAL ? SIZE : textureWidth;
        final int widgetHeight = config.getOrientation() == ArmorHudExtraConfig.Orientation.VERTICAL ? textureWidth : SIZE;

        final int widgetX = config.getOffsetX() * sideMultiplier + switch (config.getAnchor()) {
            case TOP_CENTER -> (graphics.guiWidth() - widgetWidth) / 2;
            case TOP, BOTTOM -> (widgetWidth - graphics.guiWidth()) * sideOffsetMultiplier;
            case HOTBAR -> graphics.guiWidth() / 2
                    + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier)
                    + (widgetWidth * sideOffsetMultiplier);
        };

        final int widgetY = switch (config.getAnchor()) {
            case BOTTOM, HOTBAR -> graphics.guiHeight() - widgetHeight - config.getOffsetY() - modCompat.screenSafeArea();
            case TOP, TOP_CENTER -> config.getOffsetY();
        };

        return Optional.of(new Rect2i(widgetX, widgetY, widgetWidth, widgetHeight));
    }

    public static Optional<Rect2i> getEffectiveWidgetRect(GuiGraphicsExtractor graphics, Player player) {
        ArmorHudExtraConfig config = manager.getConfig();
        return getWidgetRect(graphics, player).map(rect -> {
            if (config.getOrientation() == ArmorHudExtraConfig.Orientation.HORIZONTAL) {
                int extraHeight = 0;
                if (config.isWarningShown()) {
                    extraHeight += WARNING_SIZE + 2 + (config.getWarningBobIntensity() / 2);
                }
                if (config.getDurabilityDisplay() != ArmorHudExtraConfig.DurabilityDisplay.BAR) {
                    extraHeight += Minecraft.getInstance().font.lineHeight;
                }
                rect.setHeight(rect.getHeight() + extraHeight);
                if (!config.getAnchor().isTop()) {
                    rect.setY(rect.getY() - extraHeight);
                }
            }
            return rect;
        });
    }

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        double percentage = 1.0 - ((double) damage / maxDamage);
        return percentage <= manager.getConfig().getMinDurabilityPercentage()
                || maxDamage - damage <= manager.getConfig().getMinDurabilityValue();
    }

    public static boolean shouldPlayBreakSound(Player player) {
        ArmorHudExtraConfig config = manager.getConfig();
        boolean alert = false;

        for (EquipmentSlot slot : SLOT_IDS) {
            alert |= updateDamageState(slot, player.getItemBySlot(slot), true);
        }

        if (config.isWarnHeldItems()) {
            if (config.isShowMainHand()) {
                alert |= updateDamageState(EquipmentSlot.MAINHAND, player.getMainHandItem(), true);
            }
            if (config.isShowOffhand()) {
                alert |= updateDamageState(EquipmentSlot.OFFHAND, player.getOffhandItem(), true);
            }
        }

        return alert;
    }

    private static boolean updateDamageState(EquipmentSlot slot, ItemStack stack, boolean enabled) {
        int current = stack.isDamageableItem() ? stack.getDamageValue() : 0;
        Integer previous = LAST_DAMAGE.put(slot, current);
        return enabled
                && previous != null
                && current > previous
                && shouldShowWarning(stack);
    }

    public static void onInitialize() {
        Ukutils.registerToggleBind(
                new KeyMapping(
                        "armorhudextra.keybind.toggle",
                        InputConstants.UNKNOWN.getValue(),
                        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "key"))
                ),
                () -> manager.getConfig().isEnabled(),
                value -> manager.getConfig().setEnabled(value),
                Component.translatable("armorhudextra.keybind.toggle.msg")
        );
    }
}
