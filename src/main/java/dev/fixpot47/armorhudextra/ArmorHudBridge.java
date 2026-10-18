package dev.fixpot47.armorhudextra;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Small reflection bridge into fixpot's Armor HUD.
 *
 * Armor HUD Extra requires the base mod at runtime. Reflection keeps this add-on
 * build independent from publishing the base mod to a Maven repository while
 * still using the base mod's live layout and configuration.
 */
public final class ArmorHudBridge {
    private static final String BASE_CLASS = "ru.berdinskiybear.armorhud.ArmorHudMod";

    private ArmorHudBridge() {
    }

    @SuppressWarnings("unchecked")
    public static Optional<Rect2i> getWidgetRect(GuiGraphicsExtractor graphics, Player player) {
        try {
            Class<?> clazz = Class.forName(BASE_CLASS);
            Method method = clazz.getMethod("getWidgetRect", GuiGraphicsExtractor.class, Player.class);
            return (Optional<Rect2i>) method.invoke(null, graphics, player);
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ItemStack> getArmorItems(Player player) {
        try {
            Class<?> clazz = Class.forName(BASE_CLASS);
            Method method = clazz.getMethod("getArmorItems", Player.class);
            return (List<ItemStack>) method.invoke(null, player);
        } catch (ReflectiveOperationException e) {
            return Collections.emptyList();
        }
    }

    public static boolean isBaseEnabled() {
        Object config = getBaseConfig();
        return config != null && booleanValue(config, "isEnabled", true);
    }

    public static String orientation() {
        return enumName(getBaseConfig(), "getOrientation", "HORIZONTAL");
    }

    public static String anchor() {
        return enumName(getBaseConfig(), "getAnchor", "HOTBAR");
    }

    public static String side() {
        return enumName(getBaseConfig(), "getSide", "LEFT");
    }

    public static String style() {
        return enumName(getBaseConfig(), "getStyle", "HOTBAR");
    }

    public static boolean reversed() {
        Object config = getBaseConfig();
        return config != null && booleanValue(config, "isReversed", false);
    }

    private static Object getBaseConfig() {
        try {
            Class<?> clazz = Class.forName(BASE_CLASS);
            Object manager = clazz.getMethod("getManager").invoke(null);
            return manager.getClass().getMethod("getConfig").invoke(manager);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static String enumName(Object config, String method, String fallback) {
        if (config == null) return fallback;
        try {
            Object value = config.getClass().getMethod(method).invoke(config);
            return value instanceof Enum<?> enumValue ? enumValue.name() : fallback;
        } catch (ReflectiveOperationException e) {
            return fallback;
        }
    }

    private static boolean booleanValue(Object config, String method, boolean fallback) {
        try {
            Object value = config.getClass().getMethod(method).invoke(config);
            return value instanceof Boolean bool ? bool : fallback;
        } catch (ReflectiveOperationException e) {
            return fallback;
        }
    }
}
