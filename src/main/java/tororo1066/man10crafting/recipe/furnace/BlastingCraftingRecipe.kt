package tororo1066.man10crafting.recipe.furnace

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.Recipe
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.furnace.BlastingRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer

class BlastingCraftingRecipe: AbstractFurnaceRecipe() {

    override val furnaceMaterial = Material.BLAST_FURNACE

    override fun createBukkitRecipe(): Recipe {
        return BlastingRecipe(namespacedKey, result, input.createBukkitRecipeChoice(), experience, cookingTime)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return BlastingRegister(this)
    }

    companion object: RecipeDeserializer<BlastingCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): BlastingCraftingRecipe? {
            val recipe = BlastingCraftingRecipe()
            if (!recipe.deserializeCommon(section)) return null
            return recipe
        }
    }
}