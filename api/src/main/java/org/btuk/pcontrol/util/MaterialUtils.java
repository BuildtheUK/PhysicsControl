package org.btuk.pcontrol.util;

import org.bukkit.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MaterialUtils {
    private static final Set<Material> AIR_MATERIALS = new HashSet<>();

    static {
        AIR_MATERIALS.add(Material.AIR);
        AIR_MATERIALS.add(Material.CAVE_AIR);
        AIR_MATERIALS.add(Material.VOID_AIR);
    }

    @Nonnull
    public static Set<PCMaterial> getItemMaterials(boolean allowAir, @Nullable Consumer<String> onFail, @Nonnull String... names) {
        return getMaterials(material -> material.isItemMaterial(allowAir), onFail, names);
    }

    @Nonnull
    public static Set<PCMaterial> getBlockMaterials(boolean allowAir, @Nullable Consumer<String> onFail, @Nonnull String... names) {
        return getMaterials(material -> material.isBlockMaterial(allowAir), onFail, names);
    }

    @Nonnull
    public static Set<PCMaterial> getValidMaterials(boolean allowAir, @Nullable Consumer<String> onFail, @Nonnull String... names) {
        return getMaterials(material -> material.isValidMaterial(allowAir), onFail, names);
    }

    public static boolean isItemMaterial(@Nullable Material material, boolean allowAir) {
        if (material == null || isLegacyMaterial(material)) return false;
        if (!material.isItem()) return false;
        return allowAir || !isAirMaterial(material);
    }

    public static boolean isBlockMaterial(@Nullable Material material, boolean allowAir) {
        if (material == null || isLegacyMaterial(material)) return false;
        if (!material.isBlock()) return false;
        return allowAir || !isAirMaterial(material);
    }

    public static boolean isValidMaterial(@Nullable Material material, boolean allowAir) {
        if (material == null || isLegacyMaterial(material)) return false;
        return allowAir || !isAirMaterial(material);
    }

    public static boolean isAirMaterial(@Nullable Material material) {
        return material == null || AIR_MATERIALS.contains(material);
    }

    public static boolean isLegacyMaterial(@Nonnull Material material) {
        return material.name().startsWith("LEGACY_");
    }

    @Nonnull
    public static Set<PCMaterial> getMaterials(@Nonnull Predicate<PCMaterial> filter,
                                               @Nullable Consumer<String> onFail,
                                               @Nonnull String... names
    ) {
        Set<PCMaterial> result = new HashSet<>();
        for (String name : names) {
            PCMaterial material = PCMaterial.getMaterial(name);
            if (material != null
                && !material.isAirMaterial()
                && !material.isLegacyMaterial()
                && filter.test(material)
            ) {
                result.add(material);
            } else if (onFail != null) {
                onFail.accept(name);
            }
        }
        return result;
    }

}
