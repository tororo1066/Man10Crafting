package tororo1066.man10crafting.inventory.player

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.defaultMenus.CategorySInventory
import tororo1066.tororopluginapi.defaultMenus.PagedSInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class RecipeMenu(val p: Player): CategorySInventory(Man10Crafting.plugin,"${Man10Crafting.prefix}§6レシピ一覧") {


    override fun renderMenu(): Boolean {
        setOnClick {
            it.isCancelled = true
        }
        val filteredRecipes = Man10Crafting.recipes.filter { it.value.checkNeed(p) }.entries.sortedBy { it.value.index }
        val items = LinkedHashMap<String,ArrayList<SInventoryItem>>()
        filteredRecipes.forEach { (_, data) ->
            val item = moveOtherRecipeItem(p, this, data.result)
            val transKey = translate("categories.${data.category}")
            if (nowCategory == "") setCategoryName(transKey)
            if (!items.containsKey(transKey)){
                items[transKey] = arrayListOf()
            }

            items[transKey]!!.add(item)
        }
        setResourceItems(items)

        return true

    }

    fun createRecipeMenu(data: RecipeData): SInventory {
        val recipeInfoMenu = object : SInventory(Man10Crafting.plugin,data.result.itemMeta.displayName + "§l§7のレシピ",6) {
            override fun renderMenu(): Boolean {
                setOnClick {
                    it.isCancelled = true
                }
                val white = SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
                val gray = SInventoryItem(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)

                setItems(0..8,white)
                setItems(36..53,white)
                setItems(9..35,gray)

                when(data.type){
                    RecipeData.Type.SHAPED->{
                        data.shape.forEachIndexed { index, str ->
                            str.toCharArray().forEachIndexed { charIndex, char ->
                                val loc = (10 + (index * 9)) + charIndex
                                if (char.isWhitespace()){
                                    removeItem(loc)
                                } else {
                                    val material = data.materials[char]!!
                                    setItem(loc, moveOtherRecipeItem(p,this,material))
                                }
                            }
                        }

                        setItem(23,SInventoryItem(data.type.material).setCanClick(false))
                        setItem(25,moveOtherRecipeItem(p,this,data.result))

                    }

                    RecipeData.Type.SHAPELESS->{
                        removeItems(10..12)
                        removeItems(19..21)
                        removeItems(28..30)
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
                            setItem(loc,moveOtherRecipeItem(p,this,itemStack))
                        }

                        setItem(23,SInventoryItem(data.type.material).setCanClick(false))
                        setItem(25,moveOtherRecipeItem(p,this,data.result))

                    }

                    RecipeData.Type.FURNACE, RecipeData.Type.BLASTING, RecipeData.Type.SMOKING->{
                        setItem(11,moveOtherRecipeItem(p,this,data.singleMaterial))
                        setItem(20,white)
                        setItem(13,SInventoryItem(data.type.material).setCanClick(false).setLore(listOf("§7調理時間 §e${data.furnaceTime} ticks","§7経験値量 §e${data.furnaceExp}")))
                        setItem(24,moveOtherRecipeItem(p,this,data.result))
                    }

                    RecipeData.Type.SMITHING->{
                        setItem(19,moveOtherRecipeItem(p,this,data.singleMaterial))
                        setItem(21,moveOtherRecipeItem(p,this,data.smithingMaterial))
                        setItem(23,SInventoryItem(data.type.material).setCanClick(false))
                        setItem(25,moveOtherRecipeItem(p,this,data.result))
                    }

                    RecipeData.Type.STONECUTTING->{
                        setItem(20,moveOtherRecipeItem(p,this,data.singleMaterial))
                        setItem(24,moveOtherRecipeItem(p,this,data.result))
                        setItems(29..33, SInventoryItem(Material.GREEN_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
                        setItem(31, SInventoryItem(Material.STONECUTTER).setCanClick(false))
                    }
                }

                return true
            }
        }
        return recipeInfoMenu
    }

    fun moveOtherRecipeItem(p: Player, inv: SInventory, item: ItemStack): SInventoryItem {
        return SInventoryItem(item).setCanClick(false).setClickEvent { e ->
            val recipe = if (e.click.isRightClick){
                Man10Crafting.recipes.filter {
                    if (!it.value.checkNeed(p))return@filter false
                    when(it.value.type){
                        RecipeData.Type.SHAPELESS-> it.value.shapelessMaterials.any { any -> any.isSimilar(item) }
                        RecipeData.Type.SHAPED-> it.value.materials.values.any { any -> any.isSimilar(item) }
                        RecipeData.Type.FURNACE, RecipeData.Type.BLASTING, RecipeData.Type.SMOKING, RecipeData.Type.STONECUTTING -> {
                            it.value.singleMaterial.isSimilar(item)
                        }
                        RecipeData.Type.SMITHING-> it.value.singleMaterial.isSimilar(item) || it.value.smithingMaterial.isSimilar(item)
                    }
                }
            } else {
                Man10Crafting.recipes.filter { it.value.checkNeed(p) && it.value.result.isSimilar(item) }
            }
            if (recipe.isEmpty())return@setClickEvent
            val pagedInv = object : PagedSInventory(Man10Crafting.plugin,
                if (e.click.isRightClick) item.itemMeta.displayName + "§l§7を使ったレシピ" else item.itemMeta.displayName + "§l§7のレシピ",6){
                init {
                    setOnClick {
                        it.isCancelled = true
                    }
                    setLeftSlots((45..47).toList())
                    setRightSlots((51..53).toList())
                    recipe.forEach {
                        val menu = createRecipeMenu(it.value)
                        menu.renderMenu()
                        addPage(menu)
                    }
                }
            }
            inv.moveChildInventory(pagedInv,p)
        }

    }
}