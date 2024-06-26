package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

open class NormalCraftRegister: AbstractRegister("§bShaped/Shapeless") {

    var isShaped = false
    var perm = ""
    var returnBottle = false
    var command = ""
    var stackRecipe = false

    //0  1  2  3  k  5  6  7  8
    //9  s  s  s  13 14 15 16 17
    //18 s  s  s  a  23 e  25 26
    //27 s  s  s  31 32 33 34 35
    //36 37 38 39 40 41 42 43 44

    private fun booleanToString(boolean: Boolean): String {
        return if (boolean) "§f§l[§a§l有効§f§l]" else "§f§l[§c§l無効§f§l]"
    }

    override fun renderMenu(): Boolean {

        val saveItems = HashMap<Int,ItemStack>()
        listOf(10,11,12,19,20,21,28,29,30).forEach {
            saveItems[it] = getItem(it)?:return@forEach
        }

        setItems(0..44,SInventoryItem(SItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ")).setCanClick(false))

        removeItems(10..12)
        removeItems(19..21)
        removeItems(28..30)
        removeItem(24)

        saveItems.forEach {
            setItem(it.key,it.value)
        }

        if (isShaped){
            setItem(22, SItem(Material.RED_TERRACOTTA).setDisplayName("§c定型レシピ").toSInventoryItem().setCanClick(false).setBiClickEvent { item, _ ->
                isShaped = false
                setItem(22,SItem(Material.GREEN_TERRACOTTA).setDisplayName("§a自由変形レシピ").toSInventoryItem().setCanClick(false).setClickEvent {
                    isShaped = true
                    setItem(22,item)
                })
            })
        } else {
            setItem(22, SItem(Material.GREEN_TERRACOTTA).setDisplayName("§a自由変形レシピ").toSInventoryItem().setCanClick(false).setBiClickEvent { item, _ ->
                isShaped = true
                setItem(22,SItem(Material.RED_TERRACOTTA).setDisplayName("§c定型レシピ").toSInventoryItem().setCanClick(false).setClickEvent {
                    isShaped = false
                    setItem(22,item)
                })
            })
        }

        setItem(4,SInventoryItem(Material.LIME_CONCRETE).setDisplayName("§aオプション").setCanClick(false).setClickEvent {
            val optionMenu = object : SInventory(Man10Crafting.plugin,"§aオプション",3) {
                override fun renderMenu(): Boolean {
                    setItems(0..26,SInventoryItem(SItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ")).setCanClick(false))
                    setItem(10,createInputItem(SItem(Material.LIGHT_BLUE_CONCRETE).setDisplayName("§bクラフト権限変更").setLore(listOf("§a権限:${perm}")),String::class.java,"§a権限名を入れてください") { str, p ->
                        perm = str
                        p.sendPlainMessage("§b" + str + "に変更しました")
                    })
                    setItem(11,SInventoryItem(Material.GLASS_BOTTLE).setDisplayName("§bクラフト時にポーションをガラス瓶にして戻す").addLore(booleanToString(returnBottle)).setCanClick(false).setClickEvent {
                        returnBottle = !returnBottle
                        renderMenu()
                    })
                    setItem(12,createInputItem(SItem(Material.COMMAND_BLOCK).setDisplayName("§6クラフト時に実行するコマンド").addLore("§aコマンド:${command}").addLore("§e<name>はプレイヤーの名前、<uuid>はuuidに置き換えられる").addLore("§e/は必要ない"),String::class.java,"§aコマンドを入力してください") { str, p ->
                        command = str
                        p.sendPlainMessage("§b" + str + "に変更しました")
                    })
                    setItem(13, createInputItem(SItem(Material.BOOK).setDisplayName("§6登録、レシピブックの優先度")
                        .addLore("§a優先度:${index}").addLore("§e数値が低いほど優先されます"), Int::class.java, "§a数値を入力してください") { int, p ->
                        index = int
                        p.sendPlainMessage("§b" + int + "に変更しました")
                    })
                    setItem(14, createNullableInputItem(SItem(Material.WRITTEN_BOOK).setDisplayName("§6レシピ登録の優先度")
                        .addLore("§a優先度:${registerIndex}")
                        .addLore("§e数値が低いほど先に登録されます")
                        .addLore("nullでレシピブックの優先度と同じになります"), Int::class.java, "§a数値を入力してください") { int, p ->
                        registerIndex = int
                        p.sendPlainMessage("§b" + int + "に変更しました")
                    })
                    setItem(15, SInventoryItem(SItem(Material.CYAN_CONCRETE).setDisplayName("§6非表示設定"))
                        .addLore("§a現在:${if (hidden) "非表示" else "表示"}").setCanClick(false)
                        .setClickEvent {
                            hidden = !hidden
                            allRenderMenu()
                        })
                    setItem(16, SInventoryItem(
                        SItem(Material.OBSIDIAN).setDisplayName("§cスタック可能レシピ")
                            .addLore("§a現在:${if (stackRecipe) "有効" else "無効"}")
                            .addLore("§eレシピのアイテムをスタックできるようにするか")
                            .addLore("§e定型レシピのみ有効です")
                    ).setCanClick(false).setClickEvent {
                        stackRecipe = !stackRecipe
                        allRenderMenu()
                    })
                    return true
                }
            }
            moveChildInventory(optionMenu,it.whoClicked as Player)
        })

        setItem(44,createInputItem(SItem(Material.WRITABLE_BOOK).setDisplayName("§a保存"),String::class.java,"カテゴリー名を入力してください",
            invOpenCancel = true) { category, p ->
            Man10Crafting.sInput.sendInputCUI(p,String::class.java,"内部名を入力してください") { str ->
                if (save(str,category)){
                    p.sendPlainMessage("§a保存に成功しました")
                } else {
                    p.sendPlainMessage("§c完成品のアイテムが存在しません")
                    open(p)
                }
            }
        })

        return true
    }

    override fun save(namespace: String, category: String, recipeData: RecipeData): Boolean {
        val result = getItem(24)?:return false

        if (!stackRecipe) {
            listOf(10,11,12,19,20,21,28,29,30).forEach {
                inv.getItem(it)?.amount = 1
            }
        }

        recipeData.permission = perm
        recipeData.result = result
        recipeData.returnBottle = returnBottle
        recipeData.command = command
        recipeData.stackRecipe = stackRecipe

        if (isShaped){
            recipeData.type = RecipeData.Type.SHAPED

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
                return builder.toString()
            }

            val row1 = itemTask(10,11,12)
            val row2 = itemTask(19,20,21)
            val row3 = itemTask(28,29,30)
            recipeData.shape.addAll(listOf(row1,row2,row3))

        } else {
            recipeData.type = RecipeData.Type.SHAPELESS

            listOf(10,11,12,19,20,21,28,29,30).forEach {
                recipeData.shapelessMaterials.add(getItem(it)?:return@forEach)
            }
        }
        return super.save(namespace, category, recipeData)
    }
}