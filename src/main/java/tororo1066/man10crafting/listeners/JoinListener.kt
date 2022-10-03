package tororo1066.man10crafting.listeners

import org.bukkit.NamespacedKey
import org.bukkit.event.player.PlayerJoinEvent
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.annotation.SEventHandler

class JoinListener {

    @SEventHandler
    fun join(e: PlayerJoinEvent){
        e.player.discoverRecipes(Man10Crafting.recipes.filter { it.value.checkNeed(e.player) }.map { NamespacedKey(Man10Crafting.plugin,it.key) })
    }
}