package tororo1066.man10crafting.inventory.register.crafting

import tororo1066.man10crafting.recipe.crafting.ShapelessCraftingRecipe

class ShapelessCraftingRegister(
    currentData: ShapelessCraftingRecipe? = null,
) : AbstractCraftingRegister<ShapelessCraftingRecipe>(
    "§bShapeless",
    currentData ?: ShapelessCraftingRecipe(),
    currentData != null
) {

    init {
        this.currentData.ingredients.forEachIndexed { index, ingredient ->
            val slot = craftingSlots[index]
            ingredients[slot] = ingredient to 0
        }
    }

    override fun prepareSave(): Boolean {
        if (!super.prepareSave()) return false
        currentData.ingredients.clear()
        ingredients.values.forEach {
            currentData.ingredients.add(it.first)
        }
        return true
    }
}