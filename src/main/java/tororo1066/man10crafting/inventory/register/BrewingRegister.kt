package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.man10crafting.inventory.register.OptionRegister.Companion.generateItem
import tororo1066.tororopluginapi.otherClass.PlusInt
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

open class BrewingRegister: AbstractRegister("§cBrewing") {

    var brewingTime = -1

    override fun renderMenu(): Boolean {

        setItems(0..44, SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
        setItem(10,SInventoryItem(Material.NETHER_WART).setDisplayName("§a素材").setCanClick(false))
        setItem(13,SInventoryItem(Material.POTION).setDisplayName("§d元").setCanClick(false))
        removeItems(listOf(19,22,25))

        setItem(4, generateItem(
            index = index,
            indexFunc = { index = it },
            registerIndex = registerIndex,
            registerIndexFunc = { registerIndex = it },
            hidden = hidden,
            hiddenFunc = { hidden = it }
        ))

        setItem(31, createInputItem(SItem(Material.BLAZE_POWDER).setDisplayName("§c必要時間").addLore("§6現在:${brewingTime}"),PlusInt::class.java,"§a数値を入力してください"){ int, p ->
            brewingTime = int.get()
            p.sendMessage(Man10Crafting.prefix + "§a${int.get()}にしました")
        })

        setItem(44,createInputItem(SItem(Material.WRITABLE_BOOK).setDisplayName("§a保存"),String::class.java,"カテゴリー名を入力してください",
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
        val ingredient = getItem(19)?:return false
        ingredient.amount = 1
        val input = getItem(22)?:return false
        input.amount = 1
        val result = getItem(25)?:return false
        recipeData.type = RecipeData.Type.BREWING
        recipeData.singleMaterial = ingredient
        recipeData.potionInput = input
        recipeData.result = result
        recipeData.brewingTime = brewingTime
        super.save(namespace, category, recipeData)
        return true
    }
}