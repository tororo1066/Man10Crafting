package tororo1066.man10crafting.inventory.register.crafting

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.inventory.register.Items
import tororo1066.man10crafting.inventory.register.Items.optionsItem
import tororo1066.man10crafting.recipe.crafting.ShapedCraftingRecipe
import tororo1066.man10crafting.recipe.crafting.StackableShapedCraftingRecipe
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import kotlin.text.iterator

class ShapedCraftingRegister(
    currentData: ShapedCraftingRecipe? = null,
): AbstractCraftingRegister<ShapedCraftingRecipe>(
    "§bShaped",
    currentData ?: ShapedCraftingRecipe(),
    currentData != null
) {

    init {
        var index = 0
        for (row in this.currentData.shape) {
            for (col in row) {
                val slot = craftingSlots[index]
                val ingredient = this.currentData.ingredients[col]
                if (ingredient != null) {
                    ingredients[slot] = Pair(ingredient, 0)
                }
                index++
            }
        }
    }

    override fun prepareSave(): Boolean {
        if (!super.prepareSave()) return false
        currentData.ingredients.clear()

        val ingredientKeys = ingredients.keys.sorted()

        val shape = ArrayList<String>()
        for (row in 0..2) {
            var str = ""
            for (col in 0..2) {
                val slot = craftingSlots[row * 3 + col]
                val ingredient = ingredients[slot]
                if (ingredient == null) {
                    str += " "
                } else {
                    val char = ('a' + ingredientKeys.indexOf(slot))
                    str += char
                    currentData.ingredients[char] = ingredient.first
                }
            }
            shape.add(str)
        }
        currentData.shape = shape

        return true
    }

    override fun renderMenu(p: Player): Boolean {
        super.renderMenu(p)
        setItem(4, optionsItem(currentData) { inv ->
            listOf(
                SInventoryItem(Material.OBSIDIAN).setDisplayName("§cスタック可能レシピ")
                    .addLore("§a現在の値: ${Items.booleanToString(currentData is StackableShapedCraftingRecipe)}")
                    .addLore("§eレシピのアイテムをスタックできるようにするか")
                    .setCanClick(false).setClickEvent {
                        if (prepareSave()) {
                            val serialized = currentData.serialize()
                            val newData = if (currentData is StackableShapedCraftingRecipe) {
                                ShapedCraftingRecipe.deserialize(serialized)
                            } else {
                                StackableShapedCraftingRecipe.deserialize(serialized)
                            }
                            if (newData == null) {
                                p.sendMessage("§cエラーが発生しました")
                                return@setClickEvent
                            }
                            currentData = newData
                        } else {
                            if (ingredients.isEmpty() && inv.getItem(24) == null) {
                                currentData = if (currentData is StackableShapedCraftingRecipe) {
                                    ShapedCraftingRecipe()
                                } else {
                                    StackableShapedCraftingRecipe()
                                }
                            }
                        }


                        inv.renderMenu(p)
                        inv.renderInventory(inv.nowPage)
                    }
            )
        })

        return true
    }
}