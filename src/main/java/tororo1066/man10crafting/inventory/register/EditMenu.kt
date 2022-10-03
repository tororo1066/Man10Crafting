package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
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

    override fun renderMenu(): Boolean {
        val items = LinkedHashMap<String,ArrayList<SInventoryItem>>()
        Man10Crafting.recipes.forEach { (key, data) ->
            val item = SInventoryItem(data.result).addLore(listOf("§f§l[${if (data.enabled) "§a§lEnabled" else "§c§lDisabled"}§f§l]","§eクリックで編集","§eシフトクリックで有効切替")).setCanClick(false).setClickEvent {
                if (it.click.isShiftClick){
                    val syncData = Man10Crafting.recipes[key]!!
                    syncData.enabled = !syncData.enabled
                    syncData.saveConfig()
                    renderMenu()
                    afterRenderMenu()
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
            }
        return recipeEditor
    }
}