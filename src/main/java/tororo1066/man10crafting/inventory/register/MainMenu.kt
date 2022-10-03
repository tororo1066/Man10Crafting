package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.inventory.register.furnace.BlastingRegister
import tororo1066.man10crafting.inventory.register.furnace.FurnaceRegister
import tororo1066.man10crafting.inventory.register.furnace.SmokingRegister
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class MainMenu: SInventory(Man10Crafting.plugin,"MainMenu",5) {

    override fun renderMenu(): Boolean {
        setItems(0..44, SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setCanClick(false))

        setItem(10, SInventoryItem(Material.CRAFTING_TABLE).setCanClick(false).setClickEvent {
            moveChildInventory(NormalCraftRegister(),it.whoClicked as Player)
        })

        setItem(28, SInventoryItem(Material.FURNACE).setCanClick(false).setClickEvent {
            moveChildInventory(FurnaceRegister(),it.whoClicked as Player)
        })

        setItem(12, SInventoryItem(Material.SMOKER).setCanClick(false).setClickEvent {
            moveChildInventory(SmokingRegister(),it.whoClicked as Player)
        })

        setItem(30, SInventoryItem(Material.BLAST_FURNACE).setCanClick(false).setClickEvent {
            moveChildInventory(BlastingRegister(),it.whoClicked as Player)
        })

        setItem(14, SInventoryItem(Material.SMITHING_TABLE).setCanClick(false).setClickEvent {
            moveChildInventory(SmithingRegister(),it.whoClicked as Player)
        })

        setItem(44, SInventoryItem(Material.WRITABLE_BOOK).setDisplayName("§eレシピを編集する").setCanClick(false).setClickEvent {
            moveChildInventory(EditMenu(),it.whoClicked as Player)
        })

        return true
    }
}