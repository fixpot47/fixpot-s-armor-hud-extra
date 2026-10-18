package dev.fixpot47.armorhudextra.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.world.entity.HumanoidArm;
import net.uku3lig.ukulib.config.option.StringTranslatable;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArmorHudExtraConfig implements Serializable {
    private boolean enabled = true;
    private Anchor anchor = Anchor.HOTBAR;
    private Side side = Side.LEFT;
    private int offsetX = 0;
    private int offsetY = 0;
    private Style style = Style.HOTBAR;
    private Orientation orientation = Orientation.HORIZONTAL;
    private WidgetShown widgetShown = WidgetShown.NOT_EMPTY;
    private OffhandSlotBehavior offhandSlotBehavior = OffhandSlotBehavior.ADHERE;
    private DurabilityDisplay durabilityDisplay = DurabilityDisplay.PERCENTAGE;

    private boolean showMainHand = true;
    private boolean showOffhand = true;
    private boolean warnHeldItems = true;
    private boolean showFullDurability = true;

    private boolean pushBossbars = true;
    private boolean pushStatusEffectIcons = true;
    private boolean pushSubtitles = true;
    private boolean reversed = false;
    private boolean iconsShown = true;
    private boolean warningShown = true;
    private boolean playBreakSound = true;
    private int minDurabilityValue = 20;
    private double minDurabilityPercentage = 0.1;
    private int warningBobIntensity = 3;

    @Getter
    @AllArgsConstructor
    public enum Anchor implements StringTranslatable {
        TOP_CENTER("top_center", "armorhudextra.option.topCenter"),
        TOP("top", "armorhudextra.option.top"),
        BOTTOM("bottom", "armorhudextra.option.bottom"),
        HOTBAR("hotbar", "armorhudextra.option.hotbar");

        private final String name;
        private final String translationKey;

        public boolean isTop() {
            return this == TOP || this == TOP_CENTER;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Side implements StringTranslatable {
        LEFT("left", "options.mainHand.left"),
        RIGHT("right", "options.mainHand.right");

        private final String name;
        private final String translationKey;

        public Side getOpposite() {
            return this == LEFT ? RIGHT : LEFT;
        }

        public HumanoidArm asArm() {
            return this == LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum OffhandSlotBehavior implements StringTranslatable {
        ALWAYS_IGNORE("always_ignore", "armorhudextra.option.alwaysIgnore"),
        ADHERE("adhere", "armorhudextra.option.adhere"),
        ALWAYS_LEAVE_SPACE("always_leave_space", "armorhudextra.option.alwaysLeaveSpace");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum WidgetShown implements StringTranslatable {
        ALWAYS("always", "armorhudextra.option.always"),
        IF_ANY_PRESENT("if_any_present", "armorhudextra.option.ifAnyPresent"),
        NOT_EMPTY("not_empty", "armorhudextra.option.notEmpty"),
        DAMAGED_PIECES("damaged_pieces", "armorhudextra.option.damagedPieces");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum Style implements StringTranslatable {
        HOTBAR("hotbar", "armorhudextra.option.hotbar"),
        ROUNDED_CORNERS("rounded_corners", "armorhudextra.option.roundedCorners"),
        ROUNDED("rounded", "armorhudextra.option.rounded"),
        NONE("none", "armorhudextra.option.none");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum Orientation implements StringTranslatable {
        HORIZONTAL("horizontal", "armorhudextra.option.horizontal"),
        VERTICAL("vertical", "armorhudextra.option.vertical");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum DurabilityDisplay implements StringTranslatable {
        BAR("bar", "armorhudextra.option.bar"),
        NUMERIC("numeric", "armorhudextra.option.numeric"),
        PERCENTAGE("percentage", "armorhudextra.option.percentage"),
        EXACT("exact", "armorhudextra.option.exact");

        private final String name;
        private final String translationKey;
    }
}
