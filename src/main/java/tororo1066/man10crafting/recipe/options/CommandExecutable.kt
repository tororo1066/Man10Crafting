package tororo1066.man10crafting.recipe.options

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.HumanEntity

interface CommandExecutable: SerializableOption {
    var commands: List<String>

    fun executeCommands(humanEntity: HumanEntity) {
        for (command in commands) {
            val cmd = command
                .replace("<name>", humanEntity.name)
                .replace("<uuid>", humanEntity.uniqueId.toString())

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
        }
    }

    override fun serializeOption(): ConfigurationSection {
        val section = YamlConfiguration()
        section.set("commands", commands)
        return section
    }

    companion object {
        fun deserialize(section: ConfigurationSection): List<String> {
            return section.getStringList("commands")
        }
    }
}