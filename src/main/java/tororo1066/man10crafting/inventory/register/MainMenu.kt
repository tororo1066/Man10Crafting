package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.man10crafting.inventory.register.furnace.BlastingRegister
import tororo1066.man10crafting.inventory.register.furnace.FurnaceRegister
import tororo1066.man10crafting.inventory.register.furnace.SmokingRegister
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class MainMenu: SInventory(Man10Crafting.plugin,"MainMenu",5) {

    init {
        registerClickSound()
    }

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
            moveChildInventory(Man10Crafting.smithUtil.registerInventory(
                Man10Crafting.plugin,
                Man10Crafting.sInput
            ) { data ->
                val result = data.result
                val material = data.singleMaterial
                val smithingMaterial = data.smithingMaterial
                val additionalMaterial = data.additionalMaterial
                material.amount = 1
                smithingMaterial.amount = 1
                val recipeData = RecipeData()
                recipeData.type = RecipeData.Type.SMITHING
                recipeData.singleMaterial = material
                recipeData.smithingMaterial = smithingMaterial
                recipeData.result = result
                recipeData.smithingAdditionalMaterial = additionalMaterial

                recipeData.namespace = data.namespace
                recipeData.category = data.category
                recipeData.index = data.index
                recipeData.smithingCopyNbt = data.copyNbt
                recipeData.smithingTransform = data.transform
                recipeData.saveConfig()
                recipeData.register()

                return@registerInventory true
            },it.whoClicked as Player)
        })

        setItem(32, SInventoryItem(Material.STONECUTTER).setCanClick(false).setClickEvent {
            moveChildInventory(StoneCuttingRegister(),it.whoClicked as Player)
        })

        setItem(16, SInventoryItem(Material.BREWING_STAND)
            .addLore("§c§lDEMO & PAPER ONLY").setCanClick(false).setClickEvent {
                if (!SStr.isPaper()) {
                    it.whoClicked.sendMessage("§c§lPaperでのみ使用できます")
                    it.whoClicked.closeInventory()
                    return@setClickEvent
                }
                moveChildInventory(BrewingRegister(),it.whoClicked as Player)
        })

        setItem(44, SInventoryItem(Material.WRITABLE_BOOK).setDisplayName("§eレシピを編集する").setCanClick(false).setClickEvent {
            moveChildInventory(EditMenu(),it.whoClicked as Player)
        })

        return true
    }

    companion object {
        fun indexItem(inv: SInventory, index: Int, function: (int: Int) -> Unit): SInventoryItem {
            return inv.createInputItem(SItem(Material.BOOK).setDisplayName("§6登録、レシピブックの優先度").addLore("§a優先度:${index}").addLore("§e数値が低いほど優先されます"), Int::class.java, "§a数値を入力してください") { int, p ->
                function.invoke(int)
                p.sendPlainMessage("§b" + int + "に変更しました")
            }
        }
    }
}