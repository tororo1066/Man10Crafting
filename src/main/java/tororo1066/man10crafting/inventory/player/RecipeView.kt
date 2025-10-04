package tororo1066.man10crafting.inventory.player

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.inventory.register.Items
import tororo1066.man10crafting.recipe.AbstractRecipe
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.defaultMenus.PagedSInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class RecipeView(
    val player: Player,
    filter: (recipe: AbstractRecipe) -> Boolean
): PagedSInventory("${Man10Crafting.prefix}§6レシピ一覧", 6) {

    init {
        setOnClick {
            it.isCancelled = true
        }
    }

    val isEmpty: Boolean

    // page, slot, (ingredient, index)
    val currentIngredients = HashMap<Int, HashMap<Int, Pair<AbstractIngredient, Int>>>()

    val task = object : BukkitRunnable() {
        override fun run() {
            val ingredients = currentIngredients[nowPage] ?: return
            ingredients.forEach { (slot, pair) ->
                val ingredient = pair.first
                val nextIndex = ingredient.getNextIndex(pair.second)
                setItem(slot, createIngredientItem(nowPage, slot, ingredient, nextIndex))
            }
        }
    }.runTaskTimer(SJavaPlugin.plugin, 20, 20)

    init {

        setOnClose {
            task.cancel()
        }

        setLeftSlots((45..47).toList())
        setRightSlots((51..53).toList())

        val recipes = Man10Crafting.recipes.values.filter { r -> filter(r) && r.enabled && !r.hidden && r.accessible(player) }

        isEmpty = recipes.isEmpty()

        recipes.forEachIndexed { index, recipe ->
            val inv = object : SInventory(SJavaPlugin.plugin, "${Man10Crafting.prefix}§aレシピ詳細", 6) {

                init {
                    setOnClick {
                        it.isCancelled = true
                    }

                    fillItem(Items.backgroundWhite())
                    setItems(9..35, Items.backgroundGray())
                    recipe.renderRecipeView(
                        inventory = this,
                        setIngredientItem = { slot, ingredient ->
                            setItem(slot, createIngredientItem(index, slot, ingredient, 0))
                        },
                        setResultItem = { slot, itemStack ->
                            setItem(
                                slot, SInventoryItem(itemStack.clone())
                                    .setCanClick(false)
                                    .addLore(
                                        "",
                                        "§e§l[左クリック] §fこのアイテムのレシピを見る",
                                        "§e§l[右クリック] §fこのアイテムを使って作れるレシピを見る",
                                    )
                                    .setClickEvent { e ->
                                        recipeClickEvent(itemStack, e)
                                    }
                            )
                        }
                    )
                }
            }

            addPage(inv)
        }
    }

    private fun createIngredientItem(page: Int, slot: Int, ingredient: AbstractIngredient, index: Int): SInventoryItem {
        val displayItem = ingredient.displayItems[index]
        val item = SInventoryItem(displayItem.clone())
            .addLore(
                "",
                "§e§l[左クリック] §fこのアイテムのレシピを見る",
                "§e§l[右クリック] §fこのアイテムを使って作れるレシピを見る"
            )
            .apply {
                if (ingredient.displayItems.size > 1) {
                    addLore("§e§l[SHIFT + 左クリック] §f使用可能なアイテムを見る")
                }
            }
            .setCanClick(false)
            .setClickEvent { e ->
                val player = e.whoClicked as Player

                recipeClickEvent(displayItem, e)

                if (e.click == ClickType.SHIFT_LEFT && ingredient.displayItems.size > 1) {
                    val view = object : LargeSInventory("${Man10Crafting.prefix}§b使用可能なアイテム一覧") {
                        init {
                            setOnClick {
                                it.isCancelled = true
                            }
                        }

                        override fun renderMenu(p: Player): Boolean {
                            val items = ArrayList<SInventoryItem>()
                            ingredient.displayItems.forEach { item ->
                                items.add(
                                    SInventoryItem(item)
                                        .setCanClick(false)
                                        .setClickEvent { e2 ->
                                            recipeClickEvent(item, e2)
                                        }
                                )
                            }

                            setResourceItems(items)
                            return true
                        }
                    }
                    moveChildInventory(view, player)
                }
            }

        currentIngredients.computeIfAbsent(page) { HashMap() }[slot] = Pair(ingredient, index)
        return item
    }

    private fun recipeClickEvent(item: ItemStack, e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        when (e.click) {
            ClickType.LEFT -> { //そのアイテムのレシピを見る
                val view = RecipeView(player) { r ->
                    r.result.isSimilar(item)
                }
                if (!view.isEmpty) moveChildInventory(view, player)
            }
            ClickType.RIGHT -> { //そのアイテムを使って作れるレシピを見る
                val view = RecipeView(player) { r ->
                    r.getIngredients().any { it.isSimilar(item) }
                }
                if (!view.isEmpty) moveChildInventory(view, player)
            }
            else -> {}
        }
    }
}