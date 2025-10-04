package tororo1066.man10crafting.recipe.smithing

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.SmithingTransformRecipe
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.smithing.SmithingTransformRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class SmithingTransformCraftingRecipe: AbstractSmithingRecipe() {
    override fun createBukkitRecipe(): Recipe {
        return SmithingTransformRecipe(
            namespacedKey,
            result,
            recipeChoiceOrEmpty(template),
            recipeChoiceOrEmpty(base),
            recipeChoiceOrEmpty(addition),
            copyDataComponents
        )
    }

    override fun SInventory.inlineRenderRecipeView(
        setIngredientItem: (slot: Int, ingredient: AbstractIngredient) -> Unit,
        setResultItem: (slot: Int, itemStack: ItemStack) -> Unit
    ) {
        inlineRenderCommonSmithingRecipeView(setIngredientItem)

        setItem(
            23,
            SInventoryItem(Material.SMITHING_TABLE)
                .setDisplayName("§a変換レシピ")
                .setCanClick(false)
        )

        setResultItem(25, result)
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return SmithingTransformRegister(this)
    }

    companion object: RecipeDeserializer<SmithingTransformCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): SmithingTransformCraftingRecipe? {
            val recipe = SmithingTransformCraftingRecipe()
            if (!recipe.deserializeCommon(section)) return null
            return recipe
        }
    }
}