package org.btuk.pcontrol;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;

public interface VersionsAdapter {

    @Nonnull
    Material getFallingBlockMaterial(@Nonnull FallingBlock fallingBlock);

    boolean isBoneMealItem(@Nonnull ItemStack stack);

    boolean isBlockContainsWater(@Nonnull Block block);

    boolean isFacingAt(@Nonnull Block block, @Nonnull BlockFace face);

    void setItemMetaGlowing(@Nonnull ItemMeta meta);

}
