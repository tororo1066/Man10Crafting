package tororo1066.man10crafting.recipe.furnace

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.SmokingRecipe
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.furnace.SmokingRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer

class SmokingCraftingRecipe: AbstractFurnaceRecipe() {

    override val furnaceMaterial = Material.SMOKER

    override fun createBukkitRecipe(): Recipe {
        return SmokingRecipe(namespacedKey, result, input.createBukkitRecipeChoice(), experience, cookingTime)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return SmokingRegister(this)
    }

    companion object: RecipeDeserializer<SmokingCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): SmokingCraftingRecipe? {
            val recipe = SmokingCraftingRecipe()
            if (!recipe.deserializeCommon(section)) return null
            return recipe
        }
    }
}