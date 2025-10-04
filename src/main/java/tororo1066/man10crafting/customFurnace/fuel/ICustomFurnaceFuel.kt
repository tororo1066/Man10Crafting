package tororo1066.man10crafting.customFurnace.fuel

import org.bukkit.inventory.ItemStack

interface ICustomFurnaceFuel {
    val fuelStack: ItemStack
    val burnTime: Int
    val speedMultiplier: Double
    val saveName: String
}