package tororo1066.man10crafting.listeners

import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.annotation.SEventHandler

class CraftListener {

    @SEventHandler
    fun event(e: PrepareItemCraftEvent){
        val eventRecipe = e.recipe?:return
        if (eventRecipe !is Keyed)return
        val namespace = eventRecipe.key
        if (namespace.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespace.key]?:return
        if (!recipe.checkNeed(e.view as Player)) {
            e.inventory.result = ItemStack(Material.AIR)
        }
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: CraftItemEvent){
        val eventRecipe = e.recipe
        if (eventRecipe !is Keyed)return
        val namespace = eventRecipe.key
        if (namespace.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespace.key]?:return
        if (!recipe.checkNeed(e.whoClicked as Player)){
            e.inventory.result = ItemStack(Material.AIR)
        }

    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: FurnaceSmeltEvent){
        val eventRecipe = e.recipe?:return
        if (eventRecipe.key.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[eventRecipe.key.key]?:return
        if (!recipe.enabled) {
            e.isCancelled = true
        }
    }
}