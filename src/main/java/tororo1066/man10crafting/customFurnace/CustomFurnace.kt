package tororo1066.man10crafting.customFurnace

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class CustomFurnace {

    var internalName: String = ""
    var displayName: String = ""
    var fuelSpeedMultiplier: Double = 1.0
    var speedMultiplier: Double = 1.0
    /**
     * . . i . . . . . .
     * . . . . . . . o .
     * . . f . . . . . .
     * i: input
     * o: output
     * f: fuel
     * v: overlay
     * p: progress
     * .: empty
     */
    var inventoryFormat: Array<String> = arrayOf(
        "v . i . . . . . .",
        ". . . . p . o . .",
        ". . f . . . . . ."
    )
    var overlayMaterial: Material = Material.BARRIER
    var overlayCustomModelData: Int = 0

    var progressMaterial: Material = Material.BLACK_STAINED_GLASS_PANE
    var progressStep: Int = 7
    var progressStartCustomModelData: Int = 0

    var inputSlot: Int = 0
    var outputSlot: Int = 0
    var fuelSlot: Int = 0
    var progressSlot: Int = 0

    fun updateSlot() {
        for (i in inventoryFormat.indices) {
            for (j in inventoryFormat[i].indices) {
                val value = inventoryFormat[i][j]
                if (value == ' ') continue
                val index = i * 9 + (j / 2)
                when (value) {
                    'i' -> inputSlot = index
                    'o' -> outputSlot = index
                    'f' -> fuelSlot = index
                    'p' -> progressSlot = index
                }
            }
        }
    }

    companion object {
        fun loadFromYml(yml: File): CustomFurnace {
            val customFurnace = CustomFurnace()
            val config = YamlConfiguration.loadConfiguration(yml)
            customFurnace.internalName = yml.nameWithoutExtension
            customFurnace.displayName = config.getString("displayName") ?: customFurnace.internalName
            customFurnace.fuelSpeedMultiplier = config.getDouble("fuelSpeedMultiplier", 1.0)
            customFurnace.speedMultiplier = config.getDouble("speedMultiplier", 1.0)
            customFurnace.inventoryFormat = config.getStringList("inventoryFormat").toTypedArray()
            customFurnace.overlayMaterial = Material.getMaterial(config.getString("overlayMaterial") ?: "BARRIER") ?: Material.BARRIER
            customFurnace.overlayCustomModelData = config.getInt("overlayCustomModelData", 0)
            customFurnace.progressMaterial = Material.getMaterial(config.getString("progressMaterial") ?: "BLACK_STAINED_GLASS_PANE") ?: Material.BLACK_STAINED_GLASS_PANE
            customFurnace.progressStep = config.getInt("progressStep", 7)
            customFurnace.progressStartCustomModelData = config.getInt("progressStartCustomModelData", 0)
            customFurnace.updateSlot()
            return customFurnace
        }
    }
}