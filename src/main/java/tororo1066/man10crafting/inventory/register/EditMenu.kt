package tororo1066.man10crafting.inventory.register

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.man10crafting.inventory.register.furnace.BlastingRegister
import tororo1066.man10crafting.inventory.register.furnace.FurnaceRegister
import tororo1066.man10crafting.inventory.register.furnace.SmokingRegister
import tororo1066.tororopluginapi.defaultMenus.CategorySInventory
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class EditMenu: CategorySInventory(Man10Crafting.plugin,"EditMenu") {

    init {
        registerClickSound()
    }

    override fun renderMenu(): Boolean {
        val items = LinkedHashMap<String,ArrayList<SInventoryItem>>()
        Man10Crafting.recipes.entries.sortedBy { it.value.index }.forEach { (key, data) ->
            val item = SInventoryItem(data.result).addLore(listOf("§f§l[${if (data.enabled) "§a§lEnabled" else "§c§lDisabled"}§f§l]","§a優先度:${data.index}","§7${key}","§eクリックで編集","§6シフトクリックで有効切替","§cシフト右クリックで削除")).setCanClick(false).setClickEvent {
                if (it.click.isShiftClick){
                    if (it.click == ClickType.SHIFT_RIGHT){
                        Bukkit.removeRecipe(NamespacedKey(Man10Crafting.plugin,key))
                        Man10Crafting.recipes.remove(key)
                        allRenderMenu()
                        return@setClickEvent
                    }
                    val syncData = Man10Crafting.recipes[key]!!
                    syncData.enabled = !syncData.enabled
                    syncData.saveConfig()
                    allRenderMenu()
                } else {
                    moveChildInventory(editMenu(data),it.whoClicked as Player)
                }
            }
            val transKey = SLang.translate("categories.${data.category}")
            if (nowCategory == "") setCategoryName(transKey)
            if (!items.containsKey(transKey)){
                items[transKey] = arrayListOf()
            }

            items[transKey]!!.add(item)
        }
        setResourceItems(items)

        return true
    }

    fun editMenu(data: RecipeData): SInventory {

        fun saveItem(): SInventoryItem {
            return SInventoryItem(Material.WRITABLE_BOOK).setDisplayName("§a上書き保存").setCanClick(false)
        }

        val recipeEditor : SInventory =
            when(data.type){
                RecipeData.Type.SHAPED-> {
                    val inv = object : NormalCraftRegister(){
                        init {
                            isShaped = true
                            returnBottle = data.returnBottle
                            perm = data.permission
                            command = data.command
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            data.shape.forEachIndexed { index, str ->
                                str.toCharArray().forEachIndexed { charIndex, char ->
                                    val loc = (10 + (index * 9)) + charIndex
                                    if (!char.isWhitespace()){
                                        val material = data.materials[char]!!
                                        setItem(loc, material)
                                    }
                                }
                            }
                            setItem(24,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.SHAPELESS-> {
                    val inv = object : NormalCraftRegister(){
                        init {
                            isShaped = false
                            returnBottle = data.returnBottle
                            perm = data.permission
                            command = data.command
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            data.shapelessMaterials.forEachIndexed { index, itemStack ->
                                val loc = when(index){
                                    in 0..2->{
                                        10 + index
                                    }
                                    in 3..5->{
                                        16 + index
                                    }
                                    in 6..8->{
                                        22 + index
                                    }
                                    else -> 0
                                }
                                setItem(loc,itemStack)
                            }
                            setItem(24,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.FURNACE-> {
                    val inv = object : FurnaceRegister(){
                        init {
                            exp = data.furnaceExp
                            furnaceTime = data.furnaceTime
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.SMOKING-> {
                    val inv = object : SmokingRegister(){
                        init {
                            exp = data.furnaceExp
                            furnaceTime = data.furnaceTime
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.BLASTING-> {
                    val inv = object : BlastingRegister(){
                        init {
                            exp = data.furnaceExp
                            furnaceTime = data.furnaceTime
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.SMITHING-> {
                    val inv = object : SmithingRegister(){
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(19,data.singleMaterial)
                            setItem(22,data.smithingMaterial)
                            setItem(25,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.STONECUTTING-> {
                    val inv = object : StoneCuttingRegister(){
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem().setClickEvent {
                                val p = it.whoClicked as Player
                                if (save(data.namespace,data.category)){
                                    p.sendMessage("§a保存に成功しました")
                                    p.closeInventory()
                                } else {
                                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                                }
                            })
                            return true
                        }
                    }

                    inv
                }
            }
        return recipeEditor
    }
}