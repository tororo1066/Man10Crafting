package tororo1066.man10crafting.inventory.register.furnace

import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.integer.PlusInt
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

abstract class AbstractFurnaceRegister(invName: String, val type: RecipeData.Type): SInventory(Man10Crafting.plugin,invName,5) {

    var exp = 0f
    var furnaceTime = 100

    override fun renderMenu(): Boolean {
        setItems(0..44, SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
        setItems(listOf(11,15,19,21,23,25,29,32), SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
        removeItems(listOf(20,24))
        setItem(13, createInputItem(SItem(Material.EXPERIENCE_BOTTLE).setDisplayName("§a獲得経験値量").setLore(listOf("§6現在:${exp}")),PlusInt::class.java,"§a経験値量を入力してください"){ int, p ->
            exp = int.get().toFloat()
            p.sendMessage(Man10Crafting.prefix + "§a${exp}にしました")
        })

        setItem(31, createInputItem(SItem(Material.COAL).setDisplayName("§c燃焼時間").setLore(listOf("§6現在:${furnaceTime}")),PlusInt::class.java,"§a燃焼時間を入力してください"){ int, p ->
            furnaceTime = int.get()
            p.sendMessage(Man10Crafting.prefix + "§a${furnaceTime}にしました")
        })

        setItem(44,createInputItem(SItem(Material.WRITABLE_BOOK).setDisplayName("§a保存"),String::class.java,"カテゴリー名を入力してください",
            ClickType.LEFT,true) { category, p ->
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

    fun save(namespace: String, category: String): Boolean {
        val result = getItem(24)?:return false
        val material = getItem(20)?:return false
        material.amount = 1
        val recipeData = RecipeData()
        recipeData.type = type
        recipeData.singleMaterial = material
        recipeData.result = result

        recipeData.namespace = namespace
        recipeData.category = category
        recipeData.furnaceTime = furnaceTime
        recipeData.furnaceExp = exp
        recipeData.saveConfig()
        recipeData.register()

        return true
    }
}