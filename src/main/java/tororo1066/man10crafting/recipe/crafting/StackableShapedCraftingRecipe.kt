package tororo1066.man10crafting.recipe.crafting

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.CraftingInventory
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.recipe.RecipeDeserializer
import tororo1066.man10crafting.recipe.options.CommandExecutable
import tororo1066.man10crafting.recipe.options.Permissible
import kotlin.collections.iterator
import kotlin.math.min

class StackableShapedCraftingRecipe: ShapedCraftingRecipe() {

    override fun craftable(inventory: CraftingInventory): Boolean {
        shape.forEachIndexed { i, strings ->
            strings.forEachIndexed second@ { j, c ->
                if (c == ' ')return@second
                val item = inventory.getItem((i * 3 + j) + 1)?:return false
                val ingredient = ingredients[c] ?: return false
                if (!ingredient.validate(item)) {
                    return false
                }
                val ingredientAmount = ingredient.getAmount(item) ?: return false
                if (item.amount < ingredientAmount) {
                    return false
                }
            }
        }
        return true
    }

    override fun performCraft(event: CraftItemEvent): Int {
        event.isCancelled = true

        var amount = 64
        if (event.isShiftClick) {
            shape.forEachIndexed { i, strings ->
                strings.forEachIndexed second@ { j, c ->
                    if (c == ' ')return@second
                    val item = event.inventory.getItem((i * 3 + j) + 1)?:return@forEachIndexed
                    val ingredient = ingredients[c] ?: return@forEachIndexed
                    val ingredientAmount = ingredient.getAmount(item) ?: return@forEachIndexed
                    amount = min(amount, item.amount / ingredientAmount)
                }
            }
        } else {
            amount = 1
        }

        var remainItemAmount = 0
        if (event.isShiftClick) {
            val addItem = event.whoClicked.inventory.addItem(result.clone().apply {
                this.amount *= amount
            })
            remainItemAmount = addItem.values.sumOf { it.amount }
        } else {
            val cursor = event.view.cursor
            if (!cursor.isEmpty) {
                if (!cursor.isSimilar(result)) return 0
                if (cursor.amount + result.amount * amount > cursor.maxStackSize) return 0
                cursor.amount += result.amount * amount
            } else {
                event.view.setCursor(result.clone().apply {
                    this.amount *= amount
                })
            }
        }

        shape.forEachIndexed { i, strings ->
            strings.forEachIndexed second@ { j, c ->
                if (c == ' ')return@second
                val item = event.inventory.getItem((i * 3 + j) + 1)?:return@forEachIndexed
                val ingredient = ingredients[c] ?: return@forEachIndexed
                val ingredientAmount = ingredient.getAmount(item) ?: return@forEachIndexed
                item.amount -= ingredientAmount * (amount - remainItemAmount)
            }
        }

        val craftedAmount = amount - remainItemAmount
        if (craftedAmount > 0) executeCommands(event.whoClicked)
        return craftedAmount
    }

    companion object: RecipeDeserializer<StackableShapedCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): StackableShapedCraftingRecipe? {
            val recipe = StackableShapedCraftingRecipe()
            val shape = section.getStringList("shape")
            recipe.shape = shape

            val ingredientsSection = section.getConfigurationSection("ingredients") ?: return null
            val ingredients = ingredientsSection.getKeys(false)
            for (key in ingredients) {
                if (key.length != 1) return null
                val value = ingredientsSection.get(key) as? AbstractIngredient ?: return null
                recipe.ingredients[key[0]] = value
            }

            recipe.permission = Permissible.deserialize(section)
            recipe.commands = CommandExecutable.deserialize(section)
            return recipe
        }
    }
}