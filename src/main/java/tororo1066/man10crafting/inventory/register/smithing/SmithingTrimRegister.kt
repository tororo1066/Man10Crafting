package tororo1066.man10crafting.inventory.register.smithing

import tororo1066.man10crafting.recipe.smithing.SmithingTrimCraftingRecipe

class SmithingTrimRegister(
    currentData: SmithingTrimCraftingRecipe? = null
): AbstractSmithingRegister<SmithingTrimCraftingRecipe>(
    "§cSmithing Trim",
    currentData ?: SmithingTrimCraftingRecipe(),
    currentData != null
)