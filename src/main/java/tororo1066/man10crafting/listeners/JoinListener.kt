package tororo1066.man10crafting.listeners

import org.bukkit.NamespacedKey
import org.bukkit.event.player.PlayerJoinEvent
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.annotation.SEventHandler

class JoinListener {

    @SEventHandler
    fun join(e: PlayerJoinEvent){
        val discoverable = ArrayList<NamespacedKey>()
        val undiscoverable = ArrayList<NamespacedKey>()
        Man10Crafting.recipes.values.forEach {
            if (it.enabled && it.accessible(e.player) && !it.hidden){
                discoverable.add(it.namespacedKey)
            } else {
                undiscoverable.add(it.namespacedKey)
            }
        }

        e.player.discoveredRecipes.forEach {
            if (it.namespace != "man10crafting") return@forEach
            if (!Man10Crafting.recipes.containsKey(it)) {
                undiscoverable.add(it)
            }
        }

        e.player.discoverRecipes(discoverable)
        e.player.undiscoverRecipes(undiscoverable)
    }
}