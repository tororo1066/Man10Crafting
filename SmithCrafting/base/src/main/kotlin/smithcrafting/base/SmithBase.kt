package smithcrafting.base

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.plugin.java.JavaPlugin
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

interface SmithBase {

    fun create(namespacedKey: NamespacedKey,
               material: ItemStack,
               smithingMaterial: ItemStack,
               additionalMaterial: ItemStack?,
               result: ItemStack,
               transform: Boolean,
               copyNbt: Boolean): Recipe

    fun registerInventory(plugin: JavaPlugin,
                          sInput: SInput,
                          onSave: (SaveData) -> Boolean): SInventory

    fun editInventory(plugin: JavaPlugin,
                      sInput: SInput,
                      data: SaveData,
                      onSave: (SaveData) -> Boolean): SInventory

    fun viewInventory(plugin: JavaPlugin,
                      p: Player,
                      data: SaveData,
                      moveOtherRecipeItem: (p: Player, inv: SInventory, item: ItemStack) -> SInventoryItem): SInventory

    data class SaveData(
        val namespace: String,
        val category: String,
        val index: Int,
        val singleMaterial: ItemStack,
        val smithingMaterial: ItemStack,
        val additionalMaterial: ItemStack?,
        val result: ItemStack,
        val copyNbt: Boolean,
        val transform: Boolean
    )

    companion object {
        fun getInstance(): SmithBase {
            val above120 = UsefulUtility.sTry({
                Class.forName("org.bukkit.inventory.SmithingTransformRecipe")
                true
            }, { false })
            val clazz = if (above120) {
                Class.forName("smithcrafting.above_1_20.Smith")
            } else {
                Class.forName("smithcrafting.below_1_20.Smith")
            }

            return clazz.getDeclaredConstructor().newInstance() as SmithBase
        }
    }
}