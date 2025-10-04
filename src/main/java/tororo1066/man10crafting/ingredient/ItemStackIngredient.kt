package tororo1066.man10crafting.ingredient

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import tororo1066.man10crafting.inventory.register.SelectIngredientMenu
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.defaultMenus.SingleItemInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import java.util.function.Consumer

class ItemStackIngredient: AbstractIngredient() {

    val itemStacks: ArrayList<ItemStack> = ArrayList()
    override val displayItems: List<ItemStack> = itemStacks

    override fun createBukkitRecipeChoice(): RecipeChoice {
        return RecipeChoice.ExactChoice(itemStacks)
    }

    override fun validate(itemStack: ItemStack): Boolean {
        return getAmount(itemStack) != null
    }

    override fun getAmount(itemStack: ItemStack): Int? {
        val matched = itemStacks.firstOrNull { it.isSimilar(itemStack) && itemStack.amount >= it.amount } ?: return null
        return matched.amount
    }

    override fun isSimilar(itemStack: ItemStack): Boolean {
        return itemStacks.any { it.isSimilar(itemStack) }
    }

    override fun editItem(
        inventory: SInventory,
        index: Int,
        onSelectNewIngredient: (player: Player, newIngredient: AbstractIngredient?) -> Unit
    ): SInventoryItem {
        val item = displayItems.getOrNull(index) ?: ItemStack(Material.BOOK)
        val sInventoryItem =
            SInventoryItem(item)
                .setDisplayName("§aItemStackIngredient")
                .setLore(
                    "§7複数のアイテムを設定できます",
                    "§7設定したアイテムのいずれかが必要になります",
                    "",
                    "§e左クリックで編集",
                    "§e右クリックで素材の種類を変更する"
                )
                .setCanClick(false)
                .setClickEvent {
                    val p = it.whoClicked as Player
                    if (it.isRightClick) {
                        val inv = SelectIngredientMenu { newIngredient ->
                            onSelectNewIngredient(p, newIngredient)
                        }
                        inventory.moveChildInventory(inv, p)
                        return@setClickEvent
                    }

                    if (!it.isLeftClick) return@setClickEvent

                    val inv = object : LargeSInventory("§a編集") {
                        override fun renderMenu(p: Player): Boolean {
                            val items = ArrayList<SInventoryItem>()
                            itemStacks.forEach { item ->
                                items.add(SInventoryItem(item).setCanClick(false))
                            }
                            items.add(SInventoryItem(Material.EMERALD_BLOCK).setDisplayName("§a追加").setCanClick(false).setClickEvent { e ->
                                val selectItemInv = SingleItemInventory(SJavaPlugin.plugin, "§追加するアイテム")
                                selectItemInv.onConfirm = Consumer { itemStack ->
                                    itemStacks.add(itemStack)
                                    e.whoClicked.closeInventory()
                                    renderMenu(p)
                                }
                                moveChildInventory(selectItemInv, e.whoClicked as Player)
                            })

                            setResourceItems(items)
                            return true
                        }
                    }

                    inventory.moveChildInventory(inv, p)
                }
        return sInventoryItem
    }

    override fun isValid(): Boolean {
        return itemStacks.isNotEmpty()
    }

    override fun serialize(): Map<String?, Any?> {
        val map = HashMap<String?, Any?>()
        map["itemStacks"] = itemStacks
        return map
    }

    companion object {
        @JvmStatic
        @Suppress("unused", "UNCHECKED_CAST")
        fun deserialize(args: Map<String?, Any?>): ItemStackIngredient? {
            val ingredient = ItemStackIngredient()
            val itemStacks = args["itemStacks"] as? List<ItemStack> ?: return null
            ingredient.itemStacks.addAll(itemStacks)
            return ingredient
        }
    }
}