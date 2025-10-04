package tororo1066.man10crafting.inventory.register.furnace

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import tororo1066.man10crafting.recipe.furnace.SmokingCraftingRecipe
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class SmokingRegister(
    currentData: SmokingCraftingRecipe? = null,
) : AbstractFurnaceRegister<SmokingCraftingRecipe>(
    "§cSmoking",
    currentData ?: SmokingCraftingRecipe(),
    currentData != null
) {

    init {
        setItem(
            22,
            SInventoryItem(Material.SMOKER)
                .editRaw {
                    @Suppress("UnstableApiUsage")
                    it.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
                }
                .setCanClick(false)
        )
    }
}