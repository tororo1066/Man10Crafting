package tororo1066.man10crafting.recipe.furnace

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.Recipe
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.furnace.CampfireRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer

class CampfireCraftingRecipe: AbstractFurnaceRecipe() {
    override val furnaceMaterial = Material.CAMPFIRE

    override fun createBukkitRecipe(): Recipe {
        return CampfireRecipe(namespacedKey, result, input.createBukkitRecipeChoice(), experience, cookingTime)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return CampfireRegister(this)
    }

    companion object: RecipeDeserializer<CampfireCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): CampfireCraftingRecipe? {
            val recipe = CampfireCraftingRecipe()
            if (!recipe.deserializeCommon(section)) return null
            return recipe
        }
    }
}