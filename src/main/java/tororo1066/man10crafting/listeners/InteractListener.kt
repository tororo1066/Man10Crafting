package tororo1066.man10crafting.listeners

import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.inventory.player.AutoPlaceMenu
import tororo1066.tororopluginapi.annotation.SEventHandler

class InteractListener {

    @SEventHandler(EventPriority.HIGHEST)
    fun event(e: PlayerInteractEvent){
        if (e.isCancelled)return
        if (e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY)return
        if (!Man10Crafting.enabledAutoCraft)return
        if (e.hand != EquipmentSlot.HAND)return
        if (e.action != Action.RIGHT_CLICK_BLOCK || !e.player.isSneaking || e.item != null)return
        val block = e.clickedBlock?:return
        if (!listOf(
                Material.CRAFTING_TABLE,Material.FURNACE,
                Material.BLAST_FURNACE,Material.SMOKER,
                Material.STONECUTTER,Material.SMITHING_TABLE
        ).contains(block.type))return

        e.isCancelled = true

        AutoPlaceMenu(e.player).open(e.player)
    }
}