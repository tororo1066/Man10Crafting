package tororo1066.man10crafting.inventory.register.smithing

import tororo1066.man10crafting.recipe.smithing.SmithingTransformCraftingRecipe

class SmithingTransformRegister(
    currentData: SmithingTransformCraftingRecipe? = null
): AbstractSmithingRegister<SmithingTransformCraftingRecipe>(
    "§cSmithing Transform",
    currentData ?: SmithingTransformCraftingRecipe(),
    currentData != null
) {

    override fun prepareSave(): Boolean {
        val result = inv.getItem(25) ?: return false
        if (!super.prepareSave()) return false
        currentData.result = result
        return true
    }

    init {
        removeItem(25)
        val result = this.currentData.getResultOrNull()
        if (result != null) {
            inv.setItem(25, result)
        }
    }
}