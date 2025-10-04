package tororo1066.man10crafting.inventory.register.furnace

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import tororo1066.man10crafting.recipe.furnace.FurnaceCraftingRecipe
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class FurnaceRegister(
    currentData: FurnaceCraftingRecipe? = null,
) : AbstractFurnaceRegister<FurnaceCraftingRecipe>(
    "§cFurnace",
    currentData ?: FurnaceCraftingRecipe(),
    currentData != null
) {

    init {
        setItem(
            22,
            SInventoryItem(Material.FURNACE)
                .editRaw {
                    @Suppress("UnstableApiUsage")
                    it.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
                }
                .setCanClick(false)
        )
    }
}