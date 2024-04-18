package tororo1066.man10crafting.inventory.register

import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.sInventory.SInventory

abstract class AbstractRegister(name: String): SInventory(Man10Crafting.plugin, name, 5) {
    var index = Int.MAX_VALUE
    var registerIndex: Int? = null
    var hidden = false
    override var savePlaceItems = true

    open fun save(namespace: String, category: String, recipeData: RecipeData = RecipeData()): Boolean {
        recipeData.namespace = namespace
        recipeData.category = category
        recipeData.index = index
        recipeData.registerIndex = registerIndex
        recipeData.hidden = hidden
        recipeData.saveConfig()
        recipeData.register()
        return true
    }
}