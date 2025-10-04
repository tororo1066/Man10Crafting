package tororo1066.man10crafting.inventory.player

import org.bukkit.entity.Player
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.defaultMenus.CategorySInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class RecipeList: CategorySInventory("${Man10Crafting.prefix}§aレシピ一覧") {

    init {
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val recipesByResult = Man10Crafting.recipes.values
            .filter {
                it.enabled && !it.hidden && it.accessible(p)
            }.groupBy {
                it.result.clone().apply { amount = 1 }
            }.entries.sortedBy {
                it.value.minOf { r -> r.index }
            }
        val items = LinkedHashMap<String, ArrayList<SInventoryItem>>()
        recipesByResult.forEach { (result, recipes) ->
            val item = SInventoryItem(result)
                .setCanClick(false)
                .setClickEvent { _ ->
                    val view = RecipeView(p) { r -> r.result.isSimilar(result) }
                    if (view.isEmpty) return@setClickEvent
                    moveChildInventory(view, p)
                }
            val categories = recipes.map { it.category }.distinct()
            categories.forEach { category ->
                val transKey = translate("categories.$category")
                if (nowCategory.isEmpty()) nowCategory = transKey
                items.computeIfAbsent(transKey) { ArrayList() }.add(item)
            }
        }

        setResourceItems(items)
        return true
    }
}