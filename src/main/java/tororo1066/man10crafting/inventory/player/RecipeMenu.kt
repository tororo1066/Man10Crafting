package tororo1066.man10crafting.inventory.player

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.defaultMenus.CategorySInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class RecipeMenu(val p: Player): CategorySInventory(Man10Crafting.plugin,"${Man10Crafting.prefix}§6レシピ一覧") {

    override fun renderMenu(): Boolean {
        val filteredRecipes = Man10Crafting.recipes.filter { it.value.checkNeed(p) }
        val items = LinkedHashMap<String,ArrayList<SInventoryItem>>()
        filteredRecipes.forEach { (_, data) ->
            val item = SInventoryItem(data.result).setCanClick(false).setClickEvent {
                moveChildInventory(createRecipeMenu(data),p)
            }
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

                        setItem(23,data.type.material)
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

                        setItem(23,data.type.material)
                        setItem(25,moveOtherRecipeItem(p,this,data.result))

                    }

                    RecipeData.Type.FURNACE, RecipeData.Type.BLASTING, RecipeData.Type.SMOKING->{
                        setItem(11,moveOtherRecipeItem(p,this,data.singleMaterial))
                        setItem(20,white)
                        setItem(13,SItem(data.type.material).setLore(listOf("§7調理時間 §e${data.furnaceTime} ticks","§7経験値量 §e${data.furnaceExp}")))
                        setItem(24,moveOtherRecipeItem(p,this,data.result))
                    }

                    RecipeData.Type.SMITHING->{
                        setItem(19,moveOtherRecipeItem(p,this,data.singleMaterial))
                        setItem(21,moveOtherRecipeItem(p,this,data.smithingMaterial))
                        setItem(23,data.type.material)
                        setItem(25,moveOtherRecipeItem(p,this,data.result))
                    }
                }

                return true
            }
        }
        return recipeInfoMenu
    }

    fun moveOtherRecipeItem(p: Player, inv: SInventory, item: ItemStack): SInventoryItem {
        return SInventoryItem(item).setCanClick(false).setClickEvent {
            val recipe = (Man10Crafting.recipes.entries.filter { it.value.checkNeed(p) }.find { it.value.result.isSimilar(item) }?:return@setClickEvent).value
            inv.moveChildInventory(createRecipeMenu(recipe),p)
        }

    }
}