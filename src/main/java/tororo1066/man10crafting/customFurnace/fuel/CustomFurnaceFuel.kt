package tororo1066.man10crafting.customFurnace.fuel

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

class CustomFurnaceFuel(
    override val fuelStack: ItemStack,
    override val burnTime: Int,
    override val speedMultiplier: Double,
    var internalName: String,
    override val saveName: String = internalName
): ICustomFurnaceFuel {

    companion object {
        fun loadFromYml(yml: File): CustomFurnaceFuel {
            val config = YamlConfiguration.loadConfiguration(yml)
            val internalName = yml.nameWithoutExtension
            val fuelStack = config.getItemStack("fuelStack") ?: ItemStack(Material.BARRIER)
            val burnTime = config.getInt("burnTime", 200)
            val speedMultiplier = config.getDouble("speedMultiplier", 1.0)
            return CustomFurnaceFuel(fuelStack, burnTime, speedMultiplier, internalName)
        }
    }
}