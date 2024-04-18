package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.man10crafting.inventory.register.OptionRegister.Companion.generateItem
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

open class StoneCuttingRegister: AbstractRegister("§aStoneCutting") {

    override fun renderMenu(): Boolean {
        setItems(0..44, SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
        setItems(listOf(11,15,19,21,23,25,29,33), SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
        removeItems(listOf(20,24))

        setItem(4, generateItem(
            index = index,
            indexFunc = { index = it },
            registerIndex = registerIndex,
            registerIndexFunc = { registerIndex = it },
            hidden = hidden,
            hiddenFunc = { hidden = it }
        ))

        setItem(22, SInventoryItem(Material.STONECUTTER).setCanClick(false))

        setItem(44,createInputItem(
            SItem(Material.WRITABLE_BOOK).setDisplayName("§a保存"),String::class.java,"カテゴリー名を入力してください",
            invOpenCancel = true) { category, p ->
            Man10Crafting.sInput.sendInputCUI(p,String::class.java,"内部名を入力してください") { str ->
                if (save(str,category)){
                    p.sendMessage("§a保存に成功しました")
                } else {
                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                    open(p)
                }
            }
        })

        return true
    }

    override fun save(namespace: String, category: String, recipeData: RecipeData): Boolean {
        val result = getItem(24)?:return false
        val material = getItem(20)?:return false
        material.amount = 1
        recipeData.type = RecipeData.Type.STONECUTTING
        recipeData.singleMaterial = material
        recipeData.result = result

        return super.save(namespace, category, recipeData)
    }
}