package tororo1066.man10crafting.customFurnace

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import tororo1066.man10crafting.customFurnace.fuel.ICustomFurnaceFuel
import java.io.File

class CustomFurnaceRecipe: Recipe {
    var internalName: String = ""
    lateinit var input: ItemStack
    lateinit var output: ItemStack
    var exp: Float = 0.0f
    var time: Int = 0
    var fuel: ICustomFurnaceFuel? = null

    fun checkRecipe(input: ItemStack, fuel: ICustomFurnaceFuel?): Boolean {
        return this.input.isSimilar(input) && this.fuel == fuel
    }

    override fun getResult(): ItemStack {
        return output
    }

    fun saveToYml(yml: File) {
        val config = YamlConfiguration()
        config.set("input", input)
        config.set("output", output)
        config.set("exp", exp)
        config.set("time", time)
        config.set("fuel", fuel?.saveName)
        config.save(yml)
    }

    companion object {
        fun loadFromYml(yml: File): CustomFurnaceRecipe {
            val customFurnaceRecipe = CustomFurnaceRecipe()
            val config = YamlConfiguration.loadConfiguration(yml)
            customFurnaceRecipe.internalName = yml.nameWithoutExtension
            customFurnaceRecipe.input = config.getItemStack("input") ?: ItemStack(Material.BARRIER)
            customFurnaceRecipe.output = config.getItemStack("output") ?: ItemStack(Material.AIR)
            customFurnaceRecipe.exp = config.getDouble("exp", 0.0).toFloat()
            customFurnaceRecipe.time = config.getInt("time", 200)
            val fuel = config.getString("fuel")
            if (fuel != null) {
                customFurnaceRecipe.fuel = CustomFurnaceManager.instance.getFuel(fuel)
            }
            return customFurnaceRecipe
        }
    }
}