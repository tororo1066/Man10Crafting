package tororo1066.man10crafting.recipe.furnace

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.Recipe
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.furnace.FurnaceRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer

class FurnaceCraftingRecipe: AbstractFurnaceRecipe() {

    override val furnaceMaterial = Material.FURNACE

    override fun createBukkitRecipe(): Recipe {
        return FurnaceRecipe(namespacedKey, result, input.createBukkitRecipeChoice(), experience, cookingTime)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return FurnaceRegister(this)
    }

    companion object: RecipeDeserializer<FurnaceCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): FurnaceCraftingRecipe? {
            val recipe = FurnaceCraftingRecipe()
            if (!recipe.deserializeCommon(section)) return null
            return recipe
        }
    }
}