package tororo1066.man10crafting.recipe.smithing

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.SmithingTrimRecipe
import org.bukkit.inventory.meta.trim.TrimPattern
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.smithing.SmithingTrimRegister
import tororo1066.man10crafting.recipe.RecipeDeserializer
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class SmithingTrimCraftingRecipe: AbstractSmithingRecipe() {

    lateinit var trimPattern: TrimPattern

    init {
        result = ItemStack.empty() // SmithingTrimRecipeのみresultが必要ないので空で初期化
    }

    override fun createBukkitRecipe(): Recipe {
        return SmithingTrimRecipe(
            namespacedKey,
            recipeChoiceOrEmpty(template),
            recipeChoiceOrEmpty(base),
            recipeChoiceOrEmpty(addition),
            trimPattern,
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
                .setDisplayName("§a装飾レシピ")
                .setCanClick(false)
        )
    }

    override fun getRegisterInventory(): AbstractRegister<*> {
        return SmithingTrimRegister(this)
    }

    override fun serialize(): ConfigurationSection {
        val section = super.serialize()
        val trimId = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).getKey(trimPattern)
        if (trimId != null) {
            section.set("trimPattern", trimId.toString())
        }
        return section
    }

    companion object: RecipeDeserializer<SmithingTrimCraftingRecipe> {
        override fun deserialize(section: ConfigurationSection): SmithingTrimCraftingRecipe? {
            val recipe = SmithingTrimCraftingRecipe()
            if (!recipe.deserializeCommon(section)) return null
            val trimId = section.getString("trimPattern") ?: return null
            val namespacedKey = NamespacedKey.fromString(trimId) ?: return null
            recipe.trimPattern = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(namespacedKey) ?: return null
            return recipe
        }
    }
}