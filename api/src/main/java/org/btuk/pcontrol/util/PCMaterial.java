package org.btuk.pcontrol.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class PCMaterial {
    @Nullable
    public static PCMaterial getMaterial(@Nonnull String name) {
        Material result = Material.getMaterial(name.toUpperCase());
        return result == null ? null : new PCMaterial(result, null);
    }

    @Nonnull
    public static PCMaterial valueOf(@Nonnull String name) {
        PCMaterial result = getMaterial(name);
        if (result == null) throw new IllegalArgumentException(name);
        return result;
    }

    @Nonnull
    public static PCMaterial ofLegacyOrModern(@Nonnull String legacyName, @Nonnull String modernName) {
        PCMaterial result = getMaterial(modernName);
        if (result == null) {
            new IllegalArgumentException(
                "Unable to find modern material " + modernName + ", " +
                    "trying to use BARRIER..."
            ).printStackTrace();
            result = new PCMaterial(Material.BARRIER, null);
        }
        return result;
    }

    private final Material material;
    private final Byte data;
    private final int hashCode;
    private final String toString;

    public PCMaterial(@Nonnull Material material, @Nullable Byte data) {
        this.material = material;
        this.data = null;

        this.hashCode = this.material.ordinal() * (Byte.MAX_VALUE + 1) + Byte.MAX_VALUE;
        this.toString = this.material.name();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(@Nonnull Object other) {
        if (other instanceof PCMaterial) return equals((PCMaterial) other);
        if (other instanceof Material) return equals((Material) other);
        if (other instanceof Block) return equals((Block) other);
        if (other instanceof ItemStack) return equals((ItemStack) other);
        return false;
    }

    public boolean equals(@Nonnull PCMaterial other) {
        return other.material == this.material && Objects.equals(other.data, this.data);
    }

    public boolean equals(@Nonnull Material material) {
        return this.material == material;
    }

    public boolean equals(@Nonnull Block block) {
        return block.getType() == this.material;
    }

    public boolean equals(@Nonnull ItemStack stack) {
        return stack.getType() == this.material;
    }

    @Override
    public String toString() {
        return this.toString;
    }

    @Nonnull
    public ItemStack createStack(int amount) {
        return new ItemStack(this.material, amount);
    }

    public boolean isItemMaterial(boolean allowAir) {
        return MaterialUtils.isItemMaterial(this.material, allowAir);
    }

    public boolean isBlockMaterial(boolean allowAir) {
        return MaterialUtils.isBlockMaterial(this.material, allowAir);
    }

    public boolean isValidMaterial(boolean allowAir) {
        return MaterialUtils.isValidMaterial(this.material, allowAir);
    }

    public boolean isAirMaterial() {
        return MaterialUtils.isAirMaterial(this.material);
    }

    public boolean isLegacyMaterial() {
        return MaterialUtils.isLegacyMaterial(this.material);
    }

    @Nonnull
    public Material getType() {
        return this.material;
    }
}
