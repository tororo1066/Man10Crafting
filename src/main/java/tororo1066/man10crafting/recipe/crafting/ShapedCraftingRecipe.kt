package tororo1066.man10crafting.recipe.crafting

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.crafting.ShapedCraftingRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer
import tororo1066.man10crafting.recipe.options.CommandExecutable
import tororo1066.man10crafting.recipe.options.Permissible
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import kotlin.collections.iterator

open class ShapedCraftingRecipe: AbstractCraftingRecipe() {

    var ingredients: HashMap<Char, AbstractIngredient> = HashMap()
    var shape: List<String> = ArrayList()

    override fun createBukkitRecipe(): Recipe {
        val recipe = ShapedRecipe(namespacedKey, result)
        recipe.shape(*shape.toTypedArray())
        for ((char, ingredient) in ingredients) {
            recipe.setIngredient(char, ingredient.createBukkitRecipeChoice())
        }
        return recipe
    }

    override fun craftable(inventory: CraftingInventory): Boolean {
        val matrix = inventory.matrix
        for (i in matrix.indices) {
            val item = matrix[i] ?: continue
            val row = i / 3
            val col = i % 3
            if (row >= shape.size) return false
            if (col >= shape[row].length) return false
            val char = shape[row][col]
            val ingredient = ingredients[char] ?: return false
            if (!ingredient.validate(item)) return false
        }

        return true
    }

    override fun getIngredients(): List<AbstractIngredient> {
        return ingredients.values.toList()
    }

    override fun SInventory.inlineRenderRecipeView(
        setIngredientItem: (slot: Int, ingredient: AbstractIngredient) -> Unit,
        setResultItem: (slot: Int, itemStack: ItemStack) -> Unit
    ) {
        setItem(
            23,
            SInventoryItem(Material.CRAFTING_TABLE)
                .setDisplayName("§a定型レシピ")
                .setCanClick(false)
        )

        shape.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val slot = (10 + (rowIndex * 9)) + colIndex
                if (char.isWhitespace()) {
                    removeItem(slot)
                } else {
                    val ingredient = ingredients[char] ?: return@forEachIndexed
                    setIngredientItem(slot, ingredient)
                }
            }
        }

        setResultItem(25, result)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return ShapedCraftingRegister(this)
    }

    override fun serialize(): ConfigurationSection {
        val section = super.serialize()
        section.set("shape", shape)
        val ingredientSection = section.createSection("ingredients")
        for ((char, ingredient) in ingredients) {
            ingredientSection.set(char.toString(), ingredient)
        }
        return section
    }

    companion object: RecipeDeserializer<ShapedCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): ShapedCraftingRecipe? {
            val recipe = ShapedCraftingRecipe()
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