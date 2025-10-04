package tororo1066.man10crafting.ingredient

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

abstract class AbstractIngredient: ConfigurationSerializable {

    abstract val displayItems: List<ItemStack>

    abstract fun createBukkitRecipeChoice(): RecipeChoice

    abstract fun validate(itemStack: ItemStack): Boolean

    abstract fun getAmount(itemStack: ItemStack): Int?

    abstract fun isSimilar(itemStack: ItemStack): Boolean

    abstract fun isValid(): Boolean

    abstract fun editItem(
        inventory: SInventory,
        index: Int,
        onSelectNewIngredient: (player: Player, newIngredient: AbstractIngredient?) -> Unit
    ): SInventoryItem

    fun getNextIndex(currentIndex: Int): Int {
        return if (currentIndex + 1 >= displayItems.size) 0 else currentIndex + 1
    }
}