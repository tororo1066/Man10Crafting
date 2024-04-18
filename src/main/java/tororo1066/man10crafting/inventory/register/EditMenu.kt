package tororo1066.man10crafting.inventory.register

import smithcrafting.base.SmithBase
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
import tororo1066.tororopluginapi.utils.returnItem

class EditMenu: CategorySInventory(Man10Crafting.plugin,"EditMenu") {

    var nowEditCategory = ""

    init {
        registerClickSound()
    }

    override fun renderMenu(): Boolean {
        val items = LinkedHashMap<String,ArrayList<SInventoryItem>>()
        Man10Crafting.recipes.entries.sortedBy { it.value.index }.forEach { (key, data) ->
            val item = SInventoryItem(data.result)
                .addLore(
                    "§f§l[${if (data.enabled) "§a§lEnabled" else "§c§lDisabled"}§f§l]",
                    "§a優先度:${data.index}",
                    "§d登録優先度:${data.registerIndex}",
                    "§b隠す:${data.hidden}",
                    "§7${key}", "§eクリックで編集",
                    "§6シフトクリックで有効切替",
                    "§cシフト右クリックで削除"
                )
                .setCanClick(false)
                .setClickEvent {
                    if (it.click == ClickType.NUMBER_KEY) {
                        when(it.hotbarButton) {
                            1 -> {
                                (it.whoClicked as Player).returnItem(data.result)
                            }
                        }
                        return@setClickEvent
                    }
                    if (it.click.isShiftClick) {
                        if (it.click == ClickType.SHIFT_RIGHT) {
                            Bukkit.removeRecipe(NamespacedKey(Man10Crafting.plugin, key))
                            Man10Crafting.recipes.remove(key)
                            allRenderMenu()
                            return@setClickEvent
                        }
                        val syncData = Man10Crafting.recipes[key]!!
                        syncData.enabled = !syncData.enabled
                        syncData.saveConfig()
                        allRenderMenu()
                    } else {
                        moveChildInventory(editMenu(data), it.whoClicked as Player)
                    }
                }
            val transKey = SLang.translate("categories.${data.category}")
            if (nowCategory == "") {
                setCategoryName(transKey)
                nowEditCategory = data.category
            }
            if (!items.containsKey(transKey)){
                items[transKey] = arrayListOf()
            }

            items[transKey]!!.add(item)
        }
        setResourceItems(items)

        return true
    }

    override fun afterRenderMenu() {
        super.afterRenderMenu()
        getSInvItems()[49]?.let {

        }
    }

    private fun editMenu(data: RecipeData): SInventory {

        fun AbstractRegister.saveItem(): SInventoryItem {
            return SInventoryItem(Material.WRITABLE_BOOK).setDisplayName("§a上書き保存").setCanClick(false).setClickEvent {
                val p = it.whoClicked as Player
                if (save(data.namespace,data.category)){
                    p.sendMessage("§a保存に成功しました")
                    p.closeInventory()
                } else {
                    p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                }
            }
        }

        val recipeEditor : SInventory =
            when(data.type){
                RecipeData.Type.SHAPED-> {
                    val inv = object : NormalCraftRegister(){
                        init {
                            index = data.index
                            isShaped = true
                            returnBottle = data.returnBottle
                            perm = data.permission
                            command = data.command
                            hidden = data.hidden
                            stackRecipe = data.stackRecipe
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
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.SHAPELESS-> {
                    val inv = object : NormalCraftRegister(){
                        init {
                            index = data.index
                            isShaped = false
                            returnBottle = data.returnBottle
                            perm = data.permission
                            command = data.command
                            hidden = data.hidden
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
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.FURNACE-> {
                    val inv = object : FurnaceRegister(){
                        init {
                            index = data.index
                            registerIndex = data.registerIndex
                            exp = data.furnaceExp
                            furnaceTime = data.furnaceTime
                            hidden = data.hidden
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.SMOKING-> {
                    val inv = object : SmokingRegister(){
                        init {
                            index = data.index
                            registerIndex = data.registerIndex
                            exp = data.furnaceExp
                            furnaceTime = data.furnaceTime
                            hidden = data.hidden
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.BLASTING-> {
                    val inv = object : BlastingRegister(){
                        init {
                            index = data.index
                            registerIndex = data.registerIndex
                            exp = data.furnaceExp
                            furnaceTime = data.furnaceTime
                            hidden = data.hidden
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.SMITHING-> {
                    val inv = Man10Crafting.smithUtil.editInventory(
                        Man10Crafting.plugin,
                        Man10Crafting.sInput,
                        SmithBase.SaveData(
                            data.namespace,
                            data.category,
                            data.index,
                            data.registerIndex,
                            data.hidden,
                            data.singleMaterial,
                            data.smithingMaterial,
                            data.smithingAdditionalMaterial,
                            data.result,
                            data.smithingCopyNbt,
                            data.smithingTransform
                        )
                    ) { saveData ->
                        val result = saveData.result
                        val material = saveData.singleMaterial
                        val smithingMaterial = saveData.smithingMaterial
                        val additionalMaterial = saveData.additionalMaterial
                        val recipeData = RecipeData()
                        recipeData.type = RecipeData.Type.SMITHING
                        recipeData.singleMaterial = material
                        recipeData.smithingMaterial = smithingMaterial
                        recipeData.result = result
                        recipeData.smithingAdditionalMaterial = additionalMaterial

                        recipeData.namespace = saveData.namespace
                        recipeData.category = saveData.category
                        recipeData.index = saveData.index
                        recipeData.registerIndex = saveData.registerIndex
                        recipeData.saveConfig()
                        recipeData.register()

                        return@editInventory true
                    }

                    inv
                }

                RecipeData.Type.STONECUTTING-> {
                    val inv = object : StoneCuttingRegister(){
                        init {
                            index = data.index
                            registerIndex = data.registerIndex
                            hidden = data.hidden
                        }

                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(20,data.singleMaterial)
                            setItem(24,data.result)
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }

                RecipeData.Type.BREWING-> {
                    val inv = object : BrewingRegister(){
                        init {
                            index = data.index
                            registerIndex = data.registerIndex
                            brewingTime = data.brewingTime
                            hidden = data.hidden
                        }
                        override fun renderMenu(): Boolean {
                            super.renderMenu()
                            setItem(19,data.singleMaterial)
                            setItem(22,data.potionInput)
                            setItem(25,data.result)
                            setItem(43,saveItem())
                            return true
                        }
                    }

                    inv
                }
            }
        return recipeEditor
    }
}