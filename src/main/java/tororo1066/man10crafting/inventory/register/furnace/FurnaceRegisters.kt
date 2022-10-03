package tororo1066.man10crafting.inventory.register.furnace

import org.bukkit.Material
import tororo1066.man10crafting.data.RecipeData

open class FurnaceRegister: AbstractFurnaceRegister("§cFurnace",RecipeData.Type.FURNACE) {

    override fun renderMenu(): Boolean {
        super.renderMenu()
        setItem(22,Material.FURNACE)
        return true
    }
}

open class SmokingRegister: AbstractFurnaceRegister("§cSmoking",RecipeData.Type.SMOKING) {

    override fun renderMenu(): Boolean {
        super.renderMenu()
        setItem(22,Material.SMOKER)
        return true
    }
}

open class BlastingRegister: AbstractFurnaceRegister("§cBlasting",RecipeData.Type.BLASTING) {

    override fun renderMenu(): Boolean {
        super.renderMenu()
        setItem(22,Material.BLAST_FURNACE)
        return true
    }
}