package tororo1066.man10crafting.inventory.register.furnace

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import tororo1066.man10crafting.recipe.furnace.BlastingCraftingRecipe
import tororo1066.man10crafting.recipe.furnace.CampfireCraftingRecipe
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class CampfireRegister(
    currentData: CampfireCraftingRecipe? = null,
) : AbstractFurnaceRegister<CampfireCraftingRecipe>(
    "§cCampfire",
    currentData ?: CampfireCraftingRecipe(),
    currentData != null
) {

    init {
        setItem(
            22,
            SInventoryItem(Material.CAMPFIRE)
                .editRaw {
                    @Suppress("UnstableApiUsage")
                    it.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
                }
                .setCanClick(false)
        )
    }
}