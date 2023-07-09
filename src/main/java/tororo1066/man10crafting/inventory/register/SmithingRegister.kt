package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

open class SmithingRegister: SInventory(Man10Crafting.plugin,"§5Smithing",5) {

    var index = Int.MAX_VALUE
    override fun renderMenu(): Boolean {

        setItems(0..44, SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
        val whitePane = SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
        setItems(9..35,whitePane)
        removeItems(listOf(19,22,25))

        setItem(4,SInventoryItem(Material.LIME_CONCRETE).setDisplayName("§aオプション").setCanClick(false).setClickEvent {
            val optionMenu = object : SInventory(Man10Crafting.plugin,"§aオプション",3) {
                override fun renderMenu(): Boolean {
                    setItems(0..26,SInventoryItem(SItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ")).setCanClick(false))
                    setItem(10,createInputItem(SItem(Material.BOOK).setDisplayName("§6レシピブックで表示される優先度").addLore("§a優先度:${index}").addLore("§e数値が低いほど前に出てきます"),Int::class.java,"§a数値を入力してください") { int, p ->
                        index = int
                        p.sendMessage("§b" + int + "に変更しました")
                    })
                    return true
                }
            }
            moveChildInventory(optionMenu,it.whoClicked as Player)
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
        val result = getItem(25)?:return false
        val material = getItem(19)?:return false
        val smithingMaterial = getItem(22)?:return false
        material.amount = 1
        smithingMaterial.amount = 1
        if (material.isSimilar(smithingMaterial))return false
        val recipeData = RecipeData()
        recipeData.type = RecipeData.Type.SMITHING
        recipeData.singleMaterial = material
        recipeData.smithingMaterial = smithingMaterial
        recipeData.result = result

        recipeData.namespace = namespace
        recipeData.category = category
        recipeData.index = index
        recipeData.saveConfig()
        recipeData.register()

        return true
    }
}