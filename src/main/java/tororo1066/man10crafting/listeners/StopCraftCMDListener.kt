package tororo1066.man10crafting.listeners

import tororo1066.man10crafting.Man10Crafting
import tororo1066.stopcraftcmdv2.events.StopCmdInteractEvent
import tororo1066.tororopluginapi.annotation.SEventHandler

class StopCraftCMDListener {

    @SEventHandler
    fun onInteract(e: StopCmdInteractEvent) {
        if (e.isCancelled) return
        val item = e.clickEvent.currentItem ?: return
        if (Man10Crafting.ingredientCache.any { it.isSimilar(item) }) {
            e.isCancelled = true
        }
    }
}