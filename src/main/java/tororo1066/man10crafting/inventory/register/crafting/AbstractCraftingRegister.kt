package tororo1066.man10crafting.inventory.register.crafting

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.ingredient.ItemStackIngredient
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.Items
import tororo1066.man10crafting.inventory.register.Items.emptyIngredientItem
import tororo1066.man10crafting.inventory.register.Items.optionsItem
import tororo1066.man10crafting.inventory.register.Items.setAdvancedModeItem
import tororo1066.man10crafting.recipe.crafting.AbstractCraftingRecipe

abstract class AbstractCraftingRegister<T: AbstractCraftingRecipe>(
    name: String,
    currentData: T,
    isEdit: Boolean
): AbstractRegister<T>(
    name,
    currentData,
    isEdit
) {

    val craftingSlots = listOf(10,11,12,19,20,21,28,29,30)

    val ingredients = HashMap<Int, Pair<AbstractIngredient, Int>>()

    override val task = object : BukkitRunnable() {
        override fun run() {
            if (!advancedMode) return
            ingredients.forEach { (slot, pair) ->
                val ingredient = pair.first
                val nextIndex = ingredient.getNextIndex(pair.second)
                ingredients[slot] = Pair(ingredient, nextIndex)
                setItem(slot, editItem(ingredient, nextIndex, slot))
            }
        }
    }.runTaskTimer(plugin, 20, 20)

    private fun editItem(ingredient: AbstractIngredient, index: Int, slot: Int) =
        ingredient.editItem(this, index) { p, newIngredient ->
            if (newIngredient == null) {
                ingredients.remove(slot)
            } else if (newIngredient != ingredient) {
                ingredients[slot] = Pair(newIngredient, 0)
            }
            allRenderMenu(p)
        }

    override fun readInput() {
        ingredients.clear()
        craftingSlots.forEach { slot ->
            val item = inv.getItem(slot) ?: return@forEach
            val ingredient = ItemStackIngredient().apply {
                itemStacks.add(item.clone())
            }
            ingredients[slot] = Pair(ingredient,0)
        }
    }

    override fun prepareSave(): Boolean {
        if (ingredients.isEmpty()) return false
        if (ingredients.values.any { !it.first.isValid() }) return false
        val result = inv.getItem(24) ?: return false

        currentData.result = result

        return true
    }

    //0  1  2  3  k  5  6  7  8
    //9  s  s  s  13 14 15 16 17
    //18 s  s  s  a  23 e  25 26
    //27 s  s  s  31 32 33 34 35
    //36 37 38 39 40 41 42 43 b
    // s = craftingSlots
    // k = options
    // a = advanced mode
    // e = crafting result
    // b = save button

    init {
        fillItem(Items.backgroundLightBlue())
        removeItems(craftingSlots + listOf(24))

        val result = this.currentData.getResultOrNull()
        if (result != null) {
            inv.setItem(24, result.clone())
        }
    }

    override fun renderMenu(p: Player): Boolean {

        val onlySupportedAdvanced = onlySupportedAdvanced(ingredients.values.map { it.first })
        if (onlySupportedAdvanced) advancedMode = true

        setAdvancedModeItem(22, onlySupportedAdvanced)

        if (advancedMode) {
            craftingSlots.forEach {
                val ingredient = ingredients[it]
                if (ingredient == null) {
                    setItem(it, emptyIngredientItem { ingredient ->
                        if (ingredient != null) {
                            ingredients[it] = Pair(ingredient, 0)
                            allRenderMenu(p)
                        }
                    })
                } else {
                    setItem(it, editItem(ingredient.first, ingredient.second, it))
                }
            }

        } else {
            removeItems(craftingSlots)

            ingredients.forEach { (slot, pair) ->
                val ingredient = pair.first
                if (ingredient !is ItemStackIngredient) {
                    ingredients.remove(slot)
                } else {
                    inv.setItem(slot, ingredient.displayItems.first().clone())
                }
            }
        }

        setItem(4, optionsItem(currentData))

        setItem(44, saveItem())

        return true
    }
}