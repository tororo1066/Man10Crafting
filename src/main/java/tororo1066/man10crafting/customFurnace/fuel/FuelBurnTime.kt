package tororo1066.man10crafting.customFurnace.fuel

import org.bukkit.Material
import org.bukkit.Tag

object FuelBurnTime {

    val burnTimeByMaterial = mutableMapOf(
        Material.LAVA_BUCKET to 20000,
        Material.COAL_BLOCK to 16000,
        Material.BLAZE_ROD to 2400,
        Material.COAL to 1600,
        Material.CHARCOAL to 1600,
        Material.OAK_FENCE to 300,
        Material.BIRCH_FENCE to 300,
        Material.SPRUCE_FENCE to 300,
        Material.JUNGLE_FENCE to 300,
        Material.DARK_OAK_FENCE to 300,
        Material.ACACIA_FENCE to 300,
        Material.OAK_FENCE_GATE to 300,
        Material.BIRCH_FENCE_GATE to 300,
        Material.SPRUCE_FENCE_GATE to 300,
        Material.JUNGLE_FENCE_GATE to 300,
        Material.DARK_OAK_FENCE_GATE to 300,
        Material.ACACIA_FENCE_GATE to 300,
        Material.NOTE_BLOCK to 300,
        Material.BOOKSHELF to 300,
        Material.JUKEBOX to 300,
        Material.CHEST to 300,
        Material.TRAPPED_CHEST to 300,
        Material.CRAFTING_TABLE to 300,
        Material.DAYLIGHT_DETECTOR to 300,
        Material.BOW to 300,
        Material.FISHING_ROD to 300,
        Material.LADDER to 300,
        Material.WOODEN_SHOVEL to 200,
        Material.WOODEN_SWORD to 200,
        Material.WOODEN_HOE to 200,
        Material.WOODEN_AXE to 200,
        Material.WOODEN_PICKAXE to 200,
        Material.STICK to 100,
        Material.BOWL to 100,
        Material.DRIED_KELP_BLOCK to 4001,
        Material.CROSSBOW to 300,
        Material.BAMBOO to 50,
        Material.DEAD_BUSH to 100,
        Material.SCAFFOLDING to 400,
        Material.LOOM to 300,
        Material.BARREL to 300,
        Material.CARTOGRAPHY_TABLE to 300,
        Material.FLETCHING_TABLE to 300,
        Material.SMITHING_TABLE to 300,
        Material.COMPOSTER to 300,
        Material.AZALEA to 100,
        Material.FLOWERING_AZALEA to 100,
    )

    val burnTimeByTag = mutableMapOf<Tag<Material>, Int>(
        Tag.LOGS to 300,
        Tag.PLANKS to 300,
        Tag.WOODEN_STAIRS to 300,
        Tag.WOODEN_SLABS to 150,
        Tag.WOODEN_TRAPDOORS to 300,
        Tag.WOODEN_PRESSURE_PLATES to 300,
        Tag.BANNERS to 300,
        Tag.SIGNS to 200,
        Tag.WOODEN_DOORS to 200,
        Tag.ITEMS_BOATS to 1200,
        Tag.WOOL to 100,
        Tag.WOODEN_BUTTONS to 100,
        Tag.SAPLINGS to 100,
        Tag.WOOL_CARPETS to 67,
    )

    init {
        burnTimeByTag.forEach { (tag, burnTime) ->
            tag.values.forEach { material ->
                burnTimeByMaterial[material] = burnTime
            }
        }
    }

    fun getBurnTime(material: Material): Int? {
        return burnTimeByMaterial[material]
    }
}