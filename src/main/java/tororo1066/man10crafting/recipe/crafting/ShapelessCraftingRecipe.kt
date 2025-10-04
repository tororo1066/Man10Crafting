package tororo1066.man10crafting.recipe.crafting

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapelessRecipe
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.crafting.ShapelessCraftingRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer
import tororo1066.man10crafting.recipe.options.CommandExecutable
import tororo1066.man10crafting.recipe.options.Permissible
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class ShapelessCraftingRecipe: AbstractCraftingRecipe() {

    val ingredients = ArrayList<AbstractIngredient>()

    override fun createBukkitRecipe(): Recipe {
        val recipe = ShapelessRecipe(namespacedKey, result)
        for (ingredient in ingredients) {
            recipe.addIngredient(ingredient.createBukkitRecipeChoice())
        }
        return recipe
    }

    override fun craftable(inventory: CraftingInventory): Boolean {
        val matrix = inventory.matrix.toMutableList()
        for (ingredient in ingredients) {
            var found = false
            for (i in matrix.indices) {
                val item = matrix[i] ?: continue
                if (ingredient.validate(item)) {
                    found = true
                    matrix[i] = null
                    break
                }
            }
            if (!found) return false
        }
        return true
    }

    override fun getIngredients(): List<AbstractIngredient> {
        return ingredients
    }

    override fun SInventory.inlineRenderRecipeView(
        setIngredientItem: (slot: Int, ingredient: AbstractIngredient) -> Unit,
        setResultItem: (slot: Int, itemStack: ItemStack) -> Unit
    ) {
        setItem(
            23,
            SInventoryItem(Material.CRAFTING_TABLE)
                .setDisplayName("§a非定型レシピ")
                .setCanClick(false)
        )

        val slots = listOf(10,11,12,19,20,21,28,29,30)
        removeItems(slots)
        for (i in ingredients.indices) {
            if (i >= slots.size) break
            setIngredientItem(slots[i], ingredients[i])
        }

        setResultItem(25, result)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return ShapelessCraftingRegister(this)
    }

    override fun serialize(): ConfigurationSection {
        val section = super.serialize()
        section.set("ingredients", ingredients)
        return section
    }

    companion object: RecipeDeserializer<ShapelessCraftingRecipe> {
        @Suppress("UNCHECKED_CAST")
        override fun deserialize(section: ConfigurationSection): ShapelessCraftingRecipe? {
            val recipe = ShapelessCraftingRecipe()

            val ingredients = section.getList("ingredients") as? List<AbstractIngredient>
                ?: return null
            recipe.ingredients.addAll(ingredients)

            recipe.permission = Permissible.deserialize(section)
            recipe.commands = CommandExecutable.deserialize(section)

            return recipe
        }
    }
}