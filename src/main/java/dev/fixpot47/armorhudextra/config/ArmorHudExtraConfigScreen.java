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
                CyclingOption.ofBoolean("armorhudextra.option.warnHeldItems", config.isWarnHeldItems(), config::setWarnHeldItems),
                CyclingOption.ofBoolean("armorhudextra.option.showFullDurability", config.isShowFullDurability(), config::setShowFullDurability),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.anchor", ArmorHudExtraConfig.Anchor.class, config.getAnchor(), config::setAnchor),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.side", ArmorHudExtraConfig.Side.class, config.getSide(), config::setSide),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.style", ArmorHudExtraConfig.Style.class, config.getStyle(), config::setStyle),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.orientation", ArmorHudExtraConfig.Orientation.class, config.getOrientation(), config::setOrientation),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.widgetShown", ArmorHudExtraConfig.WidgetShown.class, config.getWidgetShown(), config::setWidgetShown),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.offhandSlotBehavior", ArmorHudExtraConfig.OffhandSlotBehavior.class, config.getOffhandSlotBehavior(), config::setOffhandSlotBehavior),
                CyclingOption.ofTranslatableEnum("armorhudextra.option.durabilityDisplay", ArmorHudExtraConfig.DurabilityDisplay.class, config.getDurabilityDisplay(), config::setDurabilityDisplay),
                CyclingOption.ofBoolean("armorhudextra.option.pushBossbars", config.isPushBossbars(), config::setPushBossbars),
                CyclingOption.ofBoolean("armorhudextra.option.pushIcons", config.isPushStatusEffectIcons(), config::setPushStatusEffectIcons),
                CyclingOption.ofBoolean("armorhudextra.option.pushSubtitles", config.isPushSubtitles(), config::setPushSubtitles),
                CyclingOption.ofBoolean("armorhudextra.option.reversed", config.isReversed(), config::setReversed),
                CyclingOption.ofBoolean("armorhudextra.option.showIcons", config.isIconsShown(), config::setIconsShown),
                CyclingOption.ofBoolean("armorhudextra.option.playBreakSound", config.isPlayBreakSound(), config::setPlayBreakSound),
                CyclingOption.ofBoolean("armorhudextra.option.showWarning", config.isWarningShown(), config::setWarningShown),
                new TypedInputOption<>("armorhudextra.option.iconBobIntensity", String.valueOf(config.getWarningBobIntensity()), config::setWarningBobIntensity, this::getInt),
                new TypedInputOption<>("armorhudextra.option.minDuraValue", String.valueOf(config.getMinDurabilityValue()), config::setMinDurabilityValue, this::getInt),
                new SliderOption("armorhudextra.option.minDuraPercent", config.getMinDurabilityPercentage(), config::setMinDurabilityPercentage, SliderOption.PERCENT_VALUE_TO_TEXT),
                new TypedInputOption<>("armorhudextra.option.offsetX", String.valueOf(config.getOffsetX()), config::setOffsetX, this::getInt),
                new TypedInputOption<>("armorhudextra.option.offsetY", String.valueOf(config.getOffsetY()), config::setOffsetY, this::getInt)
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
