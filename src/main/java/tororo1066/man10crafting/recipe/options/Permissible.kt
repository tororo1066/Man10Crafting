package tororo1066.man10crafting.recipe.options

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

interface Permissible: SerializableOption {
    var permission: String?

    fun hasPermission(commandSender: CommandSender): Boolean {
        val perm = permission
        return perm.isNullOrEmpty() || commandSender.hasPermission(perm)
    }

    override fun serializeOption(): ConfigurationSection {
        val section = YamlConfiguration()
        permission?.let {
            section.set("permission", it)
        }
        return section
    }

    companion object {
        fun deserialize(section: ConfigurationSection): String? {
            return section.getString("permission")
        }
    }
}