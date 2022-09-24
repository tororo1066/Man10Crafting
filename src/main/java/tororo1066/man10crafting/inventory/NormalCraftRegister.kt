package tororo1066.man10crafting.inventory

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class NormalCraftRegister: SInventory(Man10Crafting.plugin,"§bShaped/Shapeless",5) {

    var isShaped = false
    var perm = ""

    //0  1  2  3  k  5  6  7  8
    //9  s  s  s  13 14 15 16 17
    //18 s  s  s  a 23  e 25 26
    //27 s  s  s  31 32 33 34 35
    //36 37 38 39 40 41 42 43 44

    override fun renderMenu(): Boolean {

        setItems(0..44,SInventoryItem(SItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ")).setCanClick(false))

        removeItems(10..12)
        removeItems(19..21)
        removeItems(28..30)
        removeItem(24)

        setItem(22, SItem(Material.GREEN_TERRACOTTA).setDisplayName("§a自由変形レシピ").toSInventoryItem().setCanClick(false).setBiClickEvent { item, _ ->
            isShaped = true
            setItem(22,SItem(Material.RED_TERRACOTTA).setDisplayName("§c定型レシピ").toSInventoryItem().setCanClick(false).setClickEvent {
                isShaped = false
                setItem(22,item)
            })
        })

        setItem(4,createInputItem(SItem(Material.LIGHT_BLUE_CONCRETE).setDisplayName("§bクラフト権限変更").setLore(listOf("§a権限:${perm}")).toSInventoryItem(),String::class.java,"§a権限名を入れてください") { str, p ->
            perm = str
            p.sendMessage("§b" + str + "に変更しました")
        })

        setItem(44,createInputItem(SItem(Material.WRITABLE_BOOK).setDisplayName("§a保存"),String::class.java,"内部名を入力してください",ClickType.LEFT,true) { str, p ->
            if (save(str)){
                p.sendMessage("§a保存に成功しました")
            } else {
                p.sendMessage("§c完成品のアイテムが存在しません")
                open(p)
            }
        })

        return true
    }

    fun save(namespace: String): Boolean {
        val result = getItem(24)?:return false


        if (isShaped){
            val recipeData = RecipeData()
            recipeData.namespace = namespace
            recipeData.type = RecipeData.Type.SHAPED
            recipeData.permission = perm
            recipeData.result = result

            val items = HashMap<ItemStack,Char>()
            val chars = listOf('a','b','c','d','e','f','g','h','i')
            listOf(10,11,12,19,20,21,28,29,30).forEachIndexed { index, i ->
                val item = getItem(i)?:return@forEachIndexed
                if (items.containsKey(item))return@forEachIndexed
                items[item] = chars[index]
            }
            items.forEach {
                recipeData.materials[it.value] = it.key
            }

            fun itemTask(vararg index: Int): String {
                val builder = SStr()
                index.forEach {
                    val item = getItem(it)
                    if (item == null){
                        builder.append(" ")
                    } else {
                        builder.append(items[item]!!.toString())
                    }
                }
                Bukkit.broadcastMessage(builder.toString())
                return builder.toString()
            }

            val row1 = itemTask(10,11,12)
            val row2 = itemTask(19,20,21)
            val row3 = itemTask(28,29,30)
            recipeData.shape.addAll(listOf(row1,row2,row3))
            recipeData.saveConfig()
            recipeData.register()

        } else {
            val recipeData = RecipeData()
            recipeData.namespace = namespace
            recipeData.type = RecipeData.Type.SHAPELESS
            recipeData.permission = perm
            recipeData.result = result

            listOf(10,11,12,19,20,21,28,29,30).forEach {
                recipeData.shapelessMaterials.add(getItem(it)?:return@forEach)
            }

            recipeData.saveConfig()
            recipeData.register()
        }
        return true
    }
}