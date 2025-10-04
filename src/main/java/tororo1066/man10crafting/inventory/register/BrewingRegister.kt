package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.ingredient.ItemStackIngredient
import tororo1066.man10crafting.inventory.register.Items.emptyIngredientItem
import tororo1066.man10crafting.inventory.register.Items.optionsItem
import tororo1066.man10crafting.inventory.register.Items.setAdvancedModeItem
import tororo1066.man10crafting.recipe.paper.BrewingCraftingRecipe
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.otherClass.PlusInt
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class BrewingRegister(
    currentData: BrewingCraftingRecipe? = null
): AbstractRegister<BrewingCraftingRecipe>(
    "§dBrewing",
    currentData ?: BrewingCraftingRecipe(),
    currentData != null
) {

    var ingredient: Pair<AbstractIngredient, Int>? = null
    var input: Pair<AbstractIngredient, Int>? = null

    override val task: BukkitTask = object : BukkitRunnable() {
        override fun run() {
            if (!advancedMode) return
            ingredient?.let { ing ->
                val next = ing.first.getNextIndex(ing.second)
                ingredient = ing.first to next
                setItem(19, editItem(ing.first, next) { p, newIngredient ->
                    ingredient = if (newIngredient == null) null else newIngredient to 0
                    allRenderMenu(p)
                })
            }
            input?.let { ip ->
                val next = ip.first.getNextIndex(ip.second)
                input = ip.first to next
                setItem(22, editItem(ip.first, next) { p, newIngredient ->
                    input = if (newIngredient == null) null else newIngredient to 0
                    allRenderMenu(p)
                })
            }
        }
    }.runTaskTimer(SJavaPlugin.plugin, 20, 20)

    private fun editItem(
        ingredient: AbstractIngredient,
        index: Int,
        onSelect: (player: Player, newIngredient: AbstractIngredient?) -> Unit
    ) = ingredient.editItem(this, index) { player, newIngredient ->
        onSelect(player, newIngredient)
        allRenderMenu(player)
    }

    override fun readInput() {
        val ingredientItem = inv.getItem(19)
        val inputItem = inv.getItem(22)
        ingredient = ingredientItem?.let { itemStackToIngredient(it) }
        input = inputItem?.let { itemStackToIngredient(it) }
    }

    override fun prepareSave(): Boolean {
        val ing = ingredient?.first ?: return false
        val ip = input?.first ?: return false
        if (!ing.isValid() || !ip.isValid()) return false
        val result = inv.getItem(25) ?: return false

        currentData.ingredient = ing
        currentData.input = ip
        currentData.result = result
        return true
    }

    //0  1  2  3  o  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 i  20 21 b  23 24 r  26
    //27 28 29 ad 31 t  33 34 35
    //36 37 38 39 40 41 42 43 s
    //o: options, ad: advanced toggle, i: ingredient(19), b: base/input(21), r: result(25), s: save(44), t: time(31)

    init {
        fillItem(Items.backgroundLightBlue())
        setItem(
            10,
            SInventoryItem(Material.NETHER_WART)
                .setDisplayName("§a素材")
                .setCanClick(false)
        )
        setItem(
            13,
            SInventoryItem(Material.POTION)
                .setDisplayName("§dベース")
                .setCanClick(false)
        )
        removeItems(listOf(19,22,25))

        this.currentData.getIngredientOrNull()?.let { ing ->
            ingredient = ing to 0
        }
        this.currentData.getInputOrNull()?.let { ip ->
            input = ip to 0
        }
        this.currentData.getResultOrNull()?.let { res ->
            inv.setItem(25, res)
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val onlySupportedAdvanced = onlySupportedAdvanced(
            listOfNotNull(ingredient?.first, input?.first)
        )
        if (onlySupportedAdvanced) advancedMode = true

        setAdvancedModeItem(30, onlySupportedAdvanced)

        if (advancedMode) {
            fun setIngredientItem(slot: Int, pair: Pair<AbstractIngredient, Int>?, onSelect: (AbstractIngredient?) -> Unit) {
                if (pair == null) {
                    setItem(slot, emptyIngredientItem { newIng ->
                        onSelect(newIng)
                        allRenderMenu(p)
                    })
                } else {
                    setItem(slot, editItem(pair.first, pair.second) { player, newIng ->
                        onSelect(newIng)
                        allRenderMenu(player)
                    })
                }
            }
            setIngredientItem(19, ingredient) { newIng ->
                ingredient = if (newIng == null) null else newIng to 0
            }
            setIngredientItem(22, input) { newIp ->
                input = if (newIp == null) null else newIp to 0
            }
        } else {
            fun itemStackOrNull(pair: Pair<AbstractIngredient, Int>?): ItemStack? {
                val ing = pair?.first ?: return null
                if (ing !is ItemStackIngredient || ing.itemStacks.size != 1) return null
                return ing.itemStacks.first().clone()
            }
            removeItems(listOf(19,22))
            itemStackOrNull(ingredient)?.let { inv.setItem(19, it) } ?: run { ingredient = null }
            itemStackOrNull(input)?.let { inv.setItem(22, it) } ?: run { input = null }
        }

        setItem(
            32,
            createInputItem(
                SItem(Material.BLAZE_POWDER)
                    .setDisplayName("§c醸造時間")
                    .addLore("§6現在: ${currentData.brewingTime} ticks"),
                PlusInt::class.java,
                "§a数値を入力してください"
            ) { int, _ ->
                currentData.brewingTime = int.get()
            }
        )

        setItem(4, optionsItem(currentData))

        setItem(44, saveItem())
        return true
    }
}