package tororo1066.man10crafting.inventory.player

import org.bukkit.entity.Player
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class AutoPlaceMenu(private val p: Player): LargeSInventory("${Man10Crafting.prefix}§cレシピを選択") {

    init {
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(): Boolean {
        val filteredRecipes = Man10Crafting.recipes.filter {
            it.value.checkNeed(p) && RecipeMenu.creatable(p, it.value)
        }.entries.sortedBy { it.value.index }
        val items = ArrayList<SInventoryItem>()
        filteredRecipes.forEach { (_, data) ->
            val item = SInventoryItem(data.result)
                .setCanClick(false)
                .setClickEvent {
                    throughClose(p)
                    RecipeMenu.setRecipe(p, data, it.isShiftClick)
                }
            items.add(item)
        }
        setResourceItems(items)

        return true
    }
}