package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.ingredient.ItemStackIngredient
import tororo1066.man10crafting.ingredient.MaterialIngredient
import tororo1066.man10crafting.ingredient.NBTIngredient
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class SelectIngredientMenu(val onSelect: (AbstractIngredient?) -> Unit): LargeSInventory("§a素材を選択") {

    companion object {
        val ingredients = arrayListOf(
            ItemStackIngredient::class.java,
            MaterialIngredient::class.java,
            NBTIngredient::class.java
        )

        private fun newInstance(clazz: Class<out AbstractIngredient>): AbstractIngredient? {
            return try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val items = arrayListOf<SInventoryItem>()
        ingredients.forEach {
            val sInventoryItem = SInventoryItem(Material.BOOK)
                .setDisplayName("§a${it.simpleName}")
                .setLore(
                    "§e左クリックで選択"
                )
                .setCanClick(false)
                .setClickEvent { e ->
                    if (!e.isLeftClick) return@setClickEvent
                    val newIngredient = newInstance(it) ?: return@setClickEvent
                    onSelect(newIngredient)
                    e.whoClicked.closeInventory()
                }
            items.add(sInventoryItem)
        }

        items.add(SInventoryItem(Material.REDSTONE_BLOCK).setDisplayName("§c空にする").setCanClick(false).setClickEvent { e ->
            onSelect(null)
            e.whoClicked.closeInventory()
        })

        setResourceItems(items)
        return true
    }
}