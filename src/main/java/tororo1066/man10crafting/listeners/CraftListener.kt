package tororo1066.man10crafting.listeners

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.*
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.annotation.SEventHandler

class CraftListener {

    @SEventHandler
    fun event(e: PrepareItemCraftEvent){
        val eventRecipe = e.recipe?:return
        if (eventRecipe !is Keyed)return
        Bukkit.broadcastMessage(eventRecipe.key.toString())
        val namespace = eventRecipe.key
        if (namespace.key != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespace.namespace]?:return
        if (!recipe.canCraft(e.view.player as Player)) {
            e.inventory.result = ItemStack(Material.AIR)
        }
    }
}