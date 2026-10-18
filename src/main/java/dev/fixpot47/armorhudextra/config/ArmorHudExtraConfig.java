package dev.fixpot47.armorhudextra.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.uku3lig.ukulib.config.option.StringTranslatable;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArmorHudExtraConfig implements Serializable {
    private boolean enabled = true;

    private boolean showMainHand = true;
    private boolean showOffhand = true;
    private DurabilityDisplay heldItemDurability = DurabilityDisplay.PERCENTAGE;
    private boolean showFullDurability = false;

    private boolean warnHeldItems = true;
    private boolean playWarningSound = true;
    private int minDurabilityValue = 20;
    private double minDurabilityPercentage = 0.10;

    private boolean showLowestArmorPercentage = true;

    @Getter
    @AllArgsConstructor
    public enum DurabilityDisplay implements StringTranslatable {
        NONE("none", "armorhudextra.option.none"),
        REMAINING("remaining", "armorhudextra.option.remaining"),
        PERCENTAGE("percentage", "armorhudextra.option.percentage"),
        EXACT("exact", "armorhudextra.option.exact");

        private final String name;
        private final String translationKey;
    }
}
