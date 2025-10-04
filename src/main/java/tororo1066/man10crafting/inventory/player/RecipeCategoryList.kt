package tororo1066.man10crafting.inventory.player

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.CategoryDisplayItem
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class RecipeCategoryList: LargeSInventory("${Man10Crafting.prefix}§aレシピ一覧") {
    init {
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val list = Man10Crafting.recipes.values
            .filter { r -> r.enabled && !r.hidden && r.accessible(p) }
            .map { it.category }
            .distinct()

        val items = arrayListOf<SInventoryItem>()
        list.forEach { category ->
            val translated = translate("categories.$category")
            val categoryItem = Man10Crafting.categoryDisplayItems[category] ?: CategoryDisplayItem(ItemStack(Material.BOOK), false)
            val item = SInventoryItem(categoryItem.itemStack.clone())
                .apply {
                    if (!categoryItem.respectItemStackName) {
                        setDisplayName(translated)
                    }
                }
                .setCanClick(false)
                .setClickEvent {
                    moveChildInventory(RecipeList().apply {
                        setCategoryName(translated)
                    }, p)
                }
            items.add(item)
        }

        setResourceItems(items)
        return true
    }
}