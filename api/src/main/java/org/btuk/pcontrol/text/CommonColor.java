package org.btuk.pcontrol.text;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class CommonColor {

    @SuppressWarnings("unused")
    @Nonnull
    public static CommonColor rgb(int rgb) {
        return new CommonColor(new Color(rgb));
    }

    @SuppressWarnings("unused")
    @Nonnull
    public static CommonColor rgb(@Nonnull Color awtColor) {
        return new CommonColor(awtColor);
    }

    public static final CommonColor
        BLACK = new CommonColor("black", 0x000000),
        DARK_BLUE = new CommonColor("dark_blue", 0x0000AA),
        DARK_GREEN = new CommonColor("dark_green", 0x00AA00),
        DARK_AQUA = new CommonColor("dark_aqua", 0x00AAAA),
        DARK_RED = new CommonColor("dark_red", 0xAA0000),
        DARK_PURPLE = new CommonColor("dark_purple", 0xAA00AA),
        GOLD = new CommonColor("gold", 0xFFAA00),
        GRAY = new CommonColor("gray", 0xAAAAAA),
        DARK_GRAY = new CommonColor("dark_gray", 0x555555),
        BLUE = new CommonColor("blue", 0x5555FF),
        GREEN = new CommonColor("green", 0x55FF55),
        AQUA = new CommonColor("aqua", 0x55FFFF),
        RED = new CommonColor("red", 0xFF5555),
        LIGHT_PURPLE = new CommonColor("light_purple", 0xFF55FF),
        YELLOW = new CommonColor("yellow", 0xFFFF55),
        WHITE = new CommonColor("white", 0xFFFFFF);

    private static final CommonColor[] NAMED_COLORS = new CommonColor[]{
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA,
        DARK_RED, DARK_PURPLE, GOLD, GRAY,
        DARK_GRAY, BLUE, GREEN, AQUA,
        RED, LIGHT_PURPLE, YELLOW, WHITE
    };

    private final String name;
    private final Color awtColor;

    private CommonColor(@Nonnull String name, int rgb) {
        this.name = name;
        this.awtColor = new Color(rgb);
    }

    private CommonColor(@Nonnull Color awtColor) {
        this.name = null;
        this.awtColor = awtColor;
    }

    @Nullable
    public String name() {
        return this.name;
    }

    @Nonnull
    public String nearestName() {
        if (this.name != null) return this.name;
        return nearestNamedTo(this).name;
    }

    @Nonnull
    public Color awtColor() {
        return this.awtColor;
    }

    @Nonnull
    private static CommonColor nearestNamedTo(@Nonnull CommonColor any) {
        float matchedDistance = Float.MAX_VALUE;
        CommonColor match = NAMED_COLORS[0];
        int i = 0;

        for (int length = NAMED_COLORS.length; i < length; ++i) {
            CommonColor potential = NAMED_COLORS[i];

            float distance = distance(any.awtColor, potential.awtColor);
            if (distance < matchedDistance) {
                match = potential;
                matchedDistance = distance;
            }

            if (distance == 0.0F) {
                break;
            }
        }

        return match;
    }

    private static float distance(@Nonnull Color first, @Nonnull Color second) {
        float[] firstHSB = Color.RGBtoHSB(first.getRed(), first.getGreen(), first.getBlue(), null);
        float[] secondHSB = Color.RGBtoHSB(second.getRed(), second.getGreen(), second.getBlue(), null);
        float hueDistance = 3.0F * Math.min(Math.abs(firstHSB[0] - secondHSB[0]), 1.0F - Math.abs(firstHSB[0] - secondHSB[0]));
        float saturationDiff = firstHSB[1] - secondHSB[1];
        float brightnessDiff = firstHSB[2] - secondHSB[2];
        return (hueDistance * hueDistance) + (saturationDiff * saturationDiff) + (brightnessDiff * brightnessDiff);
    }
}
