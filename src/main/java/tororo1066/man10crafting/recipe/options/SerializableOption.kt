package tororo1066.man10crafting.recipe.options

import org.bukkit.configuration.ConfigurationSection

interface SerializableOption {
    fun serializeOption(): ConfigurationSection
}