package dev.fixpot47.armorhudextra;

import com.mojang.blaze3d.platform.InputConstants;
import dev.fixpot47.armorhudextra.config.ArmorHudExtraConfig;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.Ukutils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ArmorHudExtraMod {
    public static final String MOD_ID = "fixpots-armor-hud-extra";
    public static final int STEP = 20;
    public static final int SIZE = 22;

    @Getter
    private static final ConfigManager<ArmorHudExtraConfig> manager =
            ConfigManager.createDefault(ArmorHudExtraConfig.class, MOD_ID);

    private static final Map<EquipmentSlot, Integer> LAST_DAMAGE = new EnumMap<>(EquipmentSlot.class);

    private ArmorHudExtraMod() {
    }

    public record HeldEntry(ItemStack stack, EquipmentSlot slot) {
    }

    public static Player getCameraPlayer() {
        return Minecraft.getInstance().getCameraEntity() instanceof Player player ? player : null;
    }

    public static List<HeldEntry> getHeldEntries(Player player) {
        ArmorHudExtraConfig config = manager.getConfig();
        List<HeldEntry> entries = new ArrayList<>(2);

        if (config.isShowMainHand() && !player.getMainHandItem().isEmpty()) {
            entries.add(new HeldEntry(player.getMainHandItem(), EquipmentSlot.MAINHAND));
        }
        if (config.isShowOffhand() && !player.getOffhandItem().isEmpty()) {
            entries.add(new HeldEntry(player.getOffhandItem(), EquipmentSlot.OFFHAND));
        }

        return entries;
    }

    public static boolean shouldWarn(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return false;
        }

        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        double percentage = remaining / (double) stack.getMaxDamage();

        ArmorHudExtraConfig config = manager.getConfig();
        return remaining <= config.getMinDurabilityValue()
                || percentage <= config.getMinDurabilityPercentage();
    }

    public static boolean shouldPlayWarningSound(Player player) {
        ArmorHudExtraConfig config = manager.getConfig();
        if (!config.isWarnHeldItems() || !config.isPlayWarningSound()) {
            syncDamageState(player);
            return false;
        }

        boolean alert = false;
        if (config.isShowMainHand()) {
            alert |= updateDamageState(EquipmentSlot.MAINHAND, player.getMainHandItem());
        }
        if (config.isShowOffhand()) {
            alert |= updateDamageState(EquipmentSlot.OFFHAND, player.getOffhandItem());
        }
        return alert;
    }

    private static void syncDamageState(Player player) {
        updateDamageState(EquipmentSlot.MAINHAND, player.getMainHandItem());
        updateDamageState(EquipmentSlot.OFFHAND, player.getOffhandItem());
    }

    private static boolean updateDamageState(EquipmentSlot slot, ItemStack stack) {
        int current = stack.isDamageableItem() ? stack.getDamageValue() : 0;
        Integer previous = LAST_DAMAGE.put(slot, current);

        return previous != null
                && current > previous
                && shouldWarn(stack);
    }

    public static ItemStack getLowestArmorPiece(Player player) {
        ItemStack lowest = ItemStack.EMPTY;
        double lowestPercentage = Double.POSITIVE_INFINITY;

        for (ItemStack stack : ArmorHudBridge.getArmorItems(player)) {
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                continue;
            }

            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            double percentage = remaining / (double) stack.getMaxDamage();
            if (percentage < lowestPercentage) {
                lowestPercentage = percentage;
                lowest = stack;
            }
        }

        return lowest;
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
