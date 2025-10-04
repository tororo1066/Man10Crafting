package tororo1066.man10crafting.customFurnace.fuel

import org.bukkit.inventory.ItemStack

class VanillaCustomFurnaceFuel(val itemStack: ItemStack): ICustomFurnaceFuel {
    override val fuelStack: ItemStack = itemStack
    override val burnTime: Int = FuelBurnTime.getBurnTime(itemStack.type) ?: 10
    override val speedMultiplier: Double = 1.0
    override val saveName: String = itemStack.type.name
}