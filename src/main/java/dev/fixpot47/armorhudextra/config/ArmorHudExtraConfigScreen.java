package dev.fixpot47.armorhudextra.config;

import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import net.minecraft.client.gui.screens.Screen;
import net.uku3lig.ukulib.config.option.CyclingOption;
import net.uku3lig.ukulib.config.option.SliderOption;
import net.uku3lig.ukulib.config.option.TypedInputOption;
import net.uku3lig.ukulib.config.option.WidgetCreator;
import net.uku3lig.ukulib.config.screen.AbstractConfigScreen;

import java.util.Optional;

public class ArmorHudExtraConfigScreen extends AbstractConfigScreen<ArmorHudExtraConfig> {
    protected ArmorHudExtraConfigScreen(Screen parent) {
        super("armorhudextra.config", parent, ArmorHudExtraMod.getManager());
    }

    @Override
    protected WidgetCreator[] getWidgets(ArmorHudExtraConfig config) {
        return new WidgetCreator[]{
                CyclingOption.ofBoolean("armorhudextra.option.enabled", config.isEnabled(), config::setEnabled),
                CyclingOption.ofBoolean("armorhudextra.option.showMainHand", config.isShowMainHand(), config::setShowMainHand),
                CyclingOption.ofBoolean("armorhudextra.option.showOffhand", config.isShowOffhand(), config::setShowOffhand),
                CyclingOption.ofTranslatableEnum(
                        "armorhudextra.option.heldItemDurability",
                        ArmorHudExtraConfig.DurabilityDisplay.class,
                        config.getHeldItemDurability(),
                        config::setHeldItemDurability
                ),
                CyclingOption.ofBoolean(
                        "armorhudextra.option.showFullDurability",
                        config.isShowFullDurability(),
                        config::setShowFullDurability
                ),
                CyclingOption.ofBoolean(
                        "armorhudextra.option.showLowestArmorPercentage",
                        config.isShowLowestArmorPercentage(),
                        config::setShowLowestArmorPercentage
                ),
                CyclingOption.ofBoolean(
                        "armorhudextra.option.warnHeldItems",
                        config.isWarnHeldItems(),
                        config::setWarnHeldItems
                ),
                CyclingOption.ofBoolean(
                        "armorhudextra.option.playWarningSound",
                        config.isPlayWarningSound(),
                        config::setPlayWarningSound
                ),
                new TypedInputOption<>(
                        "armorhudextra.option.minDuraValue",
                        String.valueOf(config.getMinDurabilityValue()),
                        config::setMinDurabilityValue,
                        this::getInt
                ),
                new SliderOption(
                        "armorhudextra.option.minDuraPercent",
                        config.getMinDurabilityPercentage(),
                        config::setMinDurabilityPercentage,
                        SliderOption.PERCENT_VALUE_TO_TEXT
                )
        };
    }

    private Optional<Integer> getInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
