package tororo1066.man10crafting.inventory.player

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlastFurnace
import org.bukkit.block.Furnace
import org.bukkit.block.Smoker
import org.bukkit.entity.Player
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.StonecutterInventory
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.defaultMenus.CategorySInventory
import tororo1066.tororopluginapi.defaultMenus.PagedSInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class RecipeMenu(val p: Player): CategorySInventory(Man10Crafting.plugin,"${Man10Crafting.prefix}§6レシピ一覧") {

    init {
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(): Boolean {
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

    fun createRecipeMenu(data: RecipeData, inv: SInventory): SInventory {
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
                                    setItem(loc, moveOtherRecipeItem(p,inv,material))
                                }
                            }
                        }

                        setItem(23,SInventoryItem(data.type.material).setCanClick(false))
                        setItem(25,moveOtherRecipeItem(p,inv,data.result))

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
                            setItem(loc,moveOtherRecipeItem(p,inv,itemStack))
                        }

                        setItem(23,SInventoryItem(data.type.material).setCanClick(false))
                        setItem(25,moveOtherRecipeItem(p,inv,data.result))

                    }

                    RecipeData.Type.FURNACE, RecipeData.Type.BLASTING, RecipeData.Type.SMOKING->{
                        setItem(11,moveOtherRecipeItem(p,inv,data.singleMaterial))
                        setItem(20,white)
                        setItem(13,SInventoryItem(data.type.material).setCanClick(false).setLore(listOf("§7調理時間 §e${data.furnaceTime} ticks","§7経験値量 §e${data.furnaceExp}")))
                        setItem(24,moveOtherRecipeItem(p,inv,data.result))
                    }

                    RecipeData.Type.SMITHING->{
                        setItem(19,moveOtherRecipeItem(p,inv,data.singleMaterial))
                        setItem(21,moveOtherRecipeItem(p,inv,data.smithingMaterial))
                        setItem(23,SInventoryItem(data.type.material).setCanClick(false))
                        setItem(25,moveOtherRecipeItem(p,inv,data.result))
                    }

                    RecipeData.Type.STONECUTTING->{
                        setItem(20,moveOtherRecipeItem(p,inv,data.singleMaterial))
                        setItem(24,moveOtherRecipeItem(p,inv,data.result))
                        setItems(29..33, SInventoryItem(Material.GREEN_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
                        setItem(31, SInventoryItem(Material.STONECUTTER).setCanClick(false))
                    }
                }

                if (creatable(p, data)){
                    setItems(48..50, SInventoryItem(Material.PURPLE_STAINED_GLASS_PANE)
                        .setDisplayName("§d自動設置する")
                        .setCanClick(false)
                        .setClickEvent {
                            inv.throughClose(p)
                            setRecipe(p, data, it.isShiftClick)
                        })
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
                        val menu = createRecipeMenu(it.value, this)
                        menu.renderMenu()
                        addPage(menu)
                    }
                }
            }
            inv.moveChildInventory(pagedInv,p)
        }

    }

    companion object {
        fun creatable(p: Player, recipe: RecipeData): Boolean {
            when(recipe.type){
                RecipeData.Type.SHAPED ->{
                    if ((p.getTargetBlockExact(5)?:return false).type != Material.CRAFTING_TABLE)return false
                    val shapeToItems = recipe.shape.map { it.mapNotNull { map -> recipe.materials[map]?.clone() } }.stream().flatMap { it.stream() }.toList()
                    val contents = p.inventory.contents.clone().map { it?.clone() }.toMutableList()
                    var checked = true
                    for (itemStack in shapeToItems){
                        val content = contents.find { it?.isSimilar(itemStack) == true }
                        if (content == null){
                            checked = false
                            break
                        }
                        if (content.amount == 1){
                            contents.removeAt(contents.indexOf(content))
                        }

                        content.amount--
                    }

                    return checked
                }

                RecipeData.Type.SHAPELESS ->{
                    if ((p.getTargetBlockExact(5)?:return false).type != Material.CRAFTING_TABLE)return false
                    val contents = p.inventory.contents.clone().map { it?.clone() }.toMutableList()
                    var checked = true
                    for (itemStack in recipe.shapelessMaterials){
                        val content = contents.find { it?.isSimilar(itemStack) == true }
                        if (content == null){
                            checked = false
                            break
                        }
                        if (content.amount == 1){
                            contents.removeAt(contents.indexOf(content))
                        }

                        content.amount--
                    }

                    return checked
                }

                RecipeData.Type.FURNACE, RecipeData.Type.BLASTING, RecipeData.Type.SMOKING, RecipeData.Type.STONECUTTING ->{
                    if ((p.getTargetBlockExact(5)?:return false).type != recipe.type.material)return false
                    val contents = p.inventory.contents.clone()
                    return contents.any { it?.isSimilar(recipe.singleMaterial) == true }
                }

                RecipeData.Type.SMITHING ->{
                    return false
//                if ((p.getTargetBlockExact(5)?:return false).type != Material.SMITHING_TABLE)return false
//                val contents = p.inventory.contents.clone()
//                contents.find { it?.isSimilar(recipe.singleMaterial) == true }?:return false
//                contents.find { it?.isSimilar(recipe.smithingMaterial) == true }?:return false
//                return true
                }
            }
        }

        fun creatableAmount(p: Player, recipe: RecipeData, isShiftClick: Boolean): Int {
            when(recipe.type){
                RecipeData.Type.SHAPED ->{
                    if ((p.getTargetBlockExact(5)?:return 0).type != Material.CRAFTING_TABLE)return 0
                    val shapeToItems = recipe.shape.map { it.mapNotNull { map -> recipe.materials[map]?.clone() } }.stream().flatMap { it.stream() }.toList()
                    val maxStackSize = shapeToItems.maxOf { it.maxStackSize }
                    val contents = p.inventory.contents.clone().map { it?.clone() }.toMutableList()
                    var checked = true
                    var amount = 0
                    if (isShiftClick){
                        var first = true
                        while (true){
                            if (amount >= maxStackSize)break
                            for (itemStack in shapeToItems){
                                val content = contents.find { it?.isSimilar(itemStack) == true }
                                if (content == null){
                                    checked = false
                                    break
                                }
                                if (content.amount == 1){
                                    contents.removeAt(contents.indexOf(content))
                                }

                                content.amount--
                            }

                            if (!checked){
                                break
                            }
                            first = false
                            amount++
                        }

                        if (!first) checked = true
                    } else {
                        for (itemStack in shapeToItems){
                            val content = contents.find { it?.isSimilar(itemStack) == true }
                            if (content == null){
                                checked = false
                                break
                            }
                            if (content.amount == 1){
                                contents.removeAt(contents.indexOf(content))
                            }

                            content.amount--
                        }
                        if (checked) amount = 1
                    }

                    if (checked){
                        return amount
                    }
                }

                RecipeData.Type.SHAPELESS ->{
                    if ((p.getTargetBlockExact(5)?:return 0).type != Material.CRAFTING_TABLE)return 0
                    val contents = p.inventory.contents.clone().map { it?.clone() }.toMutableList()
                    val maxStackSize = recipe.shapelessMaterials.maxOf { it.maxStackSize }
                    var checked = true
                    var amount = 0
                    if (isShiftClick){
                        var first = true
                        while (true){
                            if (amount >= maxStackSize)break
                            for (itemStack in recipe.shapelessMaterials){
                                val content = contents.find { it?.isSimilar(itemStack) == true }
                                if (content == null){
                                    checked = false
                                    break
                                }
                                if (content.amount == 1){
                                    contents.removeAt(contents.indexOf(content))
                                }

                                content.amount--
                            }

                            if (!checked){
                                break
                            }
                            first = false
                            amount++
                        }

                        if (!first) checked = true
                    } else {
                        for (itemStack in recipe.shapelessMaterials){
                            val content = contents.find { it?.isSimilar(itemStack) == true }
                            if (content == null){
                                checked = false
                                break
                            }
                            if (content.amount == 1){
                                contents.removeAt(contents.indexOf(content))
                            }

                            content.amount--
                        }

                        if (checked) amount = 1
                    }

                    if (checked){
                        return amount
                    }
                }

                RecipeData.Type.FURNACE, RecipeData.Type.BLASTING, RecipeData.Type.SMOKING, RecipeData.Type.STONECUTTING ->{
                    if ((p.getTargetBlockExact(5)?:return 0).type != recipe.type.material)return 0
                    val contents = p.inventory.contents.clone()
                    if (isShiftClick){
                        val items = contents.filter { it?.isSimilar(recipe.singleMaterial) == true }.filterNotNull()
                        var amount = items.sumOf { it.amount }
                        if (amount > recipe.singleMaterial.maxStackSize) amount = recipe.singleMaterial.maxStackSize
                        return amount
                    } else {
                        contents.find { it?.isSimilar(recipe.singleMaterial) == true }?:return 0
                        return 1
                    }
                }

                RecipeData.Type.SMITHING ->{
                    return 0
//                if ((p.getTargetBlockExact(5)?:return 0).type != Material.SMITHING_TABLE)return 0
//                val contents = p.inventory.contents.clone()
//                if (isShiftClick){
//                    val singleMaterials = contents.filter { it?.isSimilar(recipe.singleMaterial) == true }.filterNotNull()
//                    val smithingMaterials = contents.filter { it?.isSimilar(recipe.smithingMaterial) == true }.filterNotNull()
//
//                    var amount = min(singleMaterials.sumOf { it.amount }, smithingMaterials.sumOf { it.amount })
//                    val maxStackSize = min(recipe.singleMaterial.maxStackSize, recipe.smithingMaterial.maxStackSize)
//                    if (amount > maxStackSize) amount = maxStackSize
//
//                    return amount
//                } else {
//                    contents.find { it?.isSimilar(recipe.singleMaterial) == true }?:return 0
//                    contents.find { it?.isSimilar(recipe.smithingMaterial) == true }?:return 0
//                    return 1
//                }
                }
            }

            return 0
        }

        fun setRecipe(p: Player, recipe: RecipeData, isShiftClick: Boolean): Boolean {
            val amount = creatableAmount(p, recipe, isShiftClick)
            if (amount == 0)return false

            when(recipe.type){
                RecipeData.Type.SHAPED-> {
                    val tableInv = p.openWorkbench(p.location, true)!!.topInventory as CraftingInventory
                    recipe.shape.stream().flatMap { it.toCharArray().toList().stream() }.toList().forEachIndexed { i, char ->
                        val item = (recipe.materials[char]?:return@forEachIndexed).clone()
                        if (!takeItem(p.inventory, item, amount))return false
                        tableInv.setItem(i+1, item.apply { this.amount = amount })
                    }
                }

                RecipeData.Type.SHAPELESS-> {
                    val tableInv = p.openWorkbench(p.location, true)!!.topInventory as CraftingInventory
                    recipe.shapelessMaterials.forEachIndexed { i, item ->
                        val shapelessMaterial = item.clone()
                        if (!takeItem(p.inventory, shapelessMaterial, amount))return false
                        tableInv.setItem(i+1, shapelessMaterial.apply { this.amount = amount })
                    }
                }

                RecipeData.Type.FURNACE-> {
                    val furnace = p.getTargetBlockExact(5)!!.state as Furnace
                    p.openInventory(furnace.inventory)
                    val material = recipe.singleMaterial.clone()
                    if (!takeItem(p.inventory, material, amount))return false
                    furnace.inventory.smelting = material.apply { this.amount = amount }
                }

                RecipeData.Type.BLASTING-> {
                    val furnace = p.getTargetBlockExact(5)!!.state as BlastFurnace
                    p.openInventory(furnace.inventory)
                    val material = recipe.singleMaterial.clone()
                    if (!takeItem(p.inventory, material, amount))return false
                    furnace.inventory.smelting = material.apply { this.amount = amount }
                }

                RecipeData.Type.SMOKING-> {
                    val furnace = p.getTargetBlockExact(5)!!.state as Smoker
                    p.openInventory(furnace.inventory)
                    val material = recipe.singleMaterial.clone()
                    if (!takeItem(p.inventory, material, amount))return false
                    furnace.inventory.smelting = material.apply { this.amount = amount }
                }

                RecipeData.Type.SMITHING-> {
                    return true
                }

                RecipeData.Type.STONECUTTING-> {
                    val stoneCutter = p.openStonecutter(p.location, true)!!.topInventory as StonecutterInventory
                    val material = recipe.singleMaterial.clone()
                    if (!takeItem(p.inventory, material, amount))return false
                    stoneCutter.inputItem = material.apply { this.amount = amount }
                }
            }

            return true
        }

        fun takeItem(inv: Inventory, item: ItemStack, amount: Int): Boolean {
            val items = inv.contents.filter { it != null && it.isSimilar(item) }.filterNotNull()
            if (amount > items.sumOf { it.amount })return false
            var variableAmount = amount
            for (singleItem in items){
                if (singleItem.amount >= variableAmount){
                    singleItem.amount -= variableAmount
                    return true
                }
                variableAmount -= singleItem.amount
                singleItem.amount = 0
            }

            return variableAmount == 0
        }
    }
}