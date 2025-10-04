package tororo1066.man10crafting.recipe

import org.bukkit.configuration.ConfigurationSection

interface RecipeDeserializer<T: AbstractRecipe> {
    fun deserialize(section: ConfigurationSection): T?
}