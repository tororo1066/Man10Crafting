package tororo1066.man10crafting.inventory.register.furnace

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import tororo1066.man10crafting.recipe.furnace.BlastingCraftingRecipe
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class BlastingRegister(
    currentData: BlastingCraftingRecipe? = null,
) : AbstractFurnaceRegister<BlastingCraftingRecipe>(
    "§cBlasting",
    currentData ?: BlastingCraftingRecipe(),
    currentData != null
) {

    init {
        setItem(
            22,
            SInventoryItem(Material.BLAST_FURNACE)
                .editRaw {
                    @Suppress("UnstableApiUsage")
                    it.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
                }
                .setCanClick(false)
        )
    }
}